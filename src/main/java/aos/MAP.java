package aos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import helpers.Linker;

/**
 * The MAP protocol class
 * @author zeqing
 *
 */
public class MAP extends SpanTree{
    
    private List<Integer> sendList; 
    private static int ScalarClock = 0;
    private volatile static int msgNum = 0;
    
    private volatile boolean active = false;
    private volatile boolean prevState = false;
    private CountDownLatch latch = null;
    
    private final int MAX_NUMBER;
    private final int MIN_PER_ACTIVE;     // "Minimum number of active node at each round";
    private final int MAX_PER_ACTIVE;         // "Maximum number of active node at each round";
    private final int MIN_SEND_DELAY;         // "Minimum number of send interval";
    
    // private Map<GlobalParams, Integer> globalParams;   // MAP protocol control variables
    
    
    //public String name;
    
    //public boolean isRoot;
    
    public MAP(Linker link, Map<GlobalParams, Integer> globalParams){
        super(link);
        // , String name 
        // this.isRoot = isRoot;
        //this.name = name;
        //this.globalParams = globalParams;
        this.MAX_NUMBER = globalParams.get(GlobalParams.MAX_NUMBER);
        this.MIN_PER_ACTIVE = globalParams.get(GlobalParams.MIN_PER_ACTIVE);
        this.MAX_PER_ACTIVE = globalParams.get(GlobalParams.MAX_PER_ACTIVE);
        this.MIN_SEND_DELAY = globalParams.get(GlobalParams.MIN_SEND_DELAY);
        this.active = (globalParams.get(GlobalParams.LOCAL_STATE) == 1);
        this.latch = new CountDownLatch(1);
    }
    
    
    //send messages to selected nodes
    public synchronized void sendApplicationMessage() throws Exception {
        // Init each interval
        latch = new CountDownLatch(1);       // reset latch
        prevState = false;
        
        List<Node> neighbors = linker.getNeighbors();
        getSendList(neighbors);
        
        System.out.println(String.format("[Node %d] [MAP] Send messages to %s",myId, sendList.toString()));
        //if node active and send less than max number
        if( active && msgNum < MAX_NUMBER) {
            
            //send to the neighbor
            for(int dstId : sendList){
                ScalarClock++;
                linker.sendMessage(dstId, Tag.APP, Integer.toString(ScalarClock));
                System.out.println(String.format("[Node %d] "
                        + "[From %d to %d] [ScalarClock %d]",myId, myId, dstId, ScalarClock ));
                msgNum++;  
            }
//            for(int i = 0; i < sendList.size(); i++)
//            {
//                ScalarClock++;
//                linker.sendMessage(sendList.get(i), Tag.APP, Integer.toString(ScalarClock));
//                System.out.println(
//                        String.format("[Node %d] [From %d to %d] [ScalarClock %d]", 
//                                myId, myId, sendList.get(i), ScalarClock ));
//                msgNum++;           
//            }
            toggleState();   // Toggle off to passive
            
            System.out.println(String.format("[Node %d] [Total %d Message Has Been Sent]",myId, msgNum));
        } else if (msgNum >= MAX_NUMBER ) { //if already send max number of messages
            active = false;
        } else { // Inactive
            return;
        }
        
    }
    
    
    
    //handle messages
    @Override
    public void handleMessage(Message msg, int srcId, Tag tag) throws IOException{
        switch (tag){
            case APP:
                handleApplicationMessage(msg, srcId, tag);
                break;
            default: // Pass message to deeper layer
                super.handleMessage(msg, srcId, tag);
        }
    }
    
    
    public void handleApplicationMessage(Message msg, int srcId, Tag tag){
        if (msgNum < MAX_NUMBER) {              // Qualified to send message 
            
            if (!active) {
                
                toggleState();
                System.out.println(String.format("[Node %d] [MAP] State has been changed to active.",myId));
                updateScalarClock(Integer.parseInt(msg.getContent()));
                
            } else {                           // Received message whilst being active 
                System.out.println(String.format("[Node %d] [MAP] Receive message when active %s",myId, msg.toString()));
                prevState = active;
                
            }
            latch.countDown();
        }
        // Ignore the messages if out of message quota
    }
    
    
  //randomly choose neighbor to send msgs
    public Node chooseNeighbor(List<Node> neighbors)
    {
        Random random = new Random();
        return neighbors.get(random.nextInt(neighbors.size()));
    }
    
    
    //randomly choose how many massages will be sent
    public int setRoundNum()
    {
        Random random = new Random();
        int max = MAX_PER_ACTIVE;
        int min = MIN_PER_ACTIVE;
        int numMsg = random.nextInt((max-min)+1)+min;
        return numMsg;
    }
    
    
    //generate list for send
    public void getSendList(List<Node> neighbors)
    {
        int numMsg = setRoundNum();
        sendList = new ArrayList<Integer>();
        while (numMsg != 0)
        {
            Node dstNeighbor = chooseNeighbor(neighbors);
            sendList.add(dstNeighbor.getNodeId());
            numMsg --;
        }
    }
    
    
    //change state once receive message or finish sending messages
    public void toggleState()
    {
        // Change state from active to passive
        // or change state from passive to active
        active = !active;
//        if(globalParams.get(GlobalParams.LOCAL_STATE) == 1)
//        {
//            globalParams.put(GlobalParams.LOCAL_STATE, 0);
//        }
//        //change state from passive to active
//        else
//        {
//            globalParams.put(GlobalParams.LOCAL_STATE, 1);
//        }
    }
    
    
    //get clock value
    public int getClock()
    {
        return ScalarClock;
    }
    
    
    //determine stop or not
    public boolean isStop()  {
        if (msgNum < MAX_NUMBER) {
            return false;
        } else {
            active = false;
            return true;
        }
        
//        if(msgNum >= globalParams.get(GlobalParams.MAX_NUMBER))
//        {
//            globalParams.put(GlobalParams.LOCAL_STATE, 0);
//            return true;
//        }
//        else
//            return false;
    }
    
    //clock action for receive 
    public void updateScalarClock(int scalarClock)
    {
        ScalarClock =  Math.max(ScalarClock, scalarClock);  
    }
    
    /**
     * Check node status
     * @return
     */
    public boolean isActive(){
        return active;
    }

    /**
     * Retrieve MAX_NUMBER
     * @return
     */
    public int getMAX_NUMBER() {
        return MAX_NUMBER;
    }

    /**
     * Retrieve MIN_PER_ACTIVE
     * @return
     */
    public int getMIN_PER_ACTIVE() {
        return MIN_PER_ACTIVE;
    }


    /**
     * Retrieve MAX_PER_ACTIVE
     * @return
     */
    public int getMAX_PER_ACTIVE() {
        return MAX_PER_ACTIVE;
    }

    /**
     * Retrieve MIN_SEND_DELAY
     * @return
     */
    public int getMIN_SEND_DELAY() {
        return MIN_SEND_DELAY;
    }
    
    public void await() throws InterruptedException{
        latch.await();
    }


    /**
     * Retrieve previous state
     * @return
     */
    public boolean isPrevActive() {
        return prevState;
    }
}

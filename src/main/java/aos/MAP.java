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
    
    public static final boolean ACTIVE = true;
    public static final boolean PASSIVE = false;
    
    /**
     * Logic Clock
     */
    private static int scalarClock = 0;
    
    /**
     * Number of messages have been sent
     */
    private volatile static int messageSentCount = 0;
    
    /**
     * Current node state. Either active(True) or passive(False)
     */
    protected volatile boolean state = false;
    
    /**
     * Previous node state.
     */
    protected volatile boolean prevState = false;
    
    /**
     * A latch to control send event. Count down when current node receives a message from a peer.
     */
    private CountDownLatch latch = null;      // Control consecutive active state
    
    /**
     * Maximum number of messages a node can send beform it permenantly halt 
     */
    public final int MAX_NUMBER;
    
    /**
     * Minimum number of active node at each round
     */
    public final int MIN_PER_ACTIVE;     
    
    /**
     * Maximum number of active node at each round
     */
    public final int MAX_PER_ACTIVE;        
    
    /**
     * Minimum number of send interval between two consecutive active state.
     */
    public final int MIN_SEND_DELAY; 
    
    public MAP(Linker link, Map<PropertyType, Integer> globalParams){
        super(link);
        this.MAX_NUMBER = globalParams.get(PropertyType.MAX_NUMBER);
        this.MIN_PER_ACTIVE = globalParams.get(PropertyType.MIN_PER_ACTIVE);
        this.MAX_PER_ACTIVE = globalParams.get(PropertyType.MAX_PER_ACTIVE);
        this.MIN_SEND_DELAY = globalParams.get(PropertyType.MIN_SEND_DELAY);
        this.state = (globalParams.get(PropertyType.LOCAL_STATE) == 1);
        this.latch = new CountDownLatch(1);
    }
    
    
    /**
     * Send messages to selected nodes
     * @throws IOException 
     * @throws InterruptedException 
     * 
     * @throws Exception
     */
    public void sendApplicationMessage() throws IOException, InterruptedException {
        // Init each interval
        latch = new CountDownLatch(1);       // reset latch
//        prevState = PASSIVE;
        
        List<Integer> sendList = generateSendList();
        
        
        System.out.format("[Node %d] [MAP] Send messages to %s\n", myId, sendList.toString());
        
        for(int dstId : sendList){
            scalarClock++;
            sendMessageWithScalar(dstId, Tag.APP, "", scalarClock);
            messageSentCount++;
            Thread.sleep(MIN_SEND_DELAY);
            System.out.println(String.format( "[Node %d] [MAP] [From %d to %d] [ScalarClock %d]\n",
                    myId, myId, dstId, scalarClock ));
        }
        
        state = PASSIVE;
        
        System.out.println(String.format("[Node %d] [MAP] [Total %d Message Has Been Sent]\n", myId, messageSentCount));        
    }
    
    
    
    //handle messages
    @Override
    public void handleMessage(Message msg, int srcId, Tag tag) throws IOException{
        switch (tag){
            case APP:
                vClock.tick();
                handleApplicationMessage(msg, srcId, tag);
                break;
            default: // Pass message to deeper layer
                super.handleMessage(msg, srcId, tag);
        }
    }
    
    
    public void handleApplicationMessage(Message msg, int srcId, Tag tag){
        if (messageSentCount < MAX_NUMBER) {              // Qualified to send message 
            if (state == PASSIVE) {
                state = ACTIVE;
                latch.countDown();
                System.out.println(String.format("[Node %d] [MAP] State has been changed to active.\n", myId));
                receiveAction(msg.getScalar());
            } else {                           // Received message whilst being active 
                System.out.println(String.format("[Node %d] [MAP] Receive message when active %s\n",myId, msg.toString()));
                // prevState = ACTIVE;                
            }
            
        }
        // Ignore the messages if out of message quota
    }
    
    
    // Randomly choose neighbor to send msgs
    public Node chooseNeighbor(List<Node> neighbors) {
        Random random = new Random();
        return neighbors.get(random.nextInt(neighbors.size()));
    }
    
    
    //randomly choose how many massages will be sent
    public int setRoundNum() {
        Random random = new Random();
        int max = MAX_PER_ACTIVE;
        int min = MIN_PER_ACTIVE;
        return random.nextInt((max-min)+1)+min;
    }
    

    /**
     * Random [MIN_PER_ACTIVE, MAX_PER_ACTIVE] destination nodes per interval
     * @return Generated List
     */
    public List<Integer> generateSendList() {
        int numMsg = setRoundNum();
        List<Integer> sendList = new ArrayList<>();
        while (numMsg != 0) {
            Node dstNeighbor = chooseNeighbor(linker.getNeighbors());
            sendList.add(dstNeighbor.getNodeId());
            numMsg --;
        }
        return sendList;
    }
    
    //get clock value
    public int getClock() {
        return scalarClock;
    }
    
    
    //determine stop or not
    public boolean isStop()  {
        
        if (messageSentCount < MAX_NUMBER) {
            return false;
        } else {
            state = PASSIVE;
            return true;
        }
    }
    
    //clock action for receive 
    public void receiveAction(int sClock) {
        scalarClock =  Math.max(scalarClock, sClock);  
    }
    
    /**
     * Check node status
     * @return
     */
    public boolean isActive() {
        return state;
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

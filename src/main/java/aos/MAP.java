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
    private volatile static int msgNum = 0;
    
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
    
    public MAP(Linker link, Map<GlobalParams, Integer> globalParams){
        super(link);
        this.MAX_NUMBER = globalParams.get(GlobalParams.MAX_NUMBER);
        this.MIN_PER_ACTIVE = globalParams.get(GlobalParams.MIN_PER_ACTIVE);
        this.MAX_PER_ACTIVE = globalParams.get(GlobalParams.MAX_PER_ACTIVE);
        this.MIN_SEND_DELAY = globalParams.get(GlobalParams.MIN_SEND_DELAY);
        this.state = (globalParams.get(GlobalParams.LOCAL_STATE) == 1);
        this.latch = new CountDownLatch(1);
    }
    
    
    /**
     * Send messages to selected nodes
     * 
     * @throws Exception
     */
    public synchronized void sendApplicationMessage() throws Exception {
        // Init each interval
        latch = new CountDownLatch(1);       // reset latch
        prevState = PASSIVE;
        
        List<Integer> sendList = generateSendList();
        msgNum += sendList.size();
        
        System.out.println(String.format("[Node %d] [MAP] Send messages to %s",myId, sendList.toString()));
        //send to the neighbor
        for(int dstId : sendList){
            scalarClock++;
            sendMessageWithScalar(dstId, Tag.APP, "", scalarClock);
            System.out.println(String.format("[Node %d] [MAP]"
                    + "[From %d to %d] [ScalarClock %d]",myId, myId, dstId, scalarClock ));
        }
        
        state = PASSIVE;
        
        System.out.println(String.format("[Node %d] [MAP] [Total %d Message Has Been Sent]",myId, msgNum));        
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
        if (msgNum < MAX_NUMBER) {              // Qualified to send message 
            
            if (state == PASSIVE) {
                state = ACTIVE;
                System.out.println(String.format("[Node %d] [MAP] State has been changed to active.",myId));
                receiveAction(msg.getScalar());
            } else {                           // Received message whilst being active 
                System.out.println(String.format("[Node %d] [MAP] Receive message when active %s",myId, msg.toString()));
                prevState = ACTIVE;                
            }
            latch.countDown();
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
        int numMsg = random.nextInt((max-min)+1)+min;
        return numMsg;
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
    
//    
//    //change state once receive message or finish sending messages
//    public void toggleState() {
//        // Change state from active to passive
//        // or change state from passive to active
//        state = !state;
//    }
//    
    
    //get clock value
    public int getClock() {
        return scalarClock;
    }
    
    
    //determine stop or not
    public boolean isStop()  {
        
        if (msgNum < MAX_NUMBER) {
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
        state = ACTIVE;
    }


    /**
     * Retrieve previous state
     * @return
     */
    public boolean isPrevActive() {
        return prevState;
    }
}

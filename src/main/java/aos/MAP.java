package aos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import clock.VectorClock;
import helpers.PropConst;
import socket.Linker;

/**
 * The MAP protocol class
 * @author zeqing
 *
 */
public class MAP extends SpanTree{
    
    public static final boolean ACTIVE = true;
    public static final boolean PASSIVE = false;
    
    /**
     * Number of messages have been sent
     */
    private volatile static int messageSentCount = 0;
    
    /**
     * Current node state. Either active(True) or passive(False)
     */
    protected volatile boolean state = false;
    
    
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
    
    public MAP(Linker link){
        super(link);
        
        this.MAX_NUMBER = Integer.parseInt(prop.getProperty(PropConst.MAX_NUMBER));
        this.MIN_PER_ACTIVE = Integer.parseInt(prop.getProperty(PropConst.MIN_PER_ACTIVE));
        this.MAX_PER_ACTIVE = Integer.parseInt(prop.getProperty(PropConst.MAX_PER_ACTIVE));
        this.MIN_SEND_DELAY = Integer.parseInt(prop.getProperty(PropConst.MIN_SEND_DELAY));
        this.state = (Integer.parseInt(prop.getProperty(PropConst.LOCAL_STATE)) == 1);
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
        
        List<Integer> sendList = generateSendList();
        
        
        System.out.format("[Node %d] [MAP] Send messages to %s\n", myId, sendList.toString());
        
        for(int dstId : sendList){            
            mutex.acquire();
            sendApplicatonMessage(dstId, "Application");
            mutex.release();
            
            messageSentCount++;
            Thread.sleep(MIN_SEND_DELAY);
        }
        
        state = PASSIVE;
        
        System.out.println(String.format("[Node %d] [MAP] [Total %d Message Has Been Sent]\n", myId, messageSentCount));        
    }
    
    
    @Override
    public synchronized void handleMessage(Message msg, int srcId, Tag tag) throws IOException{
        switch (tag){
            case APP:
                handleApplicationMessage(msg, srcId, tag);
                break;
            default: // Pass message to deeper layer
                super.handleMessage(msg, srcId, tag);
        }
    }
    
    /**
     * Handle application messages
     * @param msg
     * @param srcId
     * @param tag
     */
    public void handleApplicationMessage(Message msg, int srcId, Tag tag){
        System.out.println(String.format( "[Node %d] [MAP] [RECV] From %d] [Vector = %s] \n", myId, srcId, vClock.toString()));
        
        // Handle vector clock first
        vClock.receiveAction(msg.getVectorClock());
        
        if (messageSentCount < MAX_NUMBER) {   // Qualified to send message 
            if (state == PASSIVE) {
                state = ACTIVE;
                latch.countDown();
                System.out.println(String.format("[Node %d] [MAP] State has been changed to active.\n", myId));
            } else {                           // Received message whilst being active 
                System.out.println(String.format("[Node %d] [MAP] Receive message when active %s\n",myId, msg.toString()));                
            }
            
        }
       
        // Ignore the messages if exceed MAX_NUMBER
    }
    
    
    public synchronized void sendApplicatonMessage(int dstId, String content) throws IOException{
        vClock.sendAction();
        Message message = new Message(myId, dstId, Tag.APP, content, new VectorClock(vClock));
        sendMessage(dstId, message);
    }
    
    
    /**
     * Randomly choose neighbor to send messages
     * 
     * @param neighbors
     * @return
     */
    public Node chooseNeighbor(List<Node> neighbors) {
        Random random = new Random();
        return neighbors.get(random.nextInt(neighbors.size()));
    }
    
    /**
     * Randomly choose how many massages will be sent 
     * @return
     */
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
    
    //determine stop or not
    public boolean isStop()  {
        
        if (messageSentCount < MAX_NUMBER) {
            return false;
        } else {
            state = PASSIVE;
            return true;
        }
    }
    
    /**
     * Check node status
     * @return
     */
    public boolean isActive() {
        return state;
    }
    
    /**
     * Wait for incoming APP message to invoke this node
     * @throws InterruptedException
     */
    public void await() throws InterruptedException{
        latch.await();
    }
}

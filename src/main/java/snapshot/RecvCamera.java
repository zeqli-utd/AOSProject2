package snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import aos.GlobalParams;
import aos.MAP;
import aos.Message;
import aos.Node;
import aos.Tag;
import clock.VectorClock;
import helpers.Linker;

//Pj is a neighbor of Pi if there is a channel from Pi to Pj

public class RecvCamera extends MAP implements Camera, CamUser {
    private static final int WHITE = 0;                // Initial State
    private static final int RED = 1;                  // Snapshotted
    private int myColor = WHITE;
    
    
    private boolean closed[];   // closed[k] stop to recording messages along kth incomming channel
    private List<List<Message>> channels = null; //channels[k] records the state of the kth incoming channel
    
    //RecvCamera initialize the variables of the algorithm
    //All channels are initialized to empty
    public RecvCamera(Linker initLinker, Map<GlobalParams, Integer> globalParams) {
        super(initLinker, globalParams);
        
        this.closed = new boolean[numProc];  
        this.channels = new ArrayList<>(numProc);          
        
        // for each neighboring process Pk, closed[k] is initialized to false.
        for(int i = 0; i < numProc; i++){          
            channels.add(new ArrayList<>());
            closed[i] = false;
        } 
    }

    
    
    @Override
    // the method handleMessage gives the rule for receiving a marker message
    public void handleMessage(Message msg, int srcId, Tag tag) throws IOException {  
        int srcIdx = idToIndex(srcId);
        switch (tag){
            case MARKER:
                handleMarkerMessage(msg, srcId, tag);
                break;
            case APP:
                // handle application message, true if the application message is of type 'White-Red'
                if (myColor == RED && !closed[srcIdx]){
                    // If the application message is of type 'White-Red'
                    // then add the message to channels
                    channels.get(srcIdx).add(msg);     
                }
                // intentionally no breaking, passdown Application message.
            default:
                super.handleMessage(msg, srcId, tag);
        }
    }
    
    /**
     * Handle marker messages
     * @param msg 
     * @param srcId
     * @param tag
     */
    private void handleMarkerMessage(Message msg, int srcId, Tag tag){
        int srcIdx = idToIndex(srcId);
        
        // If the process is white, 
        // it turns red by invoking globalState()
        if(myColor == WHITE)    
            globalState();
        
        // Set closed[srcId] to true because 
        // there cannot be any message of type 'White-Red'
        // in that channel after the marker is received
        closed[srcIdx] = true;    
        
        //determines whether the process has recorded its local state and all incoming channels 
        if (isDone()){  
            StringBuilder logger = new StringBuilder();
            logger.append(String.format("[Node %d] Channel State : In-Transit Messages\n", myId));
            for (int i = 0; i < numProc; i++){
                logger.append(
                        String.format("Channel %d: ", 
                                linker.getNeighbors().get(i).getNodeId()));
                while (!channels.get(i).isEmpty()){ 
                    logger.append(channels.get(i).remove(0).toString());
                }
                logger.append("\n");
            }
            System.out.println(logger.toString());
        } 
    }
    
    //the method isDone() determines whether the process has recorded its local state and all incoming channels 
    private boolean isDone(){
        if(myColor == WHITE)
            return false;
        for(int i = 0; i < numProc; i++){
            // If there is one channel not closed(closed[i] == true),
            // it means not finished, should return false
            if(!closed[i]) 
                return false;
        }
        return true;
    }
    
    
    /**
     * The method globalState turns the process red, 
     * records the local state, and sends the Marker
     * message on all outgoing channels
     */
    @Override
    public synchronized void globalState() {
        myColor = RED;
        localState();     // Record local State;
        try {
            sendToNeighbors(Tag.MARKER, "ChandyLamport");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    /**
     * This method take a snapshot of local process.
     * That is, the vector clock.
     */
    @Override
    public void localState() {
        System.out.println(String.format("[Node %d] %s", myId, vClock.toString()));
    }
    
    private int idToIndex(int nodeId){
        return Collections.binarySearch(linker.getNeighbors(), new Node(nodeId));
    }
    
}

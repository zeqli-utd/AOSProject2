package snapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import aos.MAP;
import aos.Message;
import aos.Node;
import aos.Tag;
import clock.VectorClock;
import helpers.PropConst;
import socket.Linker;

//Pj is a neighbor of Pi if there is a channel from Pi to Pj

public class RecvCamera extends MAP implements Camera, CamUser {
    private static final int WHITE = 0;                // Initial State
    private static final int RED = 1;                  // Snapshotted
    private static final int SET_ACTIVE = 10;
    private static final int SET_CHANNEL_EMPTY = 1;
    
    private int myColor = WHITE;
    
    
    private boolean closed[];   // closed[k] stop to recording messages along kth incomming channel
    private List<List<Message>> channels = null; //channels[k] records the state of the kth incoming channel
    private boolean channelState = CHANNEL_EMPTY;
    
    public static final boolean CHANNEL_EMPTY = true;
    public static final boolean CHANNEL_NON_EMPTY = false;
    public final int SNAP_SHOT_DELAY;
    
    /**
     * RecvCamera initialize the variables of the algorithm
     * All channels are initialized to empty
     * 
     * @param initLinker
     * @param globalParams
     */
    public RecvCamera(Linker initLinker) {
        super(initLinker);
        this.SNAP_SHOT_DELAY = Integer.parseInt(prop.getProperty(PropConst.SNAP_SHOT_DELAY));
        reset();
    }
    
    /**
     * Reset channels and process color
     */
    public void reset(){ 
        this.myColor = WHITE;
        this.closed = new boolean[numProc];  
        this.channels = new ArrayList<>(numProc);  
        
        // for each neighboring process Pk, closed[k] is initialized to false.
        for(int i = 0; i < numProc; i++){          
            channels.add(new ArrayList<>());
            closed[i] = false;
        } 
        channelState = CHANNEL_EMPTY;
    }

    
    
    @Override
    // the method handleMessage gives the rule for receiving a marker message
    public synchronized void handleMessage(Message msg, int srcId, Tag tag) throws IOException {  
        int srcIdx = idToIndex(srcId);
        switch (tag){
            case MARKER:
                //System.out.println(String.format("[Node %d] Received Marker %s", myId, msg.toString()));
                handleMarkerMessage(msg, srcId, tag);
                break;
            case APP:
                // Handle application message, 
                // true if the application message is of type 'White-Red'
                if (myColor == RED && !closed[srcIdx]){
                    // If the application message is of type 'White-Red'
                    // then add the message to channels
                    channels.get(srcIdx).add(msg);     
                    channelState = CHANNEL_NON_EMPTY;
                }
                // intentionally no breaking, pass down application message.
            default:
                super.handleMessage(msg, srcId, tag);
        }
    }
    
    /**
     * Handle marker messages
     * @param msg 
     * @param srcId
     * @param tag
     * @throws IOException 
     */
    private synchronized void handleMarkerMessage(Message msg, int srcId, Tag tag) throws IOException{
        int srcIdx = idToIndex(srcId);
        
        // If the process is white, 
        // it turns red by invoking globalState()
        if(myColor == WHITE) {   
            try {
                globalState();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        
        
        if (closed[srcIdx] == true){
            System.out.println(String.format("[Node %d] [Snapshot] Warning!!! Duplicate Marker %s\n", myId, msg.toString()));
        }
        
        // Set closed[srcId] to true because 
        // there cannot be any message of type 'White-Red'
        // in that channel after the marker is received
        closed[srcIdx] = true;    
        
        //determines whether the process has recorded its local state and all incoming channels 
        if (isDone()){  
            
            
            StringBuilder logger = new StringBuilder();
            logger.append(String.format("[Node %d] Channel State : In-Transit Messages\n", myId));
            for (int i = 0; i < numProc; i++){
                logger.append(String.format("Channel %d: ", linker.getNeighbors().get(i).getNodeId()));
                while (!channels.get(i).isEmpty()){ 
                    logger.append(channels.get(i).remove(0).toString());
                }
                logger.append("\n");
            }
            System.out.println(logger.toString());
            terminateRecording();
        } 
    }
    
    /**
     * Record channel state
     * Allow collecting channel state
     */
    private void terminateRecording(){
        
        // When recording is done. Collect channel state
        // Cannot determined here
        if (channelState == CHANNEL_EMPTY)
            snapshotForMap[myId] |= SET_CHANNEL_EMPTY;
        
        // Add Map protocol snapshot to snapshot list.
        snapshotList.add(snapshotForMap);
        
        
        // Release semaphore to allow SpanTree to collect the snapshot.
        available.release();             
        
        System.out.println(String.format("[Node %d] [Snapshot] *** SNAPSHOT TAKEN *** " +
                "MAP Snapshot = %s Vector snapshot = %s\n", 
                myId, Arrays.toString(snapshotForMap), Arrays.toString(snapshotForVector)
        ));
        
        // Prepare to next snapshot
        reset();
    }
    
    /**
     * Determine whether the protocol is finished
     * @return False when at least one channel is not closed or myColor is white
     */
    private boolean isDone(){
        if(myColor == WHITE)
            return false;
        for(int i = 0; i < numProc; i++){ 
            if(!closed[i]) 
                return false;
        }
        return true;
    }
    
    /**
     * Control snapshot and collect action in order.
     * @throws InterruptedException
     */
    public void waitForSnapshotDone() throws InterruptedException{
        available.acquire();
    }
    
    
    /**
     * The method globalState turns the process red, 
     * records the local state, and sends the Marker
     * message on all outgoing channels
     * @throws IOException 
     * @throws InterruptedException 
     */
    @Override
    public synchronized void globalState() throws IOException, InterruptedException {
        mutex.acquire();
        myColor = RED;
        localState();     // Record local State;
        sendToNeighbors(Tag.MARKER, "ChandyLamport");
        mutex.release();
    }    

    /**
     * This method take a snapshot of local process.
     * That is, the vector clock.
     */
    @Override
    public synchronized void localState() {
        // Vector Timestamp Snapshot
        VectorClock snapshotForVectorCopy = new VectorClock(vClock);
        snapshotForVector = snapshotForVectorCopy.getVector();
        
        // Snapshot for termination detection
        int topologySize = Integer.parseInt(prop.getProperty(PropConst.NUM_NODES));
        snapshotForMap = new int[topologySize];
        
        //  00 - PASSIVE, NON-EMPTY
        //  01 - PASSIVE, EMPTY
        //  10 - ACTIVE , NON-EMPTY
        //  11 - ACTIVE , EMPTY
        if (state == MAP.ACTIVE)
            snapshotForMap[myId] |= SET_ACTIVE;
		String configurationFilename = prop.getProperty(PropConst.CONFIG_FILE_NAME).replaceAll(".txt", "");
        writeToFile(String.format("%s-%d.out", configurationFilename, myId), snapshotForVectorCopy.formatToOutput());
    }
    
    private void writeToFile(String address, String state){
    	File f = new File(address);
		try{
			FileWriter out = new FileWriter(f,true);
			out.write(state);
			out.write("\r\n");
			out.close();
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
    }
    
    /**
     * Helper function convert node id to channel index.
     * @param nodeId
     * @return
     */
    private int idToIndex(int nodeId){
        return Collections.binarySearch(linker.getNeighbors(), new Node(nodeId));
    } 
    
}

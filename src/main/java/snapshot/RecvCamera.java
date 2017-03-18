package snapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import aos.PropertyType;
import aos.MAP;
import aos.Message;
import aos.Node;
import aos.Tag;
import helpers.Linker;
import helpers.RKey;

//Pj is a neighbor of Pi if there is a channel from Pi to Pj

public class RecvCamera extends MAP implements Camera, CamUser {
    private static final int WHITE = 0;                // Initial State
    private static final int RED = 1;                  // Snapshotted
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
    public RecvCamera(Linker initLinker, Map<PropertyType, Integer> globalParams) {
        super(initLinker, globalParams);
        this.SNAP_SHOT_DELAY = globalParams.get(PropertyType.SNAP_SHOT_DELAY);
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
    public void handleMessage(Message msg, int srcId, Tag tag) throws IOException {  
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
     * @throws IOException 
     */
    private synchronized void handleMarkerMessage(Message msg, int srcId, Tag tag) throws IOException{
        int srcIdx = idToIndex(srcId);
        
        // If the process is white, 
        // it turns red by invoking globalState()
        if(myColor == WHITE)    
            globalState();
        
        
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
                logger.append(
                        String.format("Channel %d: ", 
                                linker.getNeighbors().get(i).getNodeId()));
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
            snapshotForMap[myId] += 1;
        
        
        snapshotList.add(snapshotForMap);
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
     */
    @Override
    public synchronized void globalState() throws IOException {
        myColor = RED;
        localState();     // Record local State;
        sendToNeighbors(Tag.MARKER, "ChandyLamport");
    }    

    /**
     * This method take a snapshot of local process.
     * That is, the vector clock.
     */
    @Override
    public synchronized void localState() {
        // Vector Timestamp Snapshot
        snapshotForVector = vClock.getVector();
        
        // Snapshot for termination detection
        snapshotForMap = new int[vClock.getTopologySize() + 1];
        boolean mapState = state;
        
        //  00 - PASSIVE, NON-EMPTY
        //  01 - PASSIVE, EMPTY
        //  10 - ACTIVE , NON-EMPTY
        //  11 - ACTIVE , EMPTY
        if (mapState == MAP.ACTIVE)
            snapshotForMap[myId] += 10;
		String configurationFilename = ((String) registry.getObject(RKey.KEY_CONFIG_FILE_NAME.name())).replaceAll(".txt", "");
        writeToFile(String.format("%s-%d.out", configurationFilename, myId), vClock.formatToOutput());
    }
    
    private int idToIndex(int nodeId){
        return Collections.binarySearch(linker.getNeighbors(), new Node(nodeId));
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
    
}

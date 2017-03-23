package aos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import clock.VectorClock;
import socket.Linker;


/**
 * Spanning Tree Builder
 * @author zeqing
 *
 */
public class SpanTree extends Process {
    
    /**
     * Parent node id, -1 if no parent assigned
     */
    private int parent = -1;    // No parent yet
    
    /**
     * Children list
     */
    private List<Integer> children;
    
    /**
     * Message received from neighbors
     */
    private int numReports = 0;         
    
    /**
     * Indicate whether tree constructed or not
     */
    private boolean isTreeConstructed = false;
    
    /**
     * List of children that not yet send converge cast message
     */
    private ArrayList<Integer> pending = new ArrayList<>();
    
    /**
     * Control variable to indicate whether 
     * or not a node has been set up a list of listening children
     * 
     */
    private boolean pendingSet = false;
    
    /**
     * Control variable to invoke computeGlobal method
     * True if a node received a TREE_BROADCAST message
     * 
     */
    private boolean isAwake = false;
    
    public SpanTree(Linker initLinker){
        super(initLinker);
        this.children = new ArrayList<>();
        reset();
        boolean isRoot = (myId == 0);
        if (isRoot){
            parent = initLinker.getMyId();
            if(initLinker.getNeighbors().size() == 0){
                isTreeConstructed = true;
            } else {
                buildSpanTree();
            }
        }
    }
    
    private void reset() {
        this.isAwake = false;
        this.pendingSet = false;
    }
    
    /**
     * Once the class is instantiated and is root node.
     * Broadcast invitation to neighbors 
     */
    private void buildSpanTree(){
        try {
            sendToNeighbors(Tag.TREE_INVITE, "Invite");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Collect all local state.
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    public synchronized void computeGlobal() throws IOException, InterruptedException{
        String logInfo;
        
        // 1. Root trigger the system global computation event
        // 2. Children waiting for TREE_BROADCAST starting signal. 
        if (parent == myId){ // Root node
            sendChildren(Tag.TREE_BROADCAST, "Broadcast");
        } else {
            while (!isAwake){
                procWait(); // Thread awake by parent's broadcast message 
            }
            System.out.println(String.format("[Node %d] [Tree] Waked up success", myId));
        }
        // Check SnapshotListSize
        logInfo = String.format("[Node %d] [Tree] %d Snapshot Taken, Collecting Snapshot-%d... \n", myId, snapshotList.size(), snapshotIndex);
        System.out.println(logInfo);
        
        // 3. Setup snapshot status
        pending = new ArrayList<>();
        pending.addAll(children);           
        pendingSet = true;
        
       
        notifyAll();            // Notify handleConvergeCast
    
        
        while (!pending.isEmpty()){
            procWait();         // Wait for children jobs finished. 
        }
       
        if (parent == myId){    // Root node
            logInfo = String.format("[Node %d] [Tree] ***** SNAPSHOT-%d %s *****\n", myId, snapshotIndex, Arrays.toString(snapshotList.get(snapshotIndex)));
            System.out.println(logInfo);
            
            checkGlobalMapProtocolStates();
        } else {  // Non-root node
            logInfo = String.format("[Node %d] [Tree] Children Done. Cast to Parent(%d) %s\n", myId, parent, Arrays.toString(snapshotForMap));
            System.out.println(logInfo);
                        
            Message message = new Message(myId, parent, Tag.TREE_CONVERGE, "Snapshot");
            message.setMapState(snapshotList.get(snapshotIndex));
            sendMessage(parent, message);
        }   
        snapshotIndex++;
        
        // Reset
        reset();
    }
    
    /**
     * When all nodes are passive and all channels are empty.
     * Do not grant permission.
     */
    private void checkGlobalMapProtocolStates() {
        String logInfo;
        int[] globalMapState = snapshotList.get(snapshotIndex);
        int terminationCount = 0;
        for (int i = 0; i < globalMapState.length; i++){
            if (globalMapState[i] == 1){
                logInfo = String.format("[Node %d] [SNAPSHOT] ++++++++++ Node %d Termination Detected ++++++++++", myId, i);
                System.out.println(logInfo);
                
                terminationCount++;
            }
        }
        if (terminationCount == vClock.getTopologySize()){
            if (snapshotPermission.availablePermits() <= 0){
                logInfo = String.format("[Node %d] [SNAPSHOT] ***** HALT *****", myId);
                System.out.println(logInfo);
            }
        } else {
            grantSnapshotPermisson();
        }
    }
    
    /**
     * Block till tree was constructed
     * 
     */
    public synchronized void waitForTreeConstruction () throws InterruptedException { 
        while (!isTreeConstructed){
            procWait();
        }
    }
    
    // Send to all children
    public synchronized void sendChildren(Tag tag, String content) throws IOException{
        for (int dstId : children) {
            sendMessage(dstId, tag, content);
        }
    }
    
    
    @Override
    public synchronized void handleMessage(Message msg, int srcId, Tag tag) throws IOException {
        switch (tag){
            case TREE_INVITE:
                handleInvitation(msg, srcId, tag);
                break;
            case TREE_ACCEPT:
            case TREE_REJECT:
                handleInvitationReponse(msg, srcId, tag);
                break;
            case TREE_CONVERGE:
                handleConvergeCast(msg, srcId, tag);
                break;
            case TREE_BROADCAST:
                handleBroadCast(msg, srcId, tag);
                break;
            default:
                super.handleMessage(msg, srcId, tag);
        }
    }
    

    /**
     * Handle message carrying tag TREE_CONVERGECAST
     */
    public synchronized void handleConvergeCast(Message msg, int srcId, Tag tag) throws IOException{
        while (!pendingSet) {
            procWait();
        }
        
        System.out.println(String.format("[Node %d] [Tree] Collect Snapshot from %d %s", 
                myId, msg.getSrcId(), Arrays.toString(msg.getMapState())));
        
        VectorClock.flatMerge(snapshotList.get(snapshotIndex), msg.getMapState());

        pending.remove(new Integer(srcId));
        if (pending.isEmpty()){
            notifyAll();  // Notify computeGlobal() cast message up
        }         
        
    }
    
    /**
     * Only received broadcast from parent
     */
    public synchronized void handleBroadCast(Message msg, int srcId, Tag tag) throws IOException{
        // Receive signal from parent, resume computeGlobal()
        isAwake = true;
        notifyAll();
        System.out.println(String.format("[Node %d] [Tree] Recv Broadcast from %d, wake up to collect snapshot", myId, msg.getSrcId()));
        
        // Non-root node
        if (parent != myId) {
            sendChildren(Tag.TREE_BROADCAST, "Broadcast");
        }
    }
    
    /**
     * Handle span tree construction Invitation message
     */
    private synchronized void handleInvitation(Message msg, int srcId, Tag tag) throws IOException{
     // If the parent reference not set yet.
        if (parent == -1) {
            numReports++;
            parent = srcId;
            sendMessage(srcId, Tag.TREE_ACCEPT, "Accept");

            for (Node neighbor : linker.getNeighbors()) {
                int dstId = neighbor.getNodeId();
                if (dstId != srcId) {
                    sendMessage(dstId, Tag.TREE_INVITE, "Invite");
                }
            }
        } else {
            // If the parent reference already set. Reject the request
            sendMessage(srcId, Tag.TREE_REJECT, "Reject");
        }
        if (numReports == numProc) {
            isTreeConstructed = true;
            System.out.println(String.format("[Node %d] [SpanTree Constructed %d/%d] Parent=%d Children=%s",
                    myId, numReports, numProc, parent, children.toString()));
            notify();    // Notify wait for done.
        }
    }
    
    /**
     * Handle Accept and Reject messages.
     */
    private synchronized void handleInvitationReponse(Message msg, int srcId, Tag tag) throws IOException{
        numReports++;
        if (tag.equals(Tag.TREE_ACCEPT))
            children.add(srcId);
        if (numReports == numProc) {
            isTreeConstructed = true;
            System.out.println(String.format("[Node %d] [SpanTree Constructed %d/%d] Parent=%d Children=%s",
                    myId, numReports, numProc, parent, children.toString()));
            notify();    // Notify wait for done.
        }
    }
}

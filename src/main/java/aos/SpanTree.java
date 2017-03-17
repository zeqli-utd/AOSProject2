package aos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import clock.VectorClock;
import helpers.Linker;


/**
 * Spanning Tree Builder
 * @author zeqing
 *
 */
public class SpanTree extends Process {
    
    private int parent = -1;    // No parent yet
    private ArrayList<Integer> children = new ArrayList<>();
    
    private int numReports = 0;        // Message received from neighbors
    private boolean done = false;
    
    
    
    // Broadcast Convergecast Controls
    private ArrayList<Integer> pending = new ArrayList<>();
    private boolean pendingSet = false;
    private boolean isAwake = false;
    private int[] snapshot;

    
    public SpanTree(Linker initLinker){
        super(initLinker);
        boolean isRoot = (myId == 0);
        if (isRoot){
            parent = initLinker.getMyId();
            if(initLinker.getNeighbors().size() == 0){
                done = true;
            } else {
                buildSpanTree();
            }
        }
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
    
    // Pseudo Compute Snapshot
    public synchronized void computeGlobal() throws IOException{
        
        // Parent node trigger the computeGlobal event
        if (parent == myId){ // Root node
            sendChildren(Tag.TREE_BROADCAST, "Broadcast");
            
            // TODO: isAwake will apply to parent as well
        } else {
            while (!isAwake){
                procWait(); 
            }
        }
        
        // Setup snapshot status
        pending = new ArrayList<>();
        pending.addAll(children);   
        
        // Retrieve a clone of vector
        snapshot = vClock.getVector();
        
        pendingSet = true;
        
        notifyAll(); // Notify handleConvergeCast
    
        
        while (!pending.isEmpty()){
            procWait();        // Wait for children 
        }
        
        // When handleConvergeCast finish up all children message handling.
        if( parent == myId){  // Root node
            System.out.println(String.format("[Node %d] [Result: %s]", myId, Arrays.toString(snapshot)));
        } else {  // Non-root node
            sendMessage(parent, Tag.TREE_CONVERGE, "", snapshot);
        }        
    }
    
    // Block till tree was constructed
    public synchronized void waitForDone () throws InterruptedException { 
        while (!done){
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
    

    
    public synchronized void handleConvergeCast(Message msg, int srcId, Tag tag) throws IOException{
        while (!pendingSet) {
            procWait();
        }
        
        if (!msg.containsVector()){
            throw new IOException("Missing Vector");
        }
        boolean succeed = VectorClock.flatMerge(snapshot, msg.getVector());
        if (!succeed){
            throw new IOException("Vector mismatch " + 
                    snapshot.toString() + " " + msg.getVector().toString());
        }
            
        
        pending.remove(new Integer(srcId));
        if (pending.isEmpty()){
            notifyAll();  // Notify computeGlobal() cast message up
        }         
        
    }
    
    // Only received broadcast from parent
    public synchronized void handleBroadCast(Message msg, int srcId, Tag tag) throws IOException{
        // Receive signal from parent, resume computeGlobal()
        isAwake = true;
        notifyAll();
        
        // Non-root node
        if (parent != myId) {
            sendChildren(Tag.TREE_BROADCAST, "Broadcast");
        }
    }
    
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

        //System.out.println(String.format("[Node %d] [Invitation: %d/%d] %s", myId, numReports, numProc, msg.toString()));
    }
    
 // Handle Accept and Reject messages.
    private synchronized void handleInvitationReponse(Message msg, int srcId, Tag tag) throws IOException{
        numReports++;
        if (tag.equals(Tag.TREE_ACCEPT))
            children.add(srcId);
        if (numReports == numProc) {
            done = true;
            System.out.println(String.format("[Node %d] "
                    + "[SpanTree Constructed %d/%d] "
                    + "Parent=%d Children=%s", 
                    myId, numReports, numProc, parent, children.toString()));
            notify();    // Notify wait for done.
        }

        //System.out.println(String.format("[Node %d] [Invite.Response %d/%d] %s", myId, numReports, numProc, msg.toString()));
    }
}

package aos;

import java.io.IOException;
import java.util.ArrayList;


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
    private int numChildren = -1;
    private ArrayList<Integer> pending = new ArrayList<>();
    
    // Testing only
    private boolean pendingSet = false;
    private boolean answerRecved;
    private int answer;
    
    public SpanTree(Linker initLinker, boolean isRoot){
        super(initLinker);
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
    
    public int computeGlobal() throws IOException{
        pending = new ArrayList<>();
        pending.addAll(children);
        pendingSet = true;
        
        notifyAll();
        
        while (!pending.isEmpty()){
            procWait();
        }
        
        if( parent == myId){  // Root node
            answer = myId;
        } else {  // Non-root node
            sendMessage(parent, Tag.TREE_CONVERGE, Integer.toString(myId));
            answerRecved = false;
            while (!answerRecved){
                procWait();
            }
        }
        
        return 0;
        
    }
    
    // block till children known
    public synchronized void waitForDone () throws InterruptedException { 
        while (!done){
            procWait();
        }
    }
    
    public void sendChildren(Tag tag, String content) throws IOException{
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
        numReports++;
        if (numReports == numChildren){
            if (parent == myId){
                // Compute global function
            } else {
                sendMessage(parent, Tag.TREE_CONVERGE, "Converge");
            }
        }
    }
    
    public synchronized void handleBroadCast(Message msg, int srcId, Tag tag) throws IOException{
        // Non-root node
        if (parent != myId) {
            for (int dstId : children) {
                sendMessage(dstId, Tag.TREE_BROADCAST, "Invite");
            }
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
            numChildren = children.size();
            System.out.println(String.format("[Node %d] [SpanTree Constructed %d/%d] Parent=%d Children=%s", myId, numReports, numProc, parent, children.toString()));
            notify();
        }

        //System.out.println(String.format("[Node %d] [Invite.Response %d/%d] %s", myId, numReports, numProc, msg.toString()));
    }
}

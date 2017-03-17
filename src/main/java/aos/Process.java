package aos;

import java.io.IOException;
import java.util.List;

import clock.VectorClock;
import helpers.Linker;

public class Process implements MessageHandler{
    protected int numProc, myId;
    protected Linker linker;
    protected VectorClock vClock;         // Vector Clock
    
    public Process(Linker initLinker){
        this.linker = initLinker;
        this.myId = linker.getMyId();
        this.numProc = linker.getNeighbors().size();   
    }
    
    /**
     * Default message handler.
     * Accept and process application message only 
     * 
     * Only one thread can handle message at the same time.
     * 
     * @throws IOException 
     */
    public synchronized void handleMessage(Message msg, int srcId, Tag tag) throws IOException{
        if(tag == Tag.APP) System.out.println("This is application message");
        else System.out.println(String.format("[Node %d] [Request] content=%s", myId, msg.toString()));
    }
    
    /**
     * Send message to a specific node
     * @param dstId
     * @param tag
     * @param content
     * @throws IOException
     */
    public void sendMessage(int dstId, Tag tag, String content) throws IOException{
        linker.sendMessage(dstId, tag, content);
    }
    
    public void sendMessage(int dstId, Tag tag, String content, int[] vector) throws IOException{
        linker.sendMessage(dstId, tag, content, vector);
    }
    
    /**
     * Broadcast messages to all its neighbors
     * @param tag
     * @param content
     * @throws IOException
     */
    public void sendToNeighbors(Tag tag, String content) throws IOException{
        List<Node> neighbors = linker.getNeighbors();
        linker.multicast(neighbors, tag, content);
    }
    
    /**
     * Retrieve message for a specific node
     * @throws IOException 
     */
    public Message receiveMessage(int fromId) throws IOException{
        try{
            return linker.receiveMessage(fromId);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        } catch (IOException e){
            linker.close();
            throw e;
        } 
    }
    
    public synchronized void procWait(){
        try{
            wait();
        } catch (InterruptedException e){
            System.err.println(e);
        }
    }
    
    public synchronized void setVectorClock(VectorClock v) {
        this.vClock = v;
    }
    
    /**
     * String representation of process
     */
    public synchronized String toString(){
        StringBuilder sb = new StringBuilder();
        String newline = "\n";
        String meta = String.format("[Node Id = %d]", myId);
        sb.append(meta).append(newline);
        if(numProc != 0){
            String neighborInfo = String.format("Neighbor Info: \nnumProc: %d", numProc);
            sb.append(neighborInfo).append(newline);
            List<Node> neighbors = linker.getNeighbors();
            for(Node node : neighbors){
                sb.append(node.toString()).append(newline);
            }
        }
        return sb.toString();
    }
}

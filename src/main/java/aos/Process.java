package aos;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

public class Process implements MessageHandler{
    protected int numProc, myId;
    protected Linker linker;
    
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
        System.out.println(String.format("[Node %d] [Request] content=%s", myId, msg.toString()));
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
}

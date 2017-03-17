package helpers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import aos.Message;
import aos.Node;
import aos.Tag;
import clock.VectorClock;

/**
 * A coordinator for manage  
 * @author zeqing
 *
 */
public class Linker {
    private ObjectOutputStream[] out;
    private ObjectInputStream[] in;
    private int myId;               // Local node id
    private int numProc;            // Number of processes it contact with
    private Connector connector;    
    private List<Node> neighbors;
    
    public Linker(int myId, List<Node> neighbors){
        this.myId = myId;
        this.numProc = neighbors.size();
        this.neighbors = neighbors;
        
        this.out = new ObjectOutputStream[numProc];
        this.in = new ObjectInputStream[numProc];
        
        this.connector = Connector.getInstance();
    }
    
    /**
     * Build bidirectional channels with all neighbors
     * @param listenPort
     * @throws Exception
     */
    public void buildChannels(int listenPort) throws Exception{
        connector.connect(listenPort, myId, in, out, neighbors);
    }
    
    /**
     * Send one message to a destination neighbor
     * 
     * @param dstId Destination id
     * @param tag Message type
     * @param content Message body
     * @throws IOException 
     */
    public synchronized void sendMessage(int dstId, Tag tag, String content) throws IOException{
        sendMessage(dstId, tag, content, new int[0]);
    }
    
    public synchronized void sendMessage(int dstId, Tag tag, String content, int[] vector) throws IOException{
        int dstIndex = idToIndex(dstId);
        Message appMessage = new Message(myId, dstId, tag, content);
        appMessage.setVector(vector);
        out[dstIndex].writeObject(appMessage);
    }
    
    /**
     * Multicast to a group of destinations
     * 
     * @param members The multicast group member
     * @param tag Message type
     * @param content Message body
     * @throws IOException
     */
    public void multicast(List<Node> members, Tag tag, String content) throws IOException{
        for(Node member : members){
            sendMessage(member.getNodeId(), tag, content);
        }
    }
        
    /**
     * Listen to a particular neighbor
     * 
     * @param fromId
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Message receiveMessage(int fromId) throws IOException, ClassNotFoundException {
        int fromIndex = idToIndex(fromId);
        Message msg = (Message)in[fromIndex].readObject();   // This will block if no message.
        return msg;
    }
    
    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Node> neighbors) {
        this.neighbors = neighbors;
    }
    
    private int idToIndex(int nodeId){
        return Collections.binarySearch(neighbors, new Node(nodeId));
    }

    public void close(){
        connector.closeSockets();
    }
    
    public int getNumProc(){
    	return this.numProc;
    }
}

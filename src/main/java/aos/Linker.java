package aos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

/**
 * A coordinator for manage  
 * @author zeqing
 *
 */
public class Linker {
    private ObjectOutputStream[] out;
    private ObjectInputStream[] in;
    private int myId;
    private int numProc;
    private Connector connector;
    private List<Node> neighbors;
    
    public Linker(int myId, List<Node> neighbors){
        this.myId = myId;
        this.numProc = neighbors.size();
        this.out = new ObjectOutputStream[numProc];
        this.in = new ObjectInputStream[numProc];
        this.connector = Connector.getInstance();
        this.neighbors = neighbors;
    }
    
    public void buildChannels(int listenPort) throws Exception{
        connector.connect(listenPort, myId, in, out, neighbors);
    }
    
    public void sendMessage(int dstId, Tag tag, String content) throws IOException{
        int dstIndex = Collections.binarySearch(neighbors, new Node(dstId));
        out[dstIndex].writeObject(new Message(myId, dstId, tag, content));
    }
    
    public void multicast(List<Node> destinations, Tag tag, String content) throws IOException{
        for(Node process : destinations){
            sendMessage(process.getNodeId(), tag, content);
        }
    }
        
    public Message receiveMessage(int fromId) throws IOException, ClassNotFoundException {
        int fromIndex = Collections.binarySearch(neighbors, new Node(fromId));
        Message msg = (Message)in[fromIndex].readObject();                       // If no message, then blocking
        System.out.println(String.format("[Node %d], recv, content=%s", myId, msg.toString()));
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

    public void close(){
        connector.closeSockets();
    }
}

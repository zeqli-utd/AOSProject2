package aos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		this.connector = new Connector();
	}
	
	public void buildChannels(int listenPort) throws Exception{
		connector.connect(listenPort, myId, in, out, neighbors);
	}
	
	public void sendMessage(int dstId, Tag tag, String content) throws IOException{
		out[dstId].writeObject(new Message(myId, dstId, tag, content));
	}
	
	public void multicast(List<Node> destinations, Tag tag, String content) throws IOException{
		for(Node process : destinations){
			sendMessage(process.getNodeId(), tag, content);
		}
	}
		
	public Message receiveMessage(int fromId) throws IOException, ClassNotFoundException {
		Message msg = (Message)in[fromId].readObject();
		System.out.println(" Received message " + msg.toString());
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

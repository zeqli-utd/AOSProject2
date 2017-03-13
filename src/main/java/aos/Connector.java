package aos;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * A collection of accept sockets for maintaining channels
 * @author zeqing
 *
 */
public class Connector {
	ServerSocket listener;
	Socket[] link;
	
	
	public Connector() {
	}


	public void connect(int listenPort, int myId, ObjectInputStream[] in, ObjectOutputStream[] out, List<Node> neighbors) throws Exception{
		int numProc = neighbors.size();
		link = new Socket[numProc];
		listener = new ServerSocket(listenPort);
		
		/* Accept connections from all the smaller processes */
		int i = 0;
		while(i < neighbors.size() && neighbors.get(i).getNodeId() < myId){
			Socket socket = listener.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            
            Message msg = (Message)ois.readObject();
            int fromId = msg.getSrcId();
            if(msg.getTag().equals(Tag.HANDSHAKE)){
            	link[fromId] = socket;  
            	in[fromId] = ois;
            	out[fromId] = new ObjectOutputStream(socket.getOutputStream());
                i++;
            }
		}
		
		/* Contact all the bigger process*/
		while(i < neighbors.size()){
			Node process = neighbors.get(i);
			link[i] = new Socket(process.getHostName(), process.getPort());
			out[i] = new ObjectOutputStream(link[i].getOutputStream());
			in[i] = new ObjectInputStream(link[i].getInputStream());
			
			/* Send a handshake message to P_i */
			out[i].writeObject(new Message(myId, process.getNodeId(), Tag.HANDSHAKE, ""));
            i++;
		}
		
	}
	
	
	/**
	 * Close all connection to this node.
	 */
	public void closeSockets(){
		try{
			listener.close();
			for(int i = 0; i < link.length; i++){
				link[i].close();
			}
		} catch(Exception e){
			System.err.println(e);
		}
	}
}

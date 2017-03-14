package aos;

import java.io.IOException;

public class Process implements MessageHandler{
	protected int numProc, myId;
	protected Linker linker;
	
	public Process(Linker initLinker){
		this.linker = initLinker;
		this.myId = linker.getMyId();
		this.numProc = linker.getNeighbors().size();
	}
	
	/**
	 * Handle message from a specific node.
	 * @throws IOException 
	 */
	public synchronized void handleMessage(Message m, int srcId, Tag tag) throws IOException{
		// TODO:  
		switch(tag){
			case VECTOR:
			case SETUP:
			case MARKER:
			case APP:
			case HANDSHAKE:
			case TREE_INVITE:
			default:
			break;
		}
//		VECTOR,

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
		for(int i = 0; i < numProc; i++){
			sendMessage(i, tag, content);
		}
	}
	
	/**
	 * Retrieve message for a specific node
	 */
	public Message receiveMessage(int fromId) throws IOException{
		try{
			return linker.receiveMessage(fromId);
		} catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
			System.err.println(e);
			linker.close();
			return null;
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

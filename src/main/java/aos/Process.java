package aos;

import java.io.IOException;

public class Process implements MessageHandler{
	protected int numProc, id;
	protected Linker linker;
	
	public Process(Linker initLinker){
		this.linker = initLinker;
		this.id = linker.getMyId();
		this.numProc = linker.getNeighbors().size();
	}
	public synchronized void handleMessage(Message m, int srcId, Tag tag){}
	
	public void sendMessage(int dstId, Tag tag, String content) throws IOException{
		linker.sendMessage(dstId, tag, content);
	}
	
	public void sendToNeighbors(Tag tag, String content) throws IOException{
		for(int i = 0; i < numProc; i++){
			sendMessage(i, tag, content);
		}
	}
	
	
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

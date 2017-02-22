package snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aos.Linker;
import aos.Message;
import aos.Process;
import aos.Tag;

public class RecvCamera extends Process implements Camera {
	private static final int WHITE = 0;
	private static final int RED = 1;
	private int myColor = WHITE;
	private boolean closed[];
	private List<List<Message>> channels = null;
	private CamUser app;
	
	public RecvCamera(Linker initLinker, CamUser app) {
		super(initLinker);
		closed = new boolean[numProc];
		channels = new ArrayList<>(numProc);
		for(int i = 0; i < numProc; i++){
			channels.add(new ArrayList<>());
			closed[i] = false;
		}
		this.app = app;
	}

	@Override
	public synchronized void globalState() {
		// TODO Auto-generated method stub
		myColor = RED;
		app.localState();     // Record local State;
		try {
			sendToNeighbors(Tag.MARKER, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	
	@Override
	public void handleMessage(Message m, int srcId, Tag tag) {
		if(tag.equals(Tag.MARKER)){
			if(myColor == WHITE) 
				globalState();
			closed[srcId] = true;
			if(isDone()){
				System.out.println("Channel State : Transit Messages");
				for(int i = 0; i < numProc; i++){
					while(!channels.get(i).isEmpty()){
						System.out.println(channels.get(i).remove(0).toString());
					}
				}
			} else { // application message
				if(myColor == RED && !closed[srcId])
					channels.get(srcId).add(m);
				app.handleMessage(m, srcId, tag); // give it to app
			}
		}

	}
	
	private boolean isDone(){
		if(myColor == WHITE)
			return false;
		for(int i = 0; i < numProc; i++){
			if(!closed[i])
				return false;
		}
		return true;
	}

}

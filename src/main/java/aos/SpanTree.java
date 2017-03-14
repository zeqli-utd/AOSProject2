package aos;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Spanning Tree Builder
 * @author zeqing
 *
 */
public class SpanTree extends Process {
	public int parent = -1;    // No parent yet
	public ArrayList<Integer> children = new ArrayList<>();
	int numReports = 0;        // Message received from neighbors
	boolean done = false;
	
	public SpanTree(Linker initLinker, boolean isRoot){
		super(initLinker);
		if (isRoot){
			parent = initLinker.getMyId();
			if(initLinker.getNeighbors().size() == 0){
				done = true;
			} else {
				try {
					sendToNeighbors(Tag.TREE_INVITE, "Invite");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// block till children known
	public synchronized void waitForDone () throws InterruptedException { 
		while (!done){
			wait();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized void handleMessage(Message m, int srcId, Tag tag) throws IOException{
		if (tag.equals(Tag.TREE_INVITE)){
			if (parent == -1) {
				numReports++;
				parent = srcId;
				sendMessage(srcId, Tag.TREE_ACCEPT, "Accept");
				
				for (Node neighbor: linker.getNeighbors()){
					int dstId = neighbor.getNodeId();
					if ( dstId != srcId){
						sendMessage(dstId, Tag.TREE_INVITE, "Invite");
					}
				}
			} else if (tag.equals(Tag.TREE_ACCEPT) | tag.equals(Tag.TREE_REJECT)){
				if (tag.equals(Tag.TREE_ACCEPT))
					children.add(srcId);
				numReports++;
				if (numReports == linker.getNeighbors().size()) {
					done = true;
					notify();
				}
			}
		}
	}
	
	
}

package clock;


import java.io.IOException;
import java.util.List;

import aos.Message;
import aos.Node;
import aos.Tag;
import helpers.Linker;

public class VectorClockLinker extends Linker {
    private VectorClock vc;
    
    public VectorClockLinker(int id, List<Node> neighbors) throws Exception {
        super(id, neighbors);
        this.vc = new VectorClock(neighbors.size(), id);
    }
    
    public void sendMessage(int dstId, Tag tag, String content) throws IOException{
        super.sendMessage(dstId, tag, content);
        super.sendMessage(dstId, tag, content);
        vc.sendAction();
    }
    
    public void simpleSendMessage(int dstId, Tag tag, String content) throws IOException{
        super.sendMessage(dstId, tag, content);
    }
    
    public Message receiveMessage(int fromId) throws IOException, ClassNotFoundException{
        Message msg = super.receiveMessage(fromId);
        if(msg.getTag().equals(Tag.VECTOR)){
            int[] receiveTag = msg.getVector();
            vc.receiveAction(receiveTag);
            return super.receiveMessage(fromId); // app message
        } else {
            return msg;
        }
    }
    
}

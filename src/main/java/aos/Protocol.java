package aos;

import java.util.List;
import java.util.Map;

/**
 * Abstract base class for implementations of protocol.
 * 
 * @author zeqing
 *
 */
public abstract class Protocol {
    protected String name; 
    protected int myId;
    protected int numProc;
    protected Map<GlobalParams, Integer> globalParams;
    protected List<Node> neighbors;
    
    protected Protocol(int myId, String name){
        this.myId = myId;
        this.name = name;
    }
    
    public int getMyId(){
        return myId;
    }
    
    public int getNumProc() {
        return numProc;
    }

    public void setNeighbors(List<Node> neighbors) {
        this.neighbors = neighbors;
        this.numProc = neighbors.size();
    }
    
    public List<Node> getNeighbors() {
        return neighbors;
    }

    public void setGlobalParams(Map<GlobalParams, Integer> globalParams) {
        this.globalParams = globalParams;
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        String newline = "\n";
        String meta = String.format("id: %d, name: %s", myId, name);
        sb.append(meta).append(newline);
        if(numProc != 0){
            String neighborInfo = String.format("Neighbor Info: \nnumProc: %d", numProc);
            sb.append(neighborInfo).append(newline);
            List<Node> neighbors = getNeighbors();
            for(Node node : neighbors){
                sb.append(node.toString()).append(newline);
            }
        }
        return sb.toString();
    }
}

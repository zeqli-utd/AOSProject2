package clock;

import java.util.Arrays;

public class VectorClock {
    private int[] v;
    private int id;
    private int topologySize;
    
    /**
     * Create a new Vector Clock instance.
     * @param numProc Topology Size
     * @param id
     */
    public VectorClock(int tSize, int id){
        this.id = id;
        this.topologySize = tSize;
        this.v = new int[topologySize + 1];
        v[id] = 1;
    }
    
    /**
     * A copy constructor
     * @param orig Original VectorClock Object
     */
    public VectorClock(VectorClock orig){
        this(orig.getTopologySize(), orig.getId());
        setVector(orig.getVector());  // Deep copy array
    }
    
    /**
     * Increase current node clock by 1
     */
    public void tick(){
        v[id]++;
    }
    
    public void sendAction(){
        // include the vector in the message
        v[id]++;
    }
    
    public void receiveAction(int[] sentValue){
        for(int i = 0; i <= topologySize; i++){
            v[i] = Math.max(v[i], sentValue[i]);
        }
        v[id]++;
    }
    
    /**
     * Merge two vectors, result vector is stored in vector1
     * 
     * @param vector1
     * @param vector2
     * @return True if merge success, otherwise False
     */
    public static boolean flatMerge(int[] vector1, int[] vector2){
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) 
            return false;
       
        for (int i = 0; i < vector1.length; i++){
            vector1[i] = Math.max(vector1[i], vector2[i]);
        }
        return true;
    }
    
    /**
     * Compare two vector clock
     * 
     * @param other
     * @return
     */
    public boolean happensBefore(VectorClock other){
        if (other == null || v.length != other.getVector().length)
            return false;
        
        // e -> f if and only if
        // (i = j) AND (C(e)[i] < C(f)[i]) OR (i != j) AND (C(e)[i] ¡Ü C(f)[i])
        if (id == other.getId()) {
            if (getValue(id) < other.getValue(id))
                return true;
        } else { // id != other.getId()
            if (getValue(id) <= other.getValue(id))
                return true;
        }
        return false;
    }
    
    public int getId(){
        return id;
    }
    
    public int getValue(int i){
        return v[i];
    }
    
    public void setVector(int[] newV) {
        this.v = newV.clone();
    }
    
    public int[] getVector(){
        return v.clone();
    }
    
    public int getTopologySize() {
        return topologySize;
    }

    @Override
    public String toString(){
        return String.format("ProcessId = %d, Vector %s", id, Arrays.toString(v));
    }
    
    public String formatToOutput(){
        int[] out = Arrays.copyOf(v, v.length - 1);
        StringBuilder sb = new StringBuilder();
        for (int i : out){
            sb.append(i).append(" ");
        }
        return sb.toString().trim();
    }
}

package clock;

import java.util.Arrays;

public class VectorClock {
    private int[] v;
    private int id;
    private int nProc;
    
    /**
     * Create a new Vector Clock instance.
     * @param numProc Topology Size
     * @param id
     */
    public VectorClock(int numProc, int id){
        this.id = id;
        this.nProc = numProc;
        this.v = new int[numProc];
        v[id] = 1;
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
        for(int i = 0; i < nProc; i++){
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
    
    public int getValue(int i){
        return v[i];
    }
    
    public int[] getVector(){
        return v.clone();
    }
    
    @Override
    public String toString(){
        return Arrays.toString(v);
    }
}

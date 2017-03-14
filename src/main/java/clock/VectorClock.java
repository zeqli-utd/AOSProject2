package clock;

import java.util.Arrays;

public class VectorClock {
    public int[] v;
    private int id;
    private int nProc;
    
    public VectorClock(int numProc, int id){
        this.id = id;
        this.nProc = numProc;
        this.v = new int[numProc];
        v[id] = 1;
    }
    
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
    
    public int getValue(int i){
        return v[i];
    }
    
    @Override
    public String toString(){
        return Arrays.toString(v);
    }
}

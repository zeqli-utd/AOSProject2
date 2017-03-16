package snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aos.Linker;
import aos.Message;
import aos.Process;
import aos.Tag;

//Pj is a neighbor of Pi if there is a channel from Pi to Pj

public class RecvCamera extends Process implements Camera {
    private static final int WHITE = 0;
    private static final int RED = 1;
    private int myColor = WHITE;
    private boolean closed[];   // closed[k] stop to recording messages along kth incomming channel
    private List<List<Message>> channels = null; //record the state of the kth coming channel
    private CamUser app;
    
    //RecvCamera initialize the variables of the algorithm
    //All channels are initialized to empty
    public RecvCamera(Linker initLinker, CamUser app) {
        super(initLinker);
       // int numProc = initLinker.getNumProc(); //no variable numProc before, have to get by linker //protected!
        closed = new boolean[numProc];  
        channels = new ArrayList<>(numProc);  
        for(int i = 0; i < numProc; i++){          // for each neighboring process Pk, closed[k] is initialized to false.
            channels.add(new ArrayList<>());
            closed[i] = false;
        } 
        this.app = app;
    }

    @Override
    public synchronized void globalState() {
        //The method globalState turns the process red, records the local state, and sends the Marker message on all outgoing channels
        myColor = RED;
        app.localState();     // Record local State;
        try {
            sendToNeighbors(Tag.MARKER, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    
    
    @Override
    // the method handleMessage gives the rule for receiving a marker message
    public void handleMessage(Message m, int srcId, Tag tag) throws IOException {  
        if(tag.equals(Tag.MARKER)){      
        	 if(myColor == WHITE)    // if the process is white, it turns red by involking globalState()
                 globalState();
             closed[srcId] = true;    //set closed[srcId] to true because there cannot be any message of type wr in that channel after the marker is received
             if(isDone()){  
                 System.out.println("Channel State : Transit Messages");
                 for(int i = 0; i < numProc; i++){
                     while(!channels.get(i).isEmpty()){
                         System.out.println(channels.get(i).remove(0).toString());
                     }
                 }
             } 
        }
        else { // handle application message, true if the application message is of type wr
            if(myColor == RED && !closed[srcId])
                channels.get(srcId).add(m);
            app.handleMessage(m, srcId, tag);
        }

    }
    
    //the method isDone() determines whether the process has recorded its local state and all incoming channels 
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

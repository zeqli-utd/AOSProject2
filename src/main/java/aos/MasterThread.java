package aos;

import java.io.IOException;

public class MasterThread implements Runnable {

    private int myId;
    private SpanTree process;
    
    
    public MasterThread(int myId, SpanTree process) {
        this.myId = myId;
        this.process = process;
    }
    
    @Override
    public void run() {
        try {
            process.computeGlobal();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

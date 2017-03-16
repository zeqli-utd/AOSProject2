package snapshot;

import java.io.IOException;
import aos.Message;
import aos.Tag;
import helpers.Linker;

public class CamCircToken extends CircToken implements CamUser {
    public CamCircToken(Linker initComm, int coordinator) {
        super(initComm, coordinator);
    }
    public synchronized void localState() {
        System.out.println("local state: " + vectorclock); //!!this clock should get from linker or other place
                                                           // make sure how the vectorClock is used and in which Classes
    }

    public synchronized void handleMessage(Message msg, int srcId, Tag tag) throws IOException{
    	System.out.print("In transit message: ");
        System.out.println(String.format("[Node %d] [Request] content=%s", myId, msg.toString()));
    }
 
}

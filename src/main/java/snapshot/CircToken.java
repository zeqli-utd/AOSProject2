package snapshot;
import java.util.Timer;

import aos.Linker;
import aos.Message;
import aos.Process;

public class CircToken extends Process implements Lock {
    boolean haveToken;
    boolean wantCS = false;
    public CircToken(Linker initComm, int coordinator) {
        super(initComm);
        haveToken = (myId == coordinator);
    }
    public synchronized void initiate() {
        if (haveToken) sendToken();
    }
    public synchronized void requestCS() {
        wantCS = true;
        while (!haveToken) myWait();
    }
    public synchronized void releaseCS() {
        wantCS = false;
        sendToken();
    }
    
    void myWait(){
    	System.out.println("Empty Method myWait()");
    }
    void sendToken() {
        /*if (haveToken && !wantCS) {
            int next = (myId + 1) % N;
            System.out.println("Process " + myId + "has sent the token");
            sendMsg(next, "token");
            haveToken = false;
        }*/
    	System.out.println("Empty Method CircToken sendToken()");
    }
    public synchronized void handleMsg(Message m, int src, String tag) {
        if (tag.equals("token")) {
            haveToken = true;
            if (wantCS)
                notify();
            else {
                Utility.mySleep(1000);
                sendToken();
            }
        }
    }
}

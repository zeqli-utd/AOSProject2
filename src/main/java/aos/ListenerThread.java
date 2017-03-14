package aos;

import java.io.IOException;

/**
 * Thread listen to each neighbors
 * @author zeqing
 *
 */
public class ListenerThread implements Runnable {
    int channel;
    MessageHandler process;
    
    
    public ListenerThread(int channel, MessageHandler process) {
        this.channel = channel;
        this.process = process;
    }


    @Override
    public void run() {
        while(true){
            Message m;
            try {
                m = process.receiveMessage(channel);
                process.handleMessage(m, m.getSrcId(), m.getTag());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }

}

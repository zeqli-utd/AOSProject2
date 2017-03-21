package aos;

/**
 * MAP protocol runner
 * @author zeqing
 *
 */
public class MAPThread implements Runnable {
    private int myId;
    private MAP process;
    
    
    public MAPThread(int myId, MAP process) {
        this.myId = myId;
        this.process = process;
    }
    

    @Override
    public void run() {
        try {
            while (!process.isStop()) {
                if (!process.isActive()){
                    process.await();
                }
                process.sendApplicationMessage();            
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("[Node %d] [MAP] LOCAL_STATE=%s\n", myId, process.isActive() ? "ACTIVE" : "PASSIVE")); 
    }

}

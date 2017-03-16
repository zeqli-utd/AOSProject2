package aos;

public class MAPThread implements Runnable {
    private int myId;
    private MAP process;
    
    
    public MAPThread(int myId, MAP process) {
        this.myId = myId;
        this.process = process;
    }
    

    @Override
    public void run() {
        //MAP m = new MAP(linker, false, myId, "MAP protocol", proto.globalParams);
        try {
            while (!process.isStop()) {
                while (!process.isActive()){
                    process.await();
                }
                if (process.isPrevActive()){
                    System.out.println(
                            String.format("[Node %d] "
                                    + "[MAP] Consecutive Active "
                                    + "Delay %sms", myId, 
                                    process.getMIN_SEND_DELAY()));
                    Thread.sleep(process.getMIN_SEND_DELAY());
                }
                process.sendApplicationMessage();            
            }
            
            
            //System.out.println(m.getClock());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(
                String.format("[Node %d] [MAP] LOCAL_STATE=%s", 
                        myId, 
                        process.isActive() ? 1 : 0));
        //System.out.println(proto.globalParams.get(GlobalParams.LOCAL_STATE)); 
    }

}

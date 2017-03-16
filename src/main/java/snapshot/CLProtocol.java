package snapshot;

import aos.Linker;
import aos.ListenerThread;

public class CLProtocol implements Runnable{
	private CamCircToken sp = null;
	private Camera camera = null;
	private int myId; 
	private int numProcess;
	public CLProtocol(Linker linker, int id, int num){
		sp = new CamCircToken(linker,0);
        camera = new RecvCamera(linker, sp);
        this.myId = id;
        this.numProcess = num;
	}
	
	public void run(){
		  
          sp.initiate();    
          for (int i = 0; i < numProcess; i++)
              if (i != myId) 
              	(new Thread(new ListenerThread(myId,i,camera))).start();
          if (myId == 0) camera.globalState();           	
	}

}

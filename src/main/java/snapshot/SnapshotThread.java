package snapshot;

import java.io.IOException;

public class SnapshotThread implements Runnable {
    private RecvCamera camera = null;
    private int myId;
    private boolean isRoot;

    public SnapshotThread(int myId, RecvCamera camera) {
        this.camera = camera;
        this.myId = myId;
        this.isRoot = (myId == 0);
    }

    @Override
    public void run() {
        try {
            while (true){                
                if (isRoot) {    
                    camera.requestSnapshotPermission();
                    
                    System.out.println(String.format("[Node %d] [Snapshot] ***** Delay ***** %dms", myId, camera.SNAP_SHOT_DELAY));
                    Thread.sleep(camera.SNAP_SHOT_DELAY);
                    camera.globalState();
                }
                camera.waitForSnapshotDone();
                camera.computeGlobal();
            }
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}

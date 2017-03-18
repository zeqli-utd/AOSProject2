package snapshot;

import java.io.IOException;

public class SnapshotThread implements Runnable {
    private RecvCamera camera = null;
    private int myId;

    public SnapshotThread(int myId, RecvCamera camera) {
        this.camera = camera;
        this.myId = myId;
    }

    @Override
    public void run() {
        try {
            while (true){
                System.out.println(
                        String.format("[Node %d] [Snapshot] ***** Delay ***** %dms", 
                                myId, camera.SNAP_SHOT_DELAY));
                Thread.sleep(camera.SNAP_SHOT_DELAY);
                if (myId == 0) {
                    camera.requestSnapshotPermission();
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

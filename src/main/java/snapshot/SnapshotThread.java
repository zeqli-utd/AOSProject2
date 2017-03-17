package snapshot;

public class SnapshotThread implements Runnable {
    private Camera camera = null;
    private int myId;

    public SnapshotThread(int myId, Camera camera) {
        this.camera = camera;
        this.myId = myId;
    }

    @Override
    public void run() {
        if (myId == 0)
            camera.globalState();
    }

}

package snapshot;

import aos.ListenerThread;
import aos.Node;
import helpers.Linker;

public class CLProtocol implements Runnable {
    private CamCircToken sp = null;
    private Camera camera = null;
    private int myId;
    private int numProcess;
    private Linker linker;

    public CLProtocol(Linker linker, int id, int num) {
        this.linker = linker;
        sp = new CamCircToken(linker, 0);
        camera = new RecvCamera(linker, sp);
        this.myId = id;
        this.numProcess = num;
    }

    public void run() {

        sp.initiate();
        for (Node node : linker.getNeighbors()) {
            (new Thread(new ListenerThread(myId, node.getNodeId(), camera))).start();
        }
        if (myId == 0)
            camera.globalState();
    }

}

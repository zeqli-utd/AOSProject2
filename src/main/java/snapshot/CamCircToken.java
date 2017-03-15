package snapshot;

import aos.Linker;

public class CamCircToken extends CircToken implements CamUser {
    public CamCircToken(Linker initComm, int coordinator) {
        super(initComm, coordinator);
    }
    public synchronized void localState() {
        System.out.println("local state: haveToken=" + haveToken);
    }
}

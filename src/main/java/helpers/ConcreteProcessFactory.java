package helpers;

import aos.MAP;
import aos.Process;
import aos.SpanTree;
import clock.VectorClock;
import snapshot.RecvCamera;

/**
 * 
 * @author zeqing
 *
 */
public class ConcreteProcessFactory implements ProcessFactory{
    
    private Registry repo;
    
    public ConcreteProcessFactory(){
        this.repo = Registry.getInstance();
    }
    
    public VectorClock getDefaultVectorClock(){
        int topologySize = Integer.parseInt(repo.getProperty(PropConst.NUM_NODES));
        int myId = repo.getLinker().getMyId();
        VectorClock v = new VectorClock(topologySize, myId); 
        return v;
    }

    @Override
    public Process createProcess() {
        Process proc = new Process(repo.getLinker());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public SpanTree createSpanTree() {
        SpanTree proc = new SpanTree(repo.getLinker());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public MAP createMAP() {
        MAP proc = new MAP(repo.getLinker());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public RecvCamera createCamera() {
        RecvCamera proc = new RecvCamera(repo.getLinker());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }
}

package helpers;

import java.util.Map;

import aos.GlobalParams;
import aos.MAP;
import aos.Process;
import aos.SpanTree;
import clock.VectorClock;
import snapshot.Camera;
import snapshot.RecvCamera;

/**
 * 
 * @author zeqing
 *
 */
public class ConcreteProcessFactory implements ProcessFactory{
    
    private Repository registry;
    
    public ConcreteProcessFactory(Repository repository){
        this.registry = repository; 
    }
    
    public VectorClock getDefaultVectorClock(){
        int topologySize = getGlobalParameters().get(GlobalParams.NUM_NODES);
        int myId = registry.getLinker().getMyId();
        VectorClock v = new VectorClock(topologySize, myId); 
        // TODO Test only, delete
        for (int i = 0; i < myId; i++){
            v.tick();
        }
        return v;
    }

    @Override
    public Process createProcess() {
        Linker linker = registry.getLinker();
        Process proc = new Process(linker);
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public SpanTree createSpanTree() {
        Linker linker = registry.getLinker();
        SpanTree proc = new SpanTree(linker);
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public MAP createMAP() {
        if (!registry.containsKey(RKey.KEY_GLOB_PARAMS.name())){
            System.err.println("[Error] Global parameters unset!");
            return null;
        }
        Linker linker = registry.getLinker();
        MAP proc = new MAP(linker, getGlobalParameters());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }

    @Override
    public RecvCamera createCamera() {
        if (!registry.containsKey(RKey.KEY_GLOB_PARAMS.name())){
            System.err.println("[Error] Global parameters unset!");
            return null;
        }
        Linker linker = registry.getLinker();
        RecvCamera proc = new RecvCamera(linker, getGlobalParameters());
        proc.setVectorClock(getDefaultVectorClock());
        return proc;
    }
    
    private Map<GlobalParams, Integer> getGlobalParameters(){
        @SuppressWarnings("unchecked")
        Map<GlobalParams, Integer> globalParams = 
                (Map<GlobalParams, Integer>) registry.getObject(
                        RKey.KEY_GLOB_PARAMS.name());
        return globalParams;
    }
    
    
    
}

package helpers;

import java.util.Map;

import aos.PropertyType;
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
    
    private Repository registry;
    
    public ConcreteProcessFactory(Repository repository){
        this.registry = repository; 
    }
    
    public VectorClock getDefaultVectorClock(){
        int topologySize = getGlobalParameters().get(PropertyType.NUM_NODES);
        int myId = registry.getLinker().getMyId();
        VectorClock v = new VectorClock(topologySize, myId); 
        v.tick(); // Initialization
        return v;
    }

    @Override
    public Process createProcess() {
        Linker linker = registry.getLinker();
        Process proc = new Process(linker);
        proc.setVectorClock(getDefaultVectorClock());
        proc.setRegistry(registry);
        return proc;
    }

    @Override
    public SpanTree createSpanTree() {
        Linker linker = registry.getLinker();
        SpanTree proc = new SpanTree(linker);
        proc.setVectorClock(getDefaultVectorClock());
        proc.setRegistry(registry);
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
        proc.setRegistry(registry);
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
        proc.setRegistry(registry);
        return proc;
    }
    
    private Map<PropertyType, Integer> getGlobalParameters(){
        @SuppressWarnings("unchecked")
        Map<PropertyType, Integer> globalParams = 
                (Map<PropertyType, Integer>) registry.getObject(
                        RKey.KEY_GLOB_PARAMS.name());
        return globalParams;
    }
    
    
    
}

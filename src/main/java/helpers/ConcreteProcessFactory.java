package helpers;

import java.util.Map;

import aos.GlobalParams;
import aos.MAP;
import aos.Process;
import aos.SpanTree;
import snapshot.Camera;

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

    @Override
    public Process createProcess() {
        Linker linker = registry.getLinker();
        return new Process(linker);
    }

    @Override
    public SpanTree createSpanTree() {
        Linker linker = registry.getLinker();
        return new SpanTree(linker);
    }

    @Override
    public MAP createMAP() {
        if (!registry.containsKey(RKey.KEY_GLOB_PARAMS.name())){
            System.err.println("[Error] Global parameters unset!");
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<GlobalParams, Integer> globalParams = 
                (Map<GlobalParams, Integer>) registry.getObject(
                        RKey.KEY_GLOB_PARAMS.name());
        Linker linker = registry.getLinker();
        return new MAP(linker, globalParams);
    }

    @Override
    public Camera createCamera() {
        Linker linker = registry.getLinker();
        int myId = linker.getMyId();
        //return new RecvCamera(linker);
        return null;
    }
    
    
    
}

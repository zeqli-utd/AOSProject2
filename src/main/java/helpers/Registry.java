package helpers;

import java.util.HashMap;
import java.util.Map;

public class Registry implements Repository {
    
    /** 
     * The repository can also be used as an object store
     * for various objects used by different protocols.
     */
    private Map<String, Object> objectMap;
    
    public Registry(){
        this.objectMap = new HashMap<String, Object>();
    }
    
    
    private Linker linker = null;
    @Override
    public void addLinker(Linker linker) {
        this.linker = linker;
    }

    @Override
    public Linker getLinker() {
        return linker;
    }

    /**
     * Puts object by key.
     * @param key key, may not be null.
     * @param value object to associate with key.
     */
    @Override
    public void putObject(final String key, final Object value){
        objectMap.put(key, value);
    }

    /**
     * Get object by key.
     * @param key key, may not be null.
     * @return object associated with key or null.
     */
    @Override
    public Object getObject(final String key) {
        return objectMap.get(key);
    }
    
    @Override
    public boolean containsKey(String key){
        return objectMap.containsKey(key);
    }

    @Override
    public ProcessFactory getProcessFactory() {
        boolean isValid = true;
        // Check all required components provided
        if (!objectMap.containsKey(RKey.KEY_NEIGHBORS.name())){
            System.err.println("[Error] Neighbors node unset!");
            isValid = false;
        }
        
        if (linker == null) {
            System.err.println("[Error] Linker unset!");
            isValid = false;
        }
        
        if (isValid) {
            return new ConcreteProcessFactory(this);
        } else {
            return null;
        }
    }

}

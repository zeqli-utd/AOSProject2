package helpers;

import java.util.HashMap;
import java.util.Map;

import socket.Linker;

public class Registry{
   

    private static Registry instance = null;
    
    /** 
     * The repository can also be used as an object store
     * for various objects used by different protocols.
     */
    private Map<String, Object> objectMap = null;
    
    
    private Map<String, String> properties = null;
    
    private Registry(){
        this.objectMap = new HashMap<>();
        this.properties = new HashMap<>();
    }
    
    public static synchronized Registry getInstance(){
        if (instance == null)
            instance = new Registry();
        return instance;
    }
    
    private Linker linker = null;
    
    public void addLinker(Linker linker) {
        this.linker = linker;
    }

    public Linker getLinker() {
        return linker;
    }

    /**
     * Puts object by key.
     * @param key key, may not be null.
     * @param value object to associate with key.
     */
    public void putObject(final String key, final Object value){
        objectMap.put(key, value);
    }

    /**
     * Get object by key.
     * @param key key, may not be null.
     * @return object associated with key or null.
     */
    public Object getObject(final String key) {
        return objectMap.get(key);
    }
    
    public String getProperty(String key){
        return properties.get(key);
    }
    
    public void setProperty(String key, String value){
        properties.put(key, value);
    }
    
    public boolean containsKey(String key){
        return objectMap.containsKey(key);
    }
    
    

    public ProcessFactory getProcessFactory() {
        boolean isValid = true;
        // Check all required components provided
        if (!objectMap.containsKey(PropConst.NEIGHBORS)) {
            System.err.println("[Error] Neighbors node unset!");
            isValid = false;
        }
        
        if (linker == null) {
            System.err.println("[Error] Linker unset!");
            isValid = false;
        }
        
        if (isValid) {
            return new ConcreteProcessFactory();
        } else {
            return null;
        }
    }

}

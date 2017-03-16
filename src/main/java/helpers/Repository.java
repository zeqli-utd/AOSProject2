package helpers;

/**
 * A SystemRepository can act as a point of registry 
 * for system configuration
 * it is used to store and retrieve bootstrap objects. 
 * @author zeqing
 *
 */
public interface Repository {
    void addLinker(Linker linker);
    Linker getLinker();
    void putObject(String key, Object value);
    Object getObject(String key);
    ProcessFactory getProcessFactory();
    boolean containsKey(String key);
}

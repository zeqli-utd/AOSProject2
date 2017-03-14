package aos;

/**
 * Interface which all configuration filer loader class need to implement.
 * @author zeqing
 *
 */
public interface ConfigurationLoader {
    void loadConfig(String relativePath, int myId, Protocol proto);
    void loadConfigFromAbs(String absolutePath, int myId, Protocol proto);
}

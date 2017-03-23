package unit;

import java.io.File;

import org.junit.Test;

import helpers.ConfigurationLoader;
import helpers.MAPConfigurationLoader;
import helpers.Registry;

public class MAPConfigurationLoaderTest {
    /* Load configuration file */
    Registry registry = Registry.getInstance();   // Manage system variables              
    ConfigurationLoader configLoader = new MAPConfigurationLoader();
    
    @Test
    public void testConfig() {
        File file = new File("src/test/resources/config.txt");
        System.out.println(file.getAbsolutePath());
        
        for(int myId = 0; myId < 5; myId++){
            System.out.println(String.format("===== Node %d Configuration =====", myId));
            configLoader.loadConfigFromAbs(file.getAbsolutePath(), myId);
        }
    }

}

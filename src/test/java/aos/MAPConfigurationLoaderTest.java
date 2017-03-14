package aos;

import java.io.File;

import org.junit.Test;

public class MAPConfigurationLoaderTest {
    ConfigurationLoader configLoader = new MAPConfigurationLoader();
    
    @Test
    public void testConfig() {
        File file = new File("src/test/resources/config.txt");
        System.out.println(file.getAbsolutePath());
        
        for(int myId = 0; myId < 5; myId++){
            System.out.println(String.format("===== Node %d Configuration =====", myId));
            Protocol proto = new MAP(myId, "MAP" + myId);
            configLoader.loadConfigFromAbs(file.getAbsolutePath(), myId, proto);
            System.out.println(proto.toString());
        }
    }

}

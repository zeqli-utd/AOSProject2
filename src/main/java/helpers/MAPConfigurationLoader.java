package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import aos.Node;

public class MAPConfigurationLoader implements ConfigurationLoader {
    Registry prop = null;
    
    public MAPConfigurationLoader(){
        this.prop = Registry.getInstance();
    }
    
    public void loadConfig(String relativePath, int myId){
        Path file = Paths.get(relativePath).toAbsolutePath();
        
        // System.out.println(file.toString());
        loadConfigFromAbs(file.toString(), myId);
    }

    public void loadConfigFromAbs(String absolutePath, int myId) {
        List<Node> hosts = new LinkedList<>();
        List<Node> neighbors = new LinkedList<>();
        Path file = Paths.get(absolutePath);
        
        // Store file name
        String filename = file.getFileName().toString();
        String fileDir = file.getParent().toString();
        prop.setProperty(PropConst.CONFIG_FILE_NAME, filename);
        prop.setProperty(PropConst.CONFIG_FILE_DIRECTORY, fileDir);
        

        System.out.println(String.format("%s = %s\n", PropConst.CONFIG_FILE_NAME,filename));
        System.out.println(String.format("%s = %s\n", PropConst.CONFIG_FILE_DIRECTORY,fileDir));
        
        
        
        StringBuilder logger = new StringBuilder();
        logger.append(String.format("[Node %d] [Config Loader] Global Paramaters:\n", myId));
        
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            int n = 0;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)             // Skip empty lines
                    continue;
                String[] params = line.split("\\s+"); // Split to read parameters
                /*
                 * 0 NUM_NODES,          // "Number of Nodes";
                 * 1 MIN_PER_ACTIVE,     // "Minimum number of active node at each round";
                 * 2 MAX_PER_ACTIVE,     // "Maximum number of active node at each round";
                 * 3 MIN_SEND_DELAY,     // "Minimum number of send interval";
                 * 4 SNAP_SHOT_DELAY,    // "Snapshot interval";
                 * 5 MAX_NUMBER,         // "Maximum messages a node can send in its life cycle";
                 * 6 LOCAL_STATE;        // "Local state: active or passive";
                 */

                prop.setProperty(PropConst.NUM_NODES, params[0]);
                prop.setProperty(PropConst.MIN_PER_ACTIVE, params[1]);
                prop.setProperty(PropConst.MAX_PER_ACTIVE, params[2]);
                prop.setProperty(PropConst.MIN_SEND_DELAY, params[3]);
                prop.setProperty(PropConst.SNAP_SHOT_DELAY, params[4]);
                prop.setProperty(PropConst.MAX_NUMBER, params[5]);
                
                logger.append(String.format("%s = %s\n", PropConst.NUM_NODES, params[0]));
                logger.append(String.format("%s = %s\n", PropConst.MIN_PER_ACTIVE, params[1]));
                logger.append(String.format("%s = %s\n", PropConst.MAX_PER_ACTIVE, params[2]));
                logger.append(String.format("%s = %s\n", PropConst.MIN_SEND_DELAY, params[3]));
                logger.append(String.format("%s = %s\n", PropConst.SNAP_SHOT_DELAY, params[4]));
                logger.append(String.format("%s = %s\n", PropConst.MAX_NUMBER, params[5]));
                
                
                
                // Randomly set other nodes' state.  0 - passive, 1 - active
                // nextInt is normally exclusive of the top value,
                // so add 1 to make it inclusive
                // Always set node 0 as active
                
                int initState = (myId == 0) ? 1 : ThreadLocalRandom.current().nextInt(0, 2);
                prop.setProperty(PropConst.LOCAL_STATE, Integer.toString(initState));
                logger.append(String.format("%s = %d\n", PropConst.LOCAL_STATE, initState));
                break;
            }

            n =  Integer.parseInt(prop.getProperty(PropConst.NUM_NODES));
            
            // Load host list.
            while (n != 0 && (line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                
                //System.out.println(String.format("[host info: %s]", line));
                String[] hostInfo = line.split("\\s+");
                
                // hostInfo[0] - node id, hostInfo[1] - host addr, hostInfo[2] - host port
                Node host = new Node(Integer.parseInt(hostInfo[0]), hostInfo[1] + ".utdallas.edu", hostInfo[2]);
                hosts.add(host);
                n--;
            }
            validateConfigurationFile(n);
            
            n = Integer.parseInt(prop.getProperty(PropConst.NUM_NODES));
            
            // Load neighbors
            while (n != 0 && (line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                String[] neighborIds = line.split("\\s+");
                int currentId = Integer.parseInt(prop.getProperty(PropConst.NUM_NODES)) - n;
                
                if(currentId == myId){
                    for(int i = 0; i < neighborIds.length; i++){
                        int id = Integer.parseInt(neighborIds[i]);
                        Node node = hosts.get(id);
                        neighbors.add(node);
                    }
                }
                n--;
            }
            
            validateConfigurationFile(n);
            
            doConfigure(neighbors);
            
            System.out.println(logger.toString());
            
            
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        } catch (NullPointerException e){
            System.err.println(e.getMessage());
        }
    }
    
    public void doConfigure(List<Node> neighbors){
        Collections.sort(neighbors);
        prop.putObject(PropConst.NEIGHBORS, neighbors);
    }
    
    private void validateConfigurationFile(int n) throws IOException{
        if(n != 0){
            throw new IOException("Insufficent valid lines in config file.");
        }
    }
}

package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import aos.PropertyType;
import aos.Node;

public class MAPConfigurationLoader implements ConfigurationLoader {
    Repository repo;
    
    public MAPConfigurationLoader(Repository repo){
        this.repo = repo;
    }
    
    public void loadConfig(String relativePath, int myId){
        Path file = Paths.get(relativePath).toAbsolutePath();
        
        // System.out.println(file.toString());
        loadConfigFromAbs(file.toString(), myId);
    }

    public void loadConfigFromAbs(String absolutePath, int myId) {
        Map<PropertyType, Integer> globalParams= new EnumMap<>(PropertyType.class);
        List<Node> hosts = new LinkedList<>();
        List<Node> neighbors = new LinkedList<>();
        Path file = Paths.get(absolutePath);
        
        // Store file name
        String filename = file.getFileName().toString();
        String fileDir = file.getParent().toString();
        repo.putObject(RKey.KEY_CONFIG_FILE_NAME.name(), filename);
        repo.putObject(RKey.KEY_CONFIG_FILE_DIRECTORY.name(), fileDir);
        System.out.println(filename);
        System.out.println(fileDir);
        
        
        
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
                for (int i = 0; i < params.length + 1; i++){
                     if (i == params.length) {
                        
                     } else {
                        int value = Integer.parseInt(params[i]);
                         PropertyType key = PropertyType.values()[i];
                         globalParams.put(key, value);
                         logger.append(String.format("%s = %d\n", key, value));
                     }
                }
                
                
                
                // Randomly set other nodes' state.  0 - passive, 1 - active
                // nextInt is normally exclusive of the top value,
                // so add 1 to make it inclusive
                // Always set node 0 as active
                int value = (myId == 0) ? 1 : ThreadLocalRandom.current().nextInt(0, 2);
                PropertyType key = PropertyType.LOCAL_STATE;
                globalParams.put(key, value);
                logger.append(String.format("%s = %d\n", key, value));
                break;
            }
            
            n = globalParams.get(PropertyType.NUM_NODES);
            
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
            if(n != 0){
                throw new IOException("Insufficent valid lines in config file.");
            }
            
            n = globalParams.get(PropertyType.NUM_NODES);
            
            // Load neighbors
            while (n != 0 && (line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                String[] neighborIds = line.split("\\s+");
                int currentId = globalParams.get(PropertyType.NUM_NODES) - n;
                
                if(currentId == myId){
                    for(int i = 0; i < neighborIds.length; i++){
                        int id = Integer.parseInt(neighborIds[i]);
                        Node node = hosts.get(id);
                        neighbors.add(node);
                    }
                }
                n--;
            }
            if(n != 0){
                throw new IOException("Insufficent valid lines in config file.");
            }
            
            System.out.println(logger.toString());
            doConfigure(globalParams, neighbors);
            
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        } catch (NullPointerException e){
            System.err.println(e.getMessage());
        }
    }
    
    public void doConfigure(Map<PropertyType, Integer> globalParams, List<Node> neighbors){
        Collections.sort(neighbors);
        repo.putObject(RKey.KEY_NEIGHBORS.name(), neighbors);
        repo.putObject(RKey.KEY_GLOB_PARAMS.name(), globalParams);
    }
}

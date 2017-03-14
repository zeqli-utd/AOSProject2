package aos;

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

public class MAPConfigurationLoader implements ConfigurationLoader {
    
    public void loadConfig(String relativePath, int myId, Protocol proto){
        Path file = Paths.get(relativePath).toAbsolutePath();
        
        // System.out.println(file.toString());
        loadConfigFromAbs(file.toString(), myId, proto);
    }

    public void loadConfigFromAbs(String absolutePath, int myId, Protocol proto) {
        Map<GlobalParams, Integer> globalParams= new EnumMap<>(GlobalParams.class);
        List<Node> hosts = new LinkedList<>();
        List<Node> neighbors = new LinkedList<>();
        Path file = Paths.get(absolutePath);
        
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            int n = 0;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                String[] params = line.split("\\s+");
                for(int i = 0; i < params.length; i++){
                    int value = Integer.parseInt(params[i]);
                    GlobalParams key = GlobalParams.values()[i];
                    globalParams.put(key, value);
                    // System.out.println(String.format("Global parameter (%s, %d) added", key, value));
                }
                break;
            }
            
            n = globalParams.get(GlobalParams.NUM_NODES);
            
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
            
            n = globalParams.get(GlobalParams.NUM_NODES);
            
            // Load neighbors
            while (n != 0 && (line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                String[] neighborIds = line.split("\\s+");
                int currentId = globalParams.get(GlobalParams.NUM_NODES) - n;
                
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
            
            doConfigure(proto, globalParams, neighbors);
            
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        } catch (NullPointerException e){
            System.err.println(e.getMessage());
        }
    }
    
    public void doConfigure(Protocol proto, Map<GlobalParams, Integer> globalParams, List<Node> neighbors){
        Collections.sort(neighbors);
        proto.setNeighbors(neighbors);
        proto.setGlobalParams(globalParams);
    }

}

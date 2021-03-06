package aos;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import helpers.ConfigurationLoader;
import helpers.MAPConfigurationLoader;
import helpers.ProcessFactory;
import helpers.PropConst;
import helpers.Registry;
import snapshot.RecvCamera;
import snapshot.SnapshotThread;
import socket.Linker;
/**
 * 
 * @author Zeqing Li, The University of Texas at Dallas
 * @author Ming Sun, The University of Texas at Dallas
 * @author Jingyi Liu, The University of Texas at Dallas
 */
public class Server {    
    /**
     * 
     * @param args <port> <node id> <config-file>
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java Server <port> <node id> <config file>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int myId = Integer.parseInt(args[1]);
        String relativePath = args[2];
        
        /* Load configuration file */    
        Registry registry = Registry.getInstance();
        ConfigurationLoader configLoader = new MAPConfigurationLoader();
        
        // load List<Node> neighbors
        configLoader.loadConfig(relativePath, myId);
        
        // 1. Setup registry.
        @SuppressWarnings("unchecked")
        List<Node> neighbors = (List<Node>) registry.getObject(PropConst.NEIGHBORS);
        Linker linker = new Linker(myId, neighbors);
        
        
        /* Use thread pools to manage process behaviors */
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        
        try {
            

            linker.buildChannels(port);
            registry.addLinker(linker);
            ProcessFactory factory = registry.getProcessFactory();
            
            
            // 2. Produce main thread process
            RecvCamera proc = factory.createCamera();
            
            // 3. Setup listener thread
            for(Node node : linker.getNeighbors()){
                Runnable task = new ListenerThread(myId, node.getNodeId(), proc);
                executorService.execute(task);
            }
            
            // 4. Wait for spanning tree setup
            proc.waitForTreeConstruction();  
            
            // 5. Setup Chandy Lamport Protocol
            SnapshotThread chandyLamportProtocol = new SnapshotThread(myId, proc);
            
            // 6. Setup MAP protocol
            MAPThread mapProtocol = new MAPThread(myId, proc);
            
            executorService.execute(chandyLamportProtocol);
            executorService.execute(mapProtocol);
            
            // Thread.sleep(30000);
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            linker.close();
        }
        
    }
    


}

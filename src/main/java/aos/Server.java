package aos;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import helpers.ConfigurationLoader;
import helpers.Linker;
import helpers.MAPConfigurationLoader;
import helpers.ProcessFactory;
import helpers.RKey;
import helpers.Registry;
import helpers.Repository;
import snapshot.CamCircToken;
import snapshot.Camera;
import snapshot.RecvCamera;
/**
 * 
 * @author Zeqing Li, zxl165030, The University of Texas at Dallas
 *
 */
public class Server {    
    /**
     * @param args
     *            args[0] - port, args[1] - node id, args[2] - file
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
        Repository registry = new Registry();   // Manage system variables              
        ConfigurationLoader configLoader = 
                new MAPConfigurationLoader(registry);
        
        // load List<Node> neighbors
        // load GlobalParams;
        configLoader.loadConfig(relativePath, myId);
        
        try {
            
            @SuppressWarnings("unchecked")
            List<Node> neighbors = (List<Node>) registry.getObject(RKey.KEY_NEIGHBORS.toString());
            Linker linker = new Linker(myId, neighbors);
            linker.buildChannels(port);
            registry.addLinker(linker);


            ProcessFactory factory = registry.getProcessFactory();
            //SpanTree proc = (SpanTree) factory.createSpanTree();
            MAP proc = (MAP) factory.createMAP();
            

            /* Use thread pools to manage process behaviors */
            ExecutorService executorService = Executors.newFixedThreadPool(50);
            for(Node node : linker.getNeighbors()){
                Runnable task = new ListenerThread(myId, node.getNodeId(), proc);
                executorService.execute(task);
            }
            

            proc.waitForDone();
            System.out.println(String.format("[Node %d] Continue", myId));
               
            Runnable mapTask = new MAPThread(myId, proc);
            executorService.execute(mapTask);
            
            
            
 //-------------------------------------- for CL Protocol--------------------------------------------------
            while(nothalt){ // need to discuss the boolean variable nothalt in which class
            	            // nothalt is used for detect whether the Map halt or not
            	
            	// the following codes need to be put in a thread in a future, which should restart in some time
            	/*  CamCircToken sp = new CamCircToken(linker,0);
                  Camera camera = new RecvCamera(linker, sp);
                  sp.initiate();
                  int numProcess = proto.numProc;
                   
                  for (int i = 0; i < numProcess; i++)
                      if (i != myId) 
                      	(new Thread(new ListenerThread(myId,i,camera))).start();
                  if (myId == 0) camera.globalState();       */    	
            	CLProtocol clp = new CLProtocol(linker,myId,proto.numProc);
            	(new Thread(clp)).start();
            	Thread.sleep(100);
            }
//---------------------------------------CL Protocol End----------------------------------------------------
            Thread.sleep(5000);
            linker.close();
            executorService.shutdown();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    


}

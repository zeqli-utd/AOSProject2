package aos;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
		Protocol proto = new MAP(myId, "MAP protocol");
		ConfigurationLoader configLoader = new MAPConfigurationLoader();
		configLoader.loadConfig(relativePath, myId, proto);
		try {
			Linker linker = new Linker(myId, proto.getNeighbors());
			linker.buildChannels(port);
			
			/* Use thread pools to manage process behaviors */
			ExecutorService executorService = new ScheduledThreadPoolExecutor(proto.getNumProc()); 
			for(int i = 0; i < proto.getNumProc(); i++){
				Process proc = new Process(linker);
				Runnable task = new ListenerThread(i, proc);
				executorService.submit(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}

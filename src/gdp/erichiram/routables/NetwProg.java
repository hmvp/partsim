package gdp.erichiram.routables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class NetwProg {

	private static final boolean DEBUG = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: remove these sample values when the argument parser is in place
		int argId = Integer.valueOf(args[0]);
		Integer[] argNeighbours = new Integer[(args.length-1)/2];
		int[] argWeights = new int[(args.length-1)/2];
		
		for(int i = 1,n = 0,w = 0; i < args.length;)
		{
			argNeighbours[n++] = Integer.valueOf(args[i++]);
			argWeights[w++] = Integer.valueOf(args[i++]);
		}
		

		// create the application
		new NetwProg(argId, argNeighbours, argWeights);
	}

	public final int id;
	private volatile int t;
	private RoutingTable routingTable;
	private ServerSocket socket;

	public NetwProg(int argId, Integer[] argNeighbours, int[] argWeights) {

		this.id = argId;
		
		List<Integer> neighbours = Arrays.asList(argNeighbours);

		Map<Integer, SocketHandler> socketHandlers = initializeSocketHandlers(neighbours);
		routingTable = new RoutingTable(this, socketHandlers, neighbours);
		
		for(SocketHandler s : socketHandlers.values())
		{
			s.start();
		}
		
		SwingUtilities.invokeLater(new Gui(this));
	}

	private Map<Integer, SocketHandler> initializeSocketHandlers(Collection<Integer> neighbours) {
		// initialize threads for sockets
		Map<Integer, SocketHandler> socketHandlers = new HashMap<Integer, SocketHandler>();
		
		try {
			socket = new ServerSocket(id);
		
		
			//connect to everyone higher than me
			for (int neighbour : neighbours) {
				if (neighbour > id)
				{
				
					SocketHandler socketHandler = new SocketHandler(this, neighbour, routingTable);
					socketHandlers.put(neighbour, socketHandler);
					//socketHandler.start();
				}
			}
		} catch (IOException e1) {
			System.err.println("port "+ id +" already taken");
			System.exit(1);
		}
		
		//listen and start sockets if needed
		while(socketHandlers.size() != neighbours.size()){
			Socket clientsocket;
			try {
				clientsocket = socket.accept();
				//TODO we moeten hier uitvinden van wie deze connectie komt! getPort en getLocalPort geven niet het gewenste resultaat
				int neighbour = clientsocket.;
				SocketHandler socketHandler = new SocketHandler(this, clientsocket, routingTable);
				socketHandlers.put(neighbour, socketHandler);
				//socketHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return socketHandlers;
	}

	public void setT(int t) {
		this.t = t;
		debug("t is set to: "+ t);
	}

	public static void debug(String string) {
		if(DEBUG){
			System.out.println(Thread.currentThread().getId() + ": " + string);	
		}
	}

	public int getT() {
		return t;
	}
}

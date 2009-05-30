package gdp.erichiram.routables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
	private Collection<Integer> neighbours;
	private Map<Integer, SocketHandler> socketHandlers;
	private RoutingTable routingTable;
	private ServerSocket socket;

	public NetwProg(int argId, Integer[] argNeighbours, int[] argWeights) {

		this.id = argId;

		neighbours = Arrays.asList(argNeighbours);
		routingTable = new RoutingTable(socketHandlers);
		
		SwingUtilities.invokeLater(new Gui(this));
		
		initializeSocketHandlers();
	}

	private void initializeSocketHandlers() {
		// initialize threads for sockets
		socketHandlers = new HashMap<Integer, SocketHandler>();
		try {
			socket = new ServerSocket(id);
		} catch (IOException e1) {
			System.err.println("port "+ id +" already taken");
			System.exit(1);
		}
		
		//connect to everyone higher than me
		for (int neighbour : neighbours) {
			if (neighbour > id)
			{
			
				SocketHandler socketHandler = new SocketHandler(neighbour, routingTable);
				socketHandlers.put(neighbour, socketHandler);
				socketHandler.start();
			}
		}
		
		//listen and start sockets if needed
		while(true){
			Socket clientsocket;
			try {
				clientsocket = socket.accept();
				int neighbour = clientsocket.getPort();
				SocketHandler socketHandler = new SocketHandler(clientsocket, routingTable);
				socketHandlers.put(neighbour, socketHandler);
				socketHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

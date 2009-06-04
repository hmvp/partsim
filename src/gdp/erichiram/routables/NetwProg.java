package gdp.erichiram.routables;

import gdp.erichiram.routables.util.ObservableAtomicInteger;
import gdp.erichiram.routables.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

public class NetwProg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// parse the arguments
		int argId = Integer.valueOf(args[0]);
		Integer[] argNeighbours = new Integer[(args.length-1)/2];
		int[] argWeights = new int[(args.length-1)/2];
		
		for(int i = 1,n = 0,w = 0; i < args.length;)
		{
			argNeighbours[n++] = Integer.valueOf(args[i++]);
			argWeights[w++] = Integer.valueOf(args[i++]);
		}
		

		System.out.println("Starting: " + argId);
		
		// create the application
		new NetwProg(argId, argNeighbours, argWeights);
	}

	public final int id;
	private volatile int t;
	public final RoutingTable routingTable;
	private ServerSocket socket;
	
	public ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);

	public NetwProg(int argId, Integer[] argNeighbours, int[] argWeights) {

		this.id = argId;
		
		Map<Integer,Integer> neighbours = new HashMap<Integer, Integer>(argNeighbours.length);
		for (int i = 0; i < argNeighbours.length; i++){
			neighbours.put(argNeighbours[i], argWeights[i]);
		}

		Map<Integer, SocketHandler> socketHandlers = initializeSocketHandlers(neighbours.keySet());
		routingTable = new RoutingTable(this, socketHandlers, neighbours);
		
		for(SocketHandler s : socketHandlers.values())
		{
			s.setRoutingTable(routingTable);
			s.start();
		}
		System.out.println("Starting GUI: " + argId);
		
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
				
					SocketHandler socketHandler = new SocketHandler(this, neighbour);
					socketHandlers.put(neighbour, socketHandler);
					//socketHandler.start();
				}
			}
		} catch (IOException e1) {
			System.err.println("Port " + id + " is already taken.");
			System.exit(1);
		}
		
		//listen and start sockets if needed
		while(socketHandlers.size() != neighbours.size()){
			Socket clientsocket;
			try {
				clientsocket = socket.accept();
				SocketHandler socketHandler = new SocketHandler(this, clientsocket);
				socketHandlers.put(socketHandler.getPort(), socketHandler);
				//socketHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return socketHandlers;
	}

	public void setT(int t) {
		this.t = t;
		Util.debug(id, "'t' is set to: "+ t);
	}

	public int getT() {
		return t;
	}
}

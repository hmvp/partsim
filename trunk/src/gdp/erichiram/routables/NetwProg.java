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
		Map<Integer,Integer> neighbours = new HashMap<Integer, Integer>(args.length);
		for(int i = 1; i < args.length;)
		{
			neighbours.put(Integer.valueOf(args[i++]), Integer.valueOf(args[i++]));
		}
		

		System.out.println("Starting: " + argId);
		
		// create the application
		new NetwProg(argId, neighbours);
	}

	public final int id;
	private volatile int t;
	public final RoutingTable routingTable;
	private ServerSocket socket;
	
	public ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);

	public NetwProg(int argId, Map<Integer,Integer> neighbours) {

		this.id = argId;
		
		Util.debug(id, "start init socketh");

		Map<Integer, SocketHandler> socketHandlers = initializeSocketHandlers(neighbours.keySet());
		
		Util.debug(id, "start init routing");

		routingTable = new RoutingTable(this, socketHandlers, neighbours);
		
		Util.debug(id, "start socketh");
		
		for(SocketHandler s : socketHandlers.values())
		{
			s.setRoutingTable(routingTable);
			s.start();
		}
		System.out.println("Starting GUI: " + argId);
		
		Util.debug(id, "start routing");
		routingTable.initialize();
		
		Util.debug(id, "start gui");
		
		SwingUtilities.invokeLater(new Gui(this));
		
		Util.debug(id, "klaar");

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
					Util.debug(id, "start clientsockethandler "+ neighbour);

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
		Util.debug(id, "start listening");
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

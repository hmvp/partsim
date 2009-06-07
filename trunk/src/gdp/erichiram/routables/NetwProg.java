package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Repair;
import gdp.erichiram.routables.util.ObservableAtomicInteger;
import gdp.erichiram.routables.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
	private Map<Integer, SocketHandler> socketHandlers = new HashMap<Integer, SocketHandler>();;

	public NetwProg(int argId, Map<Integer,Integer> neighbours) {

		this.id = argId;
		
		

		
		
		Util.debug(id, "start routing");

		routingTable = new RoutingTable(this, socketHandlers);
		
		Util.debug(id, "start gui");
		
		SwingUtilities.invokeLater(new Gui(this));
		
		Util.debug(id, "klaar");
		
		Util.debug(id, "start socketh");

		initializeSocketHandlers(neighbours);
	}
	
	public void startRepairConnection(int neighbour, int weight)
	{
		Util.debug(id, "start clientsockethandler "+ neighbour);

		SocketHandler socketHandler = new SocketHandler(this, neighbour, weight);
		socketHandlers.put(neighbour, socketHandler);
		socketHandler.setRoutingTable(routingTable);
		socketHandler.start();
	}

	private void initializeSocketHandlers(Map<Integer,Integer> neighbours) {
		// initialize threads for sockets
		
		try {
			socket = new ServerSocket(id);		
		
			//connect to everyone higher than me
			for (int neighbour : neighbours.keySet()) {
				if (neighbour > id)
				{
					startRepairConnection(neighbour, neighbours.get(neighbour));
				}
			}

		} catch (IOException e1) {
			System.err.println("Port " + id + " is already taken.");
			System.exit(1);
		}
		
		//listen and start sockets if needed
		Util.debug(id, "start listening");
		while(!socket.isClosed()){
			try {
				Socket clientsocket = socket.accept();
				SocketHandler socketHandler = new SocketHandler(this, clientsocket);
				socketHandlers.put(socketHandler.getPort(), socketHandler);
				socketHandler.setRoutingTable(routingTable);
				socketHandler.start();
			} catch (SocketException e) { 
				//we moeten stoppen socket sluit
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(SocketHandler s : socketHandlers.values())
		{
			s.die();
		}
		
		System.exit(0);
	}

	public void setT(int t) {
		this.t = t;
		Util.debug(id, "'t' is set to: "+ t);
	}

	public int getT() {
		return t;
	}

	public void failConnection(Integer value) {
		socketHandlers.get(value).die();
	}
	
	public String toString()
	{
		return "netwProg: "+id;
	}

	public void die() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

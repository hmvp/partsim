package gdp.erichiram.routables;

import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.util.ObservableAtomicInteger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

public class NetwProg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// parse the arguments
		int argId = Integer.valueOf(args[0]);
		Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>(
				args.length);
		for (int i = 1; i < args.length;) {
			neighbours.put(Integer.valueOf(args[i++]), Integer
					.valueOf(args[i++]));
		}

		System.out.println("Starting: " + argId);

		// create the application
		NetwProg nwp = new NetwProg(argId, neighbours);
		
		nwp.run();
	}
	final static boolean DEBUG = true;
	
	
	private volatile int t;
	
	private ServerSocket socket;

	public final int id;
	
	final Map<Integer, Neighbour> idsToSocketHandlers = new ConcurrentHashMap<Integer, Neighbour>();
	
	public final ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);
	public final Map<Integer, Integer> startingNeighbours;
	public final RoutingTable routingTable = new RoutingTable(this);


	public NetwProg(int argId, Map<Integer, Integer> neighbours) {

		this.id = argId;
		this.startingNeighbours = new ConcurrentHashMap<Integer, Integer>(neighbours);
	}
	
	private void run()
	{
		debug("start gui");

		SwingUtilities.invokeLater(new Gui(this));
		
		debug("start sockets");
		
		try {
			socket = new ServerSocket(id);
			
			
			// connect to everyone higher than me
			for (int neighbour : startingNeighbours.keySet()) {
				if (neighbour > id) {
					startRepairConnection(neighbour, startingNeighbours.get(neighbour));
				}
			}
		} catch (IOException e) {
			System.err.println("Port " + id + " is already taken.");
		}

		// listen and start sockets if needed
		debug("start listening");
		while (socket != null && !socket.isClosed()) {
			try {
				Socket clientSocket = socket.accept();
				Neighbour n = new Neighbour(this, clientSocket);
				new Thread(n).start();
				idsToSocketHandlers.put(n.id, n);
			} catch (IOException e) {
				//we socket is dood of er is iets mis. dus stoppen we!
			}
		}

		// socket is closed so we are dead or crashed, either way we clean up
		// and exit
		for (Neighbour s : idsToSocketHandlers.values() ) {
			s.die();
		}

		//System.exit(0);
	}

	public void setT(int t) {
		this.t = t;
		debug("'t' is set to: " + t);
	}

	public int getT() {
		return t;
	}

	public synchronized void startRepairConnection(int neighbour, int weight) {
		if(neighbour == id)
		{
			debug("cannot repair connection to self");
			return;
		}
		
		for(int n : idsToSocketHandlers.keySet())
		{
			if(n == neighbour)
			{
				debug("cannot repair existing connection");
				return;
			}
		}
		
		debug("start clientsockethandler " + neighbour);
		Neighbour n = new Neighbour(this, neighbour, weight);
		new Thread(n).start();
		idsToSocketHandlers.put(neighbour, n);
	}

	public void failConnection(Neighbour n) {
		debug("fail connection to: " + n.id);
		if( idsToSocketHandlers.remove(n.id) == null )
		{
			debug("connection failed earlier");
			return;
		}
		n.die();
	}

	public String toString() {
		return "netwProg: " + id;
	}

	public void die() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void changeWeight(Integer number, Integer number2) {
		routingTable.changeWeight(number, number2);
	}
	
	public void error(String message)
	{
		System.err.println(id + ": " + message);
	}
	
	/**
	 * debug messages switchable with DEBUG boolean
	 * @param message
	 */
	public void debug(String message)
	{
		if (DEBUG)
		{
			System.out.println(id + ": " + message);
		}
	}

	public void send(int x, MyDist myDist) {
		// TODO Auto-generated method stub
		
	}
}

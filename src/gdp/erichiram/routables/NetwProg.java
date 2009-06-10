package gdp.erichiram.routables;

import gdp.erichiram.routables.util.ObservableAtomicInteger;
import gdp.erichiram.routables.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
		Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>(
				args.length);
		for (int i = 1; i < args.length;) {
			neighbours.put(Integer.valueOf(args[i++]), Integer
					.valueOf(args[i++]));
		}

		System.out.println("Starting: " + argId);

		// create the application
		new NetwProg(argId, neighbours);
	}

	private volatile int t;
	private ServerSocket socket;

	public final int id;
	private final Map<Neighbour, Integer> socketHandlers = new HashMap<Neighbour, Integer>();
	
	public final ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);
	public final Map<Integer, Integer> startingNeighbours; //TODO: concurrency issue
	public final RoutingTable routingTable = new RoutingTable(this, socketHandlers);


	public NetwProg(int argId, Map<Integer, Integer> neighbours) {

		this.id = argId;
		this.startingNeighbours = neighbours;
				
		run();
	}

	private void run() {

		Util.debug(id, "start gui");

		SwingUtilities.invokeLater(new Gui(this));
		
		Util.debug(id, "start sockets");
		
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
		Util.debug(id, "start listening");
		while (socket != null && !socket.isClosed()) {
			try {
				Socket clientsocket = socket.accept();
				new Thread(new Neighbour(this, clientsocket)).start();
			} catch (SocketException e) {
				// we moeten stoppen socket sluit
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// socket is closed so we are dead or crashed, either way we clean up
		// and exit
		for (Neighbour s : socketHandlers.keySet()) {
			s.die();
		}

		System.exit(0);
	}

	public void setT(int t) {
		this.t = t;
		Util.debug(id, "'t' is set to: " + t);
	}

	public int getT() {
		return t;
	}

	public void startRepairConnection(int neighbour, int weight) {
		for(Neighbour n : socketHandlers.keySet())
		{
			if(n.getPort() == neighbour)
				return;
		}
		
		Util.debug(id, "start clientsockethandler " + neighbour);
		new Thread(new Neighbour(this, neighbour, weight)).start();
	}

	public void failConnection(Neighbour value) {
		Util.debug(id, "fail connection to: " + value.getPort());
		value.die();
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
}

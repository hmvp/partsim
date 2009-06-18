package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Message;
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
		Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>(args.length);
		for (int i = 1; i < args.length;) {
			neighbours.put(Integer.valueOf(args[i++]), Integer.valueOf(args[i++]));
		}

		System.out.println("Starting: " + argId);

		// create the application
		NetwProg nwp = new NetwProg(argId, neighbours);

		nwp.run();
	}

	final static boolean DEBUG = true;

	public final int id;
	public final Map<Integer, Integer> startingNeighbours;

	private ServerSocket socket;
	final Map<Integer, SocketHandler> idsToSocketHandlers = new ConcurrentHashMap<Integer, SocketHandler>();
	

	private volatile int t;
	public final ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);
	public final RoutingTable routingTable;

	public NetwProg(int argId, Map<Integer, Integer> neighbours) {
		this.id = argId;
		this.startingNeighbours = new ConcurrentHashMap<Integer, Integer>(neighbours);
		routingTable = new RoutingTable(this);
	}

	private void run() {
		debug("Starting GUI");

		SwingUtilities.invokeLater(new Gui(this));

		debug("Starting sockets");

		try {
			socket = new ServerSocket(id);

			// connect to everyone higher than me
			for (int neighbour : startingNeighbours.keySet()) {
				if (neighbour > id) {
					startRepairConnection(neighbour, startingNeighbours.get(neighbour));
				}
			}
		} catch (IOException e) {
			System.err.println("Port " + id + " is already taken");
		}

		// listen and start sockets if needed
		debug("Starting to listen");
		while (socket != null && !socket.isClosed()) {
			try {
				Socket clientSocket = socket.accept();
				SocketHandler n = new SocketHandler(this, clientSocket);
				idsToSocketHandlers.put(n.id, n);
				new Thread(n).start();
			} catch (IOException e) {
				// The Socket just died while blocking during accept(), or something else went wrong. We ignore the IOException and quit the loop.
			}
		}

		// The Socket closed so we want to quit or we just crashed, either way we clean up and exit.
		for (SocketHandler s : idsToSocketHandlers.values()) {
			s.die();
		}

		// TODO: Uncomment before release.
		// System.exit(0);
	}

	public void setT(int t) {
		this.t = t;
		debug("t is set to: " + t);
	}

	public int getT() {
		return t;
	}

	public void changeWeightOrRepairConnection(int id, int weight) {
		if ( idsToSocketHandlers.containsKey(id) ) {
			changeWeight(id, weight);
		} else {
			startRepairConnection(id, weight);
		}
	}

	public void changeWeight(Integer id, Integer weight) {
		debug("Changing the weight to " + id + " to " + weight);
		routingTable.changeWeight(id, weight);
	}

	public synchronized void startRepairConnection(int neighbour, int weight) {
		if (neighbour == id) {
			debug("Cannot repair connection to self");
			return;
		}

		for (int n : idsToSocketHandlers.keySet()) {
			if (n == neighbour) {
				debug("Cannot repair existing connection");
				return;
			}
		}

		debug("Starting client socketHandler for " + neighbour);
		SocketHandler n = new SocketHandler(this, neighbour, weight);
		idsToSocketHandlers.put(neighbour, n);
		new Thread(n).start();
	}

	public void failConnection(int neighbour) {
		debug("Failing connection to " + neighbour);
		
		SocketHandler socketHandler = idsToSocketHandlers.remove(neighbour);
		if ( socketHandler == null) {
			debug("Connection failed earlier.");
		} else {
			socketHandler.die();
		}
	}


	public String toString() {
		return "NetwProg " + id;
	}

	public void die() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void error(String message) {
		System.err.println(id + ": " + message);
	}

	/**
	 * debug messages switchable with DEBUG boolean
	 * 
	 * @param message
	 */
	public void debug(String message) {
		if (DEBUG) {
			System.out.println(id + ": " + message);
		}
	}

	public void send(int x, Message message) {
		idsToSocketHandlers.get(x).send(message);
	}
}

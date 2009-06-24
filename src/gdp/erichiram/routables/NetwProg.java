package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.util.ObservableAtomicInteger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

public class NetwProg extends Observable{

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: input checking.
		
		// Parse the arguments.
		int argId = Integer.valueOf(args[0]);
		Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>(args.length);
		for (int i = 1; i < args.length;) {
			neighbours.put(Integer.valueOf(args[i++]), Integer.valueOf(args[i++]));
		}

		// Create and start the node.
		NetwProg netwProg = new NetwProg(argId, neighbours, false);
		netwProg.debug("Starting.");
		netwProg.run();
	}
	
	private final boolean slave;
	
	/**
	 * Show debug output.
	 */
	final static boolean DEBUG = true;
	
	/**
	 * Id for this node.
	 */
	public final int id;
	
	/**
	 * The neighbours of this node.
	 */
	public final Map<Integer, Integer> neighbours;

	/**
	 * To bootstrap the socketHandlers we use a serverSocket to listen for incoming connections.
	 */
	private ServerSocket serverSocket;
	
	/**
	 * A map containing node ids and their respective socketHandlers.
	 */
	final Map<Integer, SocketHandler> idsToSocketHandlers = new ConcurrentHashMap<Integer, SocketHandler>();
	
	/**
	 * Time a socket sleeps before processing messages, in milliseconds.
	 */
	private volatile int t;
	
	/**
	 * The number of messages sent.
	 */
	public final ObservableAtomicInteger messagesSent = new ObservableAtomicInteger(0);
	
	/**
	 * The routing table for this node.
	 */
	public final RoutingTable routingTable;

	/**
	 * @param argId			id for this node
	 * @param neighbours	mapping of neighbours to distances
	 * @param slave			is this node running in slave (debugging) mode
	 */
	public NetwProg(int argId, Map<Integer, Integer> neighbours, boolean slave) {
		this.id = argId;
		this.neighbours = new ConcurrentHashMap<Integer, Integer>(neighbours);
		this.slave = slave;
		routingTable = new RoutingTable(this);
	}

	/**
	 * Run method for the NetwProg thread
	 */
	public void run() {
		// Setting up the GUI.
		debug("Starting GUI");
		SwingUtilities.invokeLater(new Gui(this));

		try {
			// Start a listening socket.
			debug("Starting sockets");
			serverSocket = new ServerSocket(id);

			// Try to connect to everyone with a higher id using startRepairConnection.
			for (int neighbour : neighbours.keySet()) {
				if (neighbour > id) {
					startRepairConnection(neighbour, neighbours.get(neighbour));
				}
			}
			
		} catch (IOException e) {
			// Most likely the port has already been taken, print an error message.
			System.err.println("Port " + id + " is already taken");
		}

		// Listen and start sockets if needed.
		debug("Starting to listen");
		while (serverSocket != null && !serverSocket.isClosed()) {
			try {
				// Create a socketHandler for every incoming connections.
				Socket clientSocket = serverSocket.accept();
				SocketHandler socketHandler = new SocketHandler(this, clientSocket);
				
				// Add the socketHandler to a map for easy access.
				idsToSocketHandlers.put(socketHandler.id, socketHandler);
				
				// Start the socketHandler.
				new Thread(socketHandler).start();
				setChanged();
				notifyObservers();
				
			} catch (IOException e) {
				// The Socket just died while blocking during accept(), or
				// something else went wrong. We ignore the IOException and quit
				// the loop.
			}
		}

		// The Socket closed so we want to quit or we just crashed, either way we clean up and exit.
		for (SocketHandler s : idsToSocketHandlers.values()) {
			s.die();
		}

		// TODO: Is this really necessary?
		if(!slave)
			System.exit(0);
	}

	/**
	 * Send a message to a node.
	 * @param id		Id for the node.
	 * @param message	Message to be sent.
	 */
	public void send(int id, Message message) {
		idsToSocketHandlers.get(id).send(message);
	}

	/**
	 * Uninitialize the program.
	 */
	public void close() {
		try {
			// Close the listening socket.
			serverSocket.close();
		} catch (IOException e) {
			error("An I/O exception occured while closing the listening socket.");
		}
	}

	/**
	 * Change the weight for an existing connection. Repair (create) a new connection to a previously unconnected node.
	 * @param id		The id for the node to change or repair the connection to.
	 * @param weight	The new weight value. 
	 */
	public void changeWeightOrRepairConnection(int id, int weight) {
		if(id == this.id) {
			return;
		}
		
		if (idsToSocketHandlers.containsKey(id) ) {
			changeWeight(id, weight);
		} else {
			startRepairConnection(id, weight);
		}
		setChanged();
		notifyObservers();
	}

	/**
	 * Change the weight for a connection with a neighbour.
	 * @param id		The id for the node to change or repair the connection to.
	 * @param weight	The new weight value. 
	 */
	public void changeWeight(Integer id, Integer weight) {
		debug("Changing the weight to " + id + " to " + weight);
		routingTable.changeWeight(id, weight);
	}

	/**
	 * Repair (create) a new connection to a previously unconnected node with the specified weight.
	 * @param id		The id for the node to change or repair the connection to.
	 * @param weight	The new weight value. 
	 */
	public synchronized void startRepairConnection(int neighbour, int weight) {

		debug("Starting client socketHandler for " + neighbour);
		SocketHandler n = new SocketHandler(this, neighbour, weight);
		idsToSocketHandlers.put(neighbour, n);
		new Thread(n).start();
	}

	/**
	 * Fail (destroy) a connection with a neighbour.
	 * @param neighbour	The neighbour's id.
	 */
	public void failConnection(int neighbour) {
		debug("Failing connection to " + neighbour);
		
		// Remove the socketHandler from the idsToSocketHandlers map.
		SocketHandler socketHandler = idsToSocketHandlers.remove(neighbour);

		// Kill the socketHandler.
		if ( socketHandler != null) {
			socketHandler.die();
		} else {
			debug("Connection failed earlier.");
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Print error messages.
	 * @param message
	 */
	// TODO Check if this method is used consistently.
	public void error(String message) {
		System.err.println(id + ": " + message);
	}

	/**
	 * Print debug messages.
	 * 
	 * @param message
	 */
	// TODO Check if this method is used consistently.
	public void debug(String message) {
		if (DEBUG) {
			System.out.println(id + ": " + message);
		}
	}

	/**
	 * Setter for t.
	 * @param t		The new value for t.
	 * @see NetwProg#t
	 */
	public void setT(int t) {
		this.t = t;
		debug("t is set to: " + t);
	}

	/**
	 * Getter for t.
	 * @return current value of t
	 * @see NetwProg#t
	 */
	public int getT() {
		return t;
	}

	/**
	 * The pretty print method.
	 */
	public String toString() {
		return "NetwProg " + id;
	}
}

// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

package gdp.erichiram.routables;

import gdp.erichiram.routables.gui.Gui;
import gdp.erichiram.routables.message.ChangeWeight;
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

/**
 * The main program of the application.
 * @author Hiram van Paassen, Eric Broersma
 */
public class NetwProg extends Observable{

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length > 0)
		{
			try {
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
				return;
			} catch (NumberFormatException e)
			{
				System.out.println("De input bestond niet alleen uit getallen!");
			}
		}
		
		System.out.println("Usage: NetwProg <process-id> (<process-id> <weight>)*");
	}
	
	private final boolean slave;
	
	/**
	 * Id for this node.
	 */
	public final int id;
	
	/**
	 * The initial neighbours of this node.
	 * Used to initialize with asymmetric weights.
	 */
	public final Map<Integer, Integer> neighboursToWeights;

	/**
	 * To bootstrap the channels we use a serverSocket to listen for incoming connections.
	 */
	private ServerSocket serverSocket;
	
	/**
	 * A map containing node ids and their respective channels.
	 */
	private final Map<Integer, Channel> idsToChannels = new ConcurrentHashMap<Integer, Channel>();
	
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

	private Gui gui;

	/**
	 * @param argId			id for this node
	 * @param neighboursToWeights	mapping of neighbours to distances
	 * @param slave			is this node running in slave (debugging) mode
	 */
	public NetwProg(int argId, Map<Integer, Integer> neighboursToWeights, boolean slave) {
		this.id = argId;
		this.neighboursToWeights = new ConcurrentHashMap<Integer, Integer>(neighboursToWeights);
		this.slave = slave;
		routingTable = new RoutingTable(this);
		gui = new Gui(this);
		
		messagesSent.addObserver(gui);
	}

	/**
	 * Run method for the NetwProg thread
	 */
	public void run() {
		// Setting up the GUI.
		debug("Starting GUI");
		SwingUtilities.invokeLater(gui);

		try {
			// Start a listening socket.
			debug("Starting sockets");
			serverSocket = new ServerSocket(id);

			// Try to connect to everyone with a higher id using startRepairConnection.
			for (int neighbour : neighboursToWeights.keySet()) {
				if (id < neighbour) {
					startRepairConnection(neighbour, neighboursToWeights.get(neighbour));
				}
			}
			
		} catch (IOException e) {
			// Most likely the port has already been taken, print an error message.
			error("Port is already taken");
		}
		
		// Listen and start sockets if needed.
		debug("Starting to listen");
		while (serverSocket != null && !serverSocket.isClosed()) {
			try {
				// Create a channel for every incoming connections.
				Socket clientSocket = serverSocket.accept();
				Channel channel = new Channel(this, clientSocket);
				
				// Add the channel to a map for easy access.
				idsToChannels.put(channel.getId(), channel);
				
				// Start the channel.
				new Thread(channel).start();
				setChanged();
				notifyObservers(idsToChannels.keySet());
				
			} catch (IOException e) {
				// The Socket just died while blocking during accept(), or
				// something else went wrong. We ignore the IOException and quit
				// the loop.
			}
		}

		// The Socket closed so we want to quit or we just crashed, either way we clean up and exit.
		for (Channel channel : idsToChannels.values()) {
			channel.close();
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
		Channel channel = idsToChannels.get(id);
		
		//We need to check if the channel is in the set
		//If it isn't or if its closing we ignore the message and continue
		//This is ok according to the netchange pdf
		if(channel != null)
		{
			channel.send(message);
		}
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
		
		if (idsToChannels.containsKey(id) ) {
			changeWeight(id, weight);
		} else {
			startRepairConnection(id, weight);
		}
	}

	/**
	 * Change the weight for a connection with a neighbour.
	 * @param id		The id for the node to change or repair the connection to.
	 * @param weight	The new weight value. 
	 */
	public void changeWeight(Integer id, Integer weight) {
		debug("Changing the weight to " + id + " to " + weight);
		routingTable.receive(new ChangeWeight(id, weight));
	}

	/**
	 * Repair (create) a new connection to a previously unconnected node with the specified weight.
	 * @param id		The id for the node to change or repair the connection to.
	 * @param weight	The new weight value. 
	 */
	public synchronized void startRepairConnection(int neighbour, int weight) {

		debug("Starting client channel for " + neighbour);
		Channel n = new Channel(this, neighbour, weight);
		idsToChannels.put(neighbour, n);
		new Thread(n).start();
		
		// Let our observers know we've changed.
		setChanged();
		notifyObservers(idsToChannels.keySet());
	}

	/**
	 * Fail (destroy) a connection with a neighbour.
	 * @param neighbour	The neighbour's id.
	 */
	public void failConnection(int neighbour) {
		debug("Failing connection to " + neighbour);
		
		// get the channel from the idsToChannels map.
		Channel channel = idsToChannels.get(neighbour);

		// Kill the channel.
		if ( channel != null) {
			channel.close();
		} else {
			debug("Connection failed earlier.");
		}
		
		idsToChannels.remove(neighbour);
		
		setChanged();
		notifyObservers(idsToChannels.keySet());
	}
	
	/**
	 * Print error messages.
	 * @param message
	 */
	public void error(String message) {
		System.err.println(id + ": " + message);
	}

	/**
	 * Print debug messages.
	 * 
	 * @param message
	 */
	public void debug(String message) {
		if (Configuration.printDebug) {
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

package gdp.erichiram.routables;

import gdp.erichiram.routables.message.ChangeWeight;
import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class contains the Netchange algorithm and the routing table.
 * @author Hiram van Paassen, Eric Broersma
 */
public class RoutingTable extends Observable {
	
	/**
	 * -1 is an illegal port number so we use it to signify an undefined node id.
	 */
	public static final int UNDEF_ID = -1;
	
	/**
	 * There is a maximum of 20 nodes, and the maximum distance between 2 nodes is 1000.
	 * The longest path in the network is 19 hops, the longest possible path has length 19000.
	 */
	public static final int MAX_DIST = 19001;

	/**
	 * Reference to the main program.
	 */
	private final NetwProg netwProg;

	/**
	 * All nodes in the network.
	 */
	private final CopyOnWriteArraySet<Integer> nodes = new CopyOnWriteArraySet<Integer>();

	/**
	 * All neighbours to this node.
	 */
	private final ConcurrentHashMap<Integer, Integer> neighboursToWeight = new ConcurrentHashMap<Integer, Integer>(22,1,22);

	/**
	 * Preferred neighbours for nodes.
	 * 
	 * NB.get(a) is the preferred neighbour for a.
	 */
	private final ConcurrentHashMap<Integer, Integer> NB = new ConcurrentHashMap<Integer, Integer>(22,1,22);

	/**
	 * Distance estimates for this node to certain nodes.
	 * 
	 * D.get(a) estimates the distance between this node and node a.
	 */
	private final ConcurrentHashMap<Integer, Integer> D = new ConcurrentHashMap<Integer, Integer>(22,1,22);

	/**
	 * Distance estimates for certain nodes to certain nodes.
	 * 
	 * ndis.get(a).get(b) estimates the distance between nodes a and b.
	 */
	private final ConcurrentHashMap<Integer, Map<Integer, Integer>> ndis = new ConcurrentHashMap<Integer, Map<Integer, Integer>>();

	/**
	 * Producer consumer queue. Channels put messages in the queue and
	 * RoutingTable processes them asynchronously. This should lead to less
	 * waiting, since the processing of one incoming message won't block the
	 * processing of another incoming message.
	 */
	private final BlockingQueue<Message> q = new LinkedBlockingQueue<Message>();
	
	/**
	 * Initialize the routing table.
	 * 
	 * @param netwProg
	 */
	public RoutingTable(NetwProg netwProg) {
		this.netwProg = netwProg;

		nodes.add(netwProg.id);
		ndis.put(netwProg.id, D);
		setNodeData(netwProg.id, netwProg.id, 0);
		
		// Start a consumer thread to process messages for this RoutingTable.
		new Thread(new Runnable(){
			public void run() {
				while (true) {
					try {
						
						// Get the next message, if any.
						Message message = q.take();

						// Process the specific message in a way that makes sense.
						if (message instanceof MyDist) {
							MyDist mydist = (MyDist) message;
							mydist(mydist.from, mydist.id, mydist.distance);
						} else if (message instanceof Repair) {
							Repair repair = (Repair) message;
							repair(repair.neighbour, repair.weight);
						} else if (message instanceof Fail) {
							Fail fail = (Fail) message;
							fail(fail.neighbour);
						} else if (message instanceof ChangeWeight) {
							ChangeWeight cw = (ChangeWeight) message;
							changeWeight(cw.node, cw.weight);
						}
					} catch (InterruptedException e) {
						// TODO: what's happening here?
					}
				}
			}
		}).start();
	}
	
	// TODO Add javadoc! And maybe the method name should be more prescriptive of what the method does exactly.
	private void checkNodeInitialized(int node)
	{
		// If we don't know the node.
		if (!nodes.contains(node)) {
			
			// Add the node.
			nodes.add(node);
			ndis.put(node, new HashMap<Integer, Integer>());

			// Fill the ndis table.
			for (int v : nodes) {
				ndis.get(node).put(v, MAX_DIST);
				ndis.get(v).put(node, MAX_DIST);
			}

			// Try to set node data.
			setNodeData(node, UNDEF_ID, MAX_DIST);
		}
	}

	/**
	 * Recompute the data for a node, as described in the Netchange algorithm by Tajibnapis.
	 * @param node id of the node to be recomputed (called v in the paper)
	 */
	private synchronized void recompute(int node) {

		boolean distancedChanged = false;
		
		if (node == netwProg.id) {
			// Try to change our preferred neighbour table.
			distancedChanged = setNodeData(node, netwProg.id, 0);
		} else {
			// Determine the closest neighbour to node and its distance.
			int estimatedDistance = MAX_DIST;
			int preferredNeighbour = UNDEF_ID;
			for (int neighbour : neighboursToWeight.keySet()) {
				
				// distance(this, v) = distance(this, w) + distance(w, v)
				int newDistance = neighboursToWeight.get(neighbour) + ndis.get(neighbour).get(node);
				
				// If we get a better distance.
				if (newDistance < estimatedDistance) {
					estimatedDistance = newDistance;
					preferredNeighbour = neighbour;
				}
			}

			// Try to change our preferred neighbour table.
			distancedChanged = setNodeData(node, preferredNeighbour, estimatedDistance);
		}
		
		// If we know something new, inform our neighbours of the fact.
		if (distancedChanged) {
			for (int x : neighboursToWeight.keySet()) {
				netwProg.send(x, new MyDist(node, D.get(node)));
			}
		}

	}
	
	/**
	 * Receive a message.
	 * @param message the received message
	 */
	public void receive(Message message)
	{
		q.offer(message);
	}

	/**
	 * Process the contents of a MyDist message, as described in the Netchange algorithm by Tajibnapis.
	 * @param from
	 * @param to
	 * @param distance
	 */
	private void mydist(int from, int to, int distance) {
		checkNodeInitialized(from);
		checkNodeInitialized(to);

		netwProg.debug("Processing mydist from: " + from + " for: " + to + " with distance: " + distance);

		ndis.get(from).put(to, distance);
		recompute(to);
	}

	/**
	 * Process the contents of a Fail message, as described in the Netchange algorithm by Tajibnapis.
	 * @param neighbour id of the failed channel
	 */
	private void fail(int neighbour) {
		netwProg.debug("Processing fail from: " + neighbour);

		neighboursToWeight.remove(neighbour);

		for(int n : NB.keySet())
		{
			if(NB.get(n) == neighbour)
				recompute(n);
		}
	}

	/**
	 * Process the contents of a Repair message, as described in the Netchange algorithm by Tajibnapis.
	 * @param neighbour id of the channel to be repaired
	 * @param weight	weight of the channel
	 */
	private void repair(int neighbour, int weight) {
		netwProg.debug("Processing repair to: " + neighbour + " with weight: " + weight);

		//we do lazy initialization so we check now
		checkNodeInitialized(neighbour);

		neighboursToWeight.put(neighbour, weight);

		for (int v : nodes) {
			netwProg.send(neighbour, new MyDist(v, D.get(v)));
		}
	}

	// TODO see if we can merge this one with RoutingTable.repair(int,int), cause of the similarities
	// @see #repair(int, int)
	private void changeWeight(int node, int weight) {
		neighboursToWeight.put(node, weight);

		for (int v : nodes) {
			recompute(v);
		}
	}

	/**
	 * Returns all node data.
	 * 
	 * @return An array of Integer[3] arrays each containing a node id, its
	 *         preferred neighbour and the distance of the shortest path to the
	 *         node id from this node.
	 */
	public synchronized Integer[][] getNodesData() {
		Integer[][] result = new Integer[NB.size()][];
		int i = 0;
		for (int n : NB.keySet()) {
			synchronized (D) {
				result[i++] = new Integer[] { n, NB.get(n), D.get(n) };
			}
		}

		return result;
	}

	/**
	 * Sets D and NB together to avoid inconsistencies. We also notify the GUI
	 * that something has changed and tell it what has changed.
	 * 
	 * @param node	id of the node for which the data is set
	 * @param preferredNeighbour id of the preferred neighbour for the node.
	 * @param distance distance to the node
	 * @return true if the distance in NB changed, false otherwise
	 */
	// TODO: deze methode is niet meer synchronised, is het dan nog nodig om
	// "Sets D and NB together to avoid inconsistencies"?
	private boolean setNodeData(int node, int preferredNeighbour, Integer distance)
	{	
		NB.put(node, preferredNeighbour);
		boolean changed = !distance.equals(D.put(node, distance));

		if (changed) {
			setChanged();
			notifyObservers(new Integer[] { node, preferredNeighbour, distance });
		}
		return changed;
	}
}

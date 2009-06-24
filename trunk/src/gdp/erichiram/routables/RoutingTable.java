package gdp.erichiram.routables;

import gdp.erichiram.routables.message.MyDist;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class contains the Netchange algorithm and the routing table.
 * 
 * @author hiram, eric
 * 
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
	 * Initialize the routing table.
	 * 
	 * @param netwProg
	 */
	public RoutingTable(NetwProg netwProg) {
		this.netwProg = netwProg;

		nodes.add(netwProg.id);
		ndis.put(netwProg.id, D);
		setDataForNode(netwProg.id, netwProg.id, 0);
	}
	
	private void checkNodeInitialized(int node)
	{
		// If we don't know the node.
		if (!nodes.contains(node)) {
			
			// Add the node.
			nodes.add(node);
			Object x = ndis.put(node, new HashMap<Integer, Integer>());
			if (x != null)
				throw new RuntimeException("cannot be initialized already");

			for (int v : nodes) {
				ndis.get(node).put(v, MAX_DIST);
				ndis.get(v).put(node, MAX_DIST);
			}

			setDataForNode(node, UNDEF_ID, MAX_DIST);
		}
	}

	private synchronized void recompute(int v) {

		boolean dChanged = false;
		if (v == netwProg.id) {
			dChanged = setDataForNode(v, netwProg.id, 0);
		} else {
			int d = MAX_DIST;
			int w = UNDEF_ID;
			//determine closest neighbour to v and its distance to v
			for (int s : neighboursToWeight.keySet()) {
				
				//distance( me , v ) = distance( me , w ) + distance( w , v )
				int i = neighboursToWeight.get(s) + ndis.get(s).get(v);
				
				if (i <= d) {
					d = i;
					w = s;
				}
			}
			
			if (d < MAX_DIST) {
				dChanged = setDataForNode(v, w, d);
			} else {
				dChanged = setDataForNode(v, UNDEF_ID, MAX_DIST);
			}
		}
		if (dChanged) {
			for (int x : neighboursToWeight.keySet()) {
				netwProg.send(x, new MyDist(v, D.get(v)));
			}
		}

	}

	public synchronized void receive(MyDist myDist) {
		checkNodeInitialized(myDist.from);
		checkNodeInitialized(myDist.id);

		netwProg.debug("Processing mydist: " + myDist+ ".");

		ndis.get(myDist.from).put(myDist.id, myDist.distance);
		recompute(myDist.id);
	}


	public synchronized void fail(int neighbour) {
		netwProg.debug("Processing fail from: " + neighbour);

		neighboursToWeight.remove(neighbour);

		for(int n : NB.keySet())
		{
			if(NB.get(n) == neighbour)
				recompute(n);
		}
	}

	public synchronized void repair(int neighbour, int weight) {
		netwProg.debug("Processing repair to: " + neighbour + " with weight: " + weight);

		//we do lazy initialization so we check now
		checkNodeInitialized(neighbour);

		neighboursToWeight.put(neighbour, weight);

		for (int v : nodes) {
			netwProg.send(neighbour, new MyDist(v, D.get(v)));
		}
	}


	public synchronized void changeWeight(int node, int weight) {
		neighboursToWeight.put(node, weight);

		for (int v : nodes) {
			recompute(v);
		}

		notifyObservers();
	}
	
	/**
	 * private method to set D and ND together to avoid inconsistencies
	 * @param n node for which the data is set
	 * @param preferred preferred neighbour for the node.
	 * @param dist distance to the node
	 * @return is the distance changed?
	 */
	private boolean setDataForNode(int n, int preferred, Integer dist)
	{	
		NB.put(n, preferred);
		boolean ret = !dist.equals(D.put(n, dist));

		if (ret) {
			setChanged();
			notifyObservers(new Integer[] { n, preferred, dist });
		}
		return ret;
	}
}

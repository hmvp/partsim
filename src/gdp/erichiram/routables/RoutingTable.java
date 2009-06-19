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
	private static final int UNDEF_ID = -1;
	private static final int MAX_DIST = 20001;

	private final NetwProg netwProg;

	/**
	 * All nodes in the network.
	 */
	private final CopyOnWriteArraySet<Integer> nodes = new CopyOnWriteArraySet<Integer>();

	/**
	 * All neighbours to this node.
	 */
	private final ConcurrentHashMap<Integer, Integer> neighboursToWeight = new ConcurrentHashMap<Integer, Integer>();

	/**
	 * Preferred neighbours for nodes.
	 * 
	 * NB.get(a) is the preferred neighbour for a.
	 */
	private final HashMap<Integer, Integer> NB = new HashMap<Integer, Integer>();

	/**
	 * Distance estimates for this node to certain nodes.
	 * 
	 * D.get(a) estimates the distance between this node and node a.
	 */
	private final HashMap<Integer, Integer> D = new HashMap<Integer, Integer>();

	/**
	 * Distance estimates for certain nodes to certain nodes.
	 * 
	 * ndis.get(a).get(b) estimates the distance between nodes a and b.
	 */
	private final HashMap<Integer, Map<Integer, Integer>> ndis = new HashMap<Integer, Map<Integer, Integer>>();

	/**
	 * Initialize the routing table.
	 * 
	 * @param netwProg
	 */
	public RoutingTable(NetwProg netwProg) {
		this.netwProg = netwProg;

		nodes.add(netwProg.id);
		ndis.put(netwProg.id, D);
		D.put(netwProg.id, 0);
		NB.put(netwProg.id, netwProg.id);
	}
	
	private void checkNodeInitialized(int node)
	{
		//if(node == netwProg.id)
			//return;

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

			D.put(node, MAX_DIST);
			NB.put(node, UNDEF_ID);
		}
	}

	public synchronized void recompute(int v) {
		checkNodeInitialized(v);

		boolean dChanged = false;
		if (v == netwProg.id) {
			dChanged = 0 != D.put(v, 0);
			NB.put(v, netwProg.id);
		} else {
			int min = MAX_DIST;
			int w = 0;
			//determine closest neighbour to v and its distance to v
			for (int s : neighboursToWeight.keySet()) {
				int x = s;
				int i = ndis.get(x).get(v);
				if (i <= min) {
					min = i;
					w = s;
				}
			}

			//distance( me , v ) = distance( me , w ) + distance( w , v )
			int	d = neighboursToWeight.get(w) + min;

			if (d < MAX_DIST) {
				dChanged = d != D.put(v, d);
				NB.put(v, w);
			} else {
				dChanged = MAX_DIST != D.put(v, MAX_DIST);
				NB.put(v, UNDEF_ID);
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

		ndis.get(myDist.from).put(myDist.id, myDist.distance);
		recompute(myDist.id);

		notifyObservers();
	}


	public synchronized void fail(int neighbour) {
		netwProg.debug("Processing fail from: " + neighbour);

		neighboursToWeight.remove(neighbour);

		for(int n : NB.keySet())
		{
			if(NB.get(n) == neighbour)
				recompute(n);
		}

		notifyObservers();
	}

	public synchronized void repair(int neighbour, int weight) {
		netwProg.debug("Processing repair to: " + neighbour + " with weight: " + weight);

		//we do lazy initialization so we check now
		checkNodeInitialized(neighbour);

		neighboursToWeight.put(neighbour, weight);

		for (int v : nodes) {
			netwProg.send(neighbour, new MyDist(v, D.get(v)));
		}

		notifyObservers();
	}

	/**
	 * This override prevents us from having to call setChanged() every time we call notifyObservers().
	 * 
	 * @see java.util.Observable#notifyObservers()
	 */
	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	public synchronized void changeWeight(int node, int weight) {
		neighboursToWeight.put(node, weight);

		for (int v : nodes) {
			recompute(v);
		}

		notifyObservers();
	}

	public synchronized int[][] getNodesData() {
		int[][] result = new int[nodes.size()][];
		int i = 0;
		for (int n : nodes) {
			result[i++] = new int[] { n, NB.get(n), D.get(n) };
		}

		return result;
	}
}

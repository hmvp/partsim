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
		setDataForNode(netwProg.id, netwProg.id, 0);
		
		new Thread(new Runnable(){
			public void run() {
				Message m;
				while (true) {
					try {
						m = q.take();

						if (m instanceof MyDist) {
							MyDist mydist = (MyDist) m;
							mydist(mydist.from, mydist.id, mydist.distance);
						} else if (m instanceof Repair) {
							Repair repair = (Repair) m;
							repair(repair.neighbour, repair.weight);
						} else if (m instanceof Fail) {
							Fail fail = (Fail) m;
							fail(fail.neighbour);
						} else if (m instanceof ChangeWeight) {
							ChangeWeight cw = (ChangeWeight) m;
							changeWeight(cw.node, cw.weight);
						}
					} catch (InterruptedException e1) {
					}
				}
			}
		}).start();
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

	/**
	 * Recompute the data for a node, as described in the Netchange algorithm by Tajibnapis.
	 * @param node id of the node to be recomputed
	 */
	private synchronized void recompute(int node) {

		boolean dChanged = false;
		if (node == netwProg.id) {
			dChanged = setDataForNode(node, netwProg.id, 0);
		} else {
			int d = MAX_DIST;
			int w = UNDEF_ID;
			//determine closest neighbour to v and its distance to v
			for (int s : neighboursToWeight.keySet()) {
				
				//distance( me , v ) = distance( me , w ) + distance( w , v )
				int i = neighboursToWeight.get(s) + ndis.get(s).get(node);
				
				if (i <= d) {
					d = i;
					w = s;
				}
			}
			
			if (d < MAX_DIST) {
				dChanged = setDataForNode(node, w, d);
			} else {
				dChanged = setDataForNode(node, UNDEF_ID, MAX_DIST);
			}
		}
		if (dChanged) {
			for (int x : neighboursToWeight.keySet()) {
				netwProg.send(x, new MyDist(node, D.get(node)));
			}
		}

	}
	
	/**
	 * Receive a message.
	 * @param m the received message
	 */
	public void receive(Message m)
	{
		q.offer(m);
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


	private void changeWeight(int node, int weight) {
		neighboursToWeight.put(node, weight);

		for (int v : nodes) {
			recompute(v);
		}

		notifyObservers();
	}
	
	/**
	 * the first time the gui needs data it needs to get it all
	 * @return
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
	 * private method to set D and ND together to avoid inconsistencies
	 * we also notify the gui that something has changed and tell it what has changed
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

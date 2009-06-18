package gdp.erichiram.routables;

import gdp.erichiram.routables.message.MyDist;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class contains the Netchange algorithm and the routing table.
 * 
 * @author hiram, eric
 *
 */
public class RoutingTable extends Observable{
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
	private final CopyOnWriteArraySet<Integer> neighbours = new CopyOnWriteArraySet<Integer> ();

	/**
	 * Preferred neighbours for nodes.
	 * 
	 * d.get(a) is the preferred neighbour for a.
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
	private final HashMap<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	
	/**
	 * Initialize the routing table.
	 * 
	 * @param netwProg
	 */
	public RoutingTable(NetwProg netwProg) {
		this.netwProg = netwProg;
		
		//ndis.put(netwProg.id, D);
		D.put(netwProg.id, 0);
		NB.put(netwProg.id, netwProg.id);
	}
	
	private void checkNodeInitialized(int node)
	{
		if(node == netwProg.id)
			return;
		
		if(!nodes.contains(node))
		{
			nodes.add(node);
			Object x = ndis.put(node, new HashMap<Integer, Integer>());
			if (x != null)
				throw new RuntimeException("cannot be initialized already");
				
			for(int v : nodes)
			{
				ndis.get(node).put(v, MAX_DIST);
				ndis.get(v).put(node, MAX_DIST);
			}
			
			D.put(node, MAX_DIST);
			NB.put(node, UNDEF_ID);
		}
	}
	
	public synchronized void recompute(int v) {	
		checkNodeInitialized(v);
		
		boolean dChanged;
		if (v == netwProg.id)
		{
			dChanged = 0 != D.put(v, 0);
			NB.put(v,netwProg.id);
		}
		else
		{
			int min = MAX_DIST;
			int w = 0;
			for(int s : neighbours)
			{	
				int x = s;
				int i = ndis.get(x).get(v);
					if(i <= min)
					{
						min = i;
						w = s;
					}
			}
			
			//TODO: soms ontvangen we een MyDist voordat we een repair hebben ontvangen. In dat geval is w UNDEFINED en gaat alles dood
			// dit kan natuurlijk onder goede omstandigheden nooit gebeuren!
			Integer d = MAX_DIST;
			if (w != 0)
				d= D.get(w) + min;
			
			if (d < MAX_DIST)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w);
			}
			else
			{
				dChanged = MAX_DIST != D.put(v, MAX_DIST);
				NB.put(v,UNDEF_ID);
			}
		}
		if(dChanged)
		{
			for(int x : neighbours)
			{
				netwProg.send(x, new MyDist(v,D.get(v)));
			}
		}
		
	}
	
	public synchronized void receive(MyDist myDist) {
		checkNodeInitialized(myDist.from);
				
		ndis.get(myDist.from).put(myDist.id,myDist.distance);
		recompute(myDist.id);
		
		notifyObservers();
	}
	
	public synchronized void fail(Neighbour n) {
		netwProg.debug("Processing fail from: " + n.id);
		
		neighbours.remove(n.id);
		//D.put(n.id, MAX);
		
//		for(int neighbour : NB.keySet())
//		{
//			if(NB.get(neighbour) == n.id)
//				recompute(neighbour);
//		}
		
		for(int v : nodes)
		{
			recompute(v);
		}
		
		notifyObservers();
	}
	
	public synchronized void repair(int neighbour, int weight) {	
		netwProg.debug("Processing repair to: " + neighbour + " with weight: "+ weight);

		checkNodeInitialized(neighbour);
		
		neighbours.add(neighbour);
		//D.put(neighbour, weight);
		
		for(int v : nodes)
		{
			ndis.get(neighbour).put(v,MAX_DIST);
			
			if(v == neighbour)
			{
				netwProg.send(neighbour, new MyDist(v,weight));
			}
			else
			{
				netwProg.send(neighbour, new MyDist(v,D.get(v)));
			}
		}
		notifyObservers();
	}
	
	/**
	 * @see java.util.Observable#notifyObservers()
	 */
	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	public synchronized void changeWeight(int node, int weight) {
		D.put(node, weight);
		//ndis.get(LOCAL).put(node, weight);
		
		for(int v : nodes)
		{
			recompute(v);
		}
		
		notifyObservers();
	}
	
	public synchronized int[] getMetricsForNode(int n)
	{
		return new int[]{NB.get(n),D.get(n)};
	}
	
	public synchronized int[][] getNodes()
	{
		int[][] result = new int[nodes.size()][];
		int i = 0;
		for (int n : nodes)
		{
			result[i++] = new int[]{n, NB.get(n),D.get(n)};
		}
		
		return result;
	}
}

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
	private static final int UNDEFINED = -1;
	private static final Integer MAX_ID = 20001;
	
	private final NetwProg netwProg;

	/**
	 * All nodes in the network.
	 */
	private final CopyOnWriteArraySet<Integer> nodes = new CopyOnWriteArraySet<Integer>();
	
	/**
	 * All neighbours to this node.
	 */
	private final CopyOnWriteArraySet<Integer> neighbours = new CopyOnWriteArraySet<Integer> ();
	
	private final HashMap<Integer, Integer> D = new HashMap<Integer, Integer>();
	private final HashMap<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private final HashMap<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
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
//			if (x != null)
//				throw new RuntimeException("cannot be initialized already");
				
			for(int v : nodes)
			{
				ndis.get(node).put(v, MAX_ID);
				ndis.get(v).put(node, MAX_ID);
			}
			
			D.put(node, MAX_ID);
			NB.put(node, UNDEFINED);
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
			int min = MAX_ID;
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
			Integer d = MAX_ID;
			if (w != 0)
				d= D.get(w) + min;
			
			if (d < MAX_ID)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w);
			}
			else
			{
				Integer oldD = D.put(v, MAX_ID);
				dChanged = MAX_ID != oldD;
				if(oldD == null)
					throw new RuntimeException("Something is completely wrong.");
				NB.put(v,UNDEFINED);
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
		D.put(neighbour, weight);
		
		for(int v : nodes)
		{
			ndis.get(neighbour).put(v,MAX_ID);
			
			netwProg.send(neighbour, new MyDist(v,D.get(v)));
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
	
	public synchronized int[] getNodes()
	{
		int[] result = new int[nodes.size()];
		int i = 0;
		for (int n : nodes)
		{
			result[i++] = n;
		}
		
		return result;
	}
}

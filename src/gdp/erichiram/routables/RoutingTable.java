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
public class RoutingTable extends Observable{
	private static final int UNDEF = 0;
	private static final Integer MAX = 20001;
	private final int LOCAL;
	private final ConcurrentHashMap<Integer, Neighbour> neighbours = new ConcurrentHashMap<Integer, Neighbour> ();
	private final HashMap<Integer, Integer> D = new HashMap<Integer, Integer>();
	private final CopyOnWriteArraySet<Integer> nodes = new CopyOnWriteArraySet<Integer>();
	private final HashMap<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private final NetwProg netwProg;
	private final HashMap<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
	public RoutingTable(NetwProg netwProg) {
		this.netwProg = netwProg;
		this.LOCAL = netwProg.id;
		
		
		//ndis.put(LOCAL, D);
		D.put(LOCAL, 0);
		NB.put(LOCAL, LOCAL);
	}
	
	private void checkNodeInitialized(int node)
	{
		if(node == LOCAL)
			return;
		
		if(!nodes.contains(node))
		{
			nodes.add(node);
			Object x = ndis.put(node, new HashMap<Integer, Integer>());
			if (x != null)
				;//throw new RuntimeException("cannot be initialized already");
				
			for(int v : nodes)
			{
				ndis.get(node).put(v, MAX);
				ndis.get(v).put(node, MAX);
			}
			
			D.put(node, MAX);
			NB.put(node, UNDEF);
		}
	}
	
	public synchronized void recompute(int v) {	
		checkNodeInitialized(v);
		
		boolean dChanged;
		if (v == netwProg.id)
		{
			dChanged = 0 != D.put(v, 0);
			NB.put(v,LOCAL);
		}
		else
		{
			int min = MAX;
			Neighbour w = null;
			for(Neighbour s : neighbours.values())
			{	
				int x = s.id;
				int i = ndis.get(x).get(v);
					if(i <= min)
					{
						min = i;
						w = s;
					}
			}
			
			//TODO: soms ontvangen we een MyDist voordat we een repair hebben ontvangen. In dat geval is w UNDEF en gaat alles dood
			// dit kan natuurlijk onder goede omstandigheden nooit gebeuren!
			Integer d = MAX;
			if (w != null)
				d= D.get(w.id) + min;
			
			if (d < MAX)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w.id);
			}
			else
			{
				Integer oldD = D.put(v, MAX);
				dChanged = MAX != oldD;
				if(oldD == null)
					throw new RuntimeException("fucked");
				NB.put(v,UNDEF);
			}
		}
		if(dChanged)
		{
			for(Neighbour x : neighbours.values())
			{
				x.send(new MyDist(v,D.get(v)));
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
	
	public synchronized void repair(Neighbour n, int weight) {	
		netwProg.debug("Processing repair to: " + n.id + " with weight: "+ weight);

		checkNodeInitialized(n.id);
		
		neighbours.put(n.id,n);
		D.put(n.id, weight);
		
		for(int v : nodes)
		{
			ndis.get(n.id).put(v,MAX);
			
			n.send(new MyDist(v,D.get(v)));
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

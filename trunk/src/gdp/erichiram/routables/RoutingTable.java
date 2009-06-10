package gdp.erichiram.routables;

import gdp.erichiram.routables.message.MyDist;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;

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
	public final Map<Neighbour, Integer> neighbours;
	HashMap<Integer, Integer> D = new HashMap<Integer, Integer>();
	Collection<Integer> nodes = new HashSet<Integer>();
	HashMap<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private NetwProg netwProg;
	public HashMap<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
	public RoutingTable(NetwProg netwProg, Map<Neighbour, Integer> neighbours) {
		this.neighbours = neighbours;
		this.netwProg = netwProg;
		this.LOCAL = netwProg.id;
		
		
		//ndis.put(LOCAL, D);
		D.put(LOCAL, 0);
		NB.put(LOCAL, LOCAL);
	}
	
	private void checkNodeInitialized(int node)
	{
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
			for(Neighbour s : neighbours.keySet())
			{	
				int x = s.getPort();
				int i = ndis.get(x).get(v);
					if(i <= min)
					{
						min = i;
						w = s;
					}
			}
			
			//TODO: soms ontvangen we een MyDist voordat we een repair hebben ontvangen. In dat geval is w UNDEF en gaat alles dood
			// dit kan natuurlijk onder goede omstandigheden nooit gebeuren!
			Integer d = D.get(w.getPort()) + min;
			
			if (d < MAX)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w.getPort());
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
			for(Neighbour x : neighbours.keySet())
			{
				x.send(new MyDist(v,D.get(v)));
			}
		}
		
	}
	
	public synchronized void receive(MyDist myDist) {
		checkNodeInitialized(myDist.from);
		checkNodeInitialized(myDist.id);
				
		ndis.get(myDist.from).put(myDist.id,myDist.distance);
		recompute(myDist.id);
		
		notifyObservers();
	}
	
	public synchronized void fail(Neighbour n) {
		neighbours.remove(n);
		
		for(int neighbour : NB.keySet())
		{
			if(NB.get(neighbour) == n.getPort())
				recompute(neighbour);
		}
		
		notifyObservers();
	}
	
	public synchronized void repair(Neighbour n, int weight) {		
		checkNodeInitialized(n.getPort());
		
		neighbours.put(n, weight);
		D.put(n.getPort(), weight);
		
		for(int v : nodes)
		{
			ndis.get(n.getPort()).put(v,MAX);
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
	
	
}

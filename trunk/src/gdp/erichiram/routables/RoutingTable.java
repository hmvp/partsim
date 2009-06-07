package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

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
	private static final Integer MAX = Integer.MAX_VALUE;
	private final int LOCAL;
	public final Map<Integer, SocketHandler> socketHandlers;
	private HashMap<Integer, Integer> D = new HashMap<Integer, Integer>();
	private Collection<Integer> nodes = new HashSet<Integer>();
	Map<Integer,Integer> neighbours = new HashMap<Integer, Integer>();
	HashMap<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private NetwProg netwProg;
	public HashMap<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
	public RoutingTable(NetwProg netwProg, Map<Integer, SocketHandler> socketHandlers) {
		this.socketHandlers = socketHandlers;
		this.netwProg = netwProg;
		this.LOCAL = netwProg.id;
		
		
		ndis.put(LOCAL, D);
		D.put(LOCAL, 0);
		NB.put(LOCAL, LOCAL);
	}

	@Deprecated
	public void initialize() {
		for(int w: neighbours.keySet())
		{
			ndis.put(w, new HashMap<Integer, Integer>());

			for(int v : nodes)
			{
				ndis.get(w).put(v, MAX);
			}
		}
		
		for(int v : nodes)
		{
			D.put(v, MAX);
			NB.put(v, UNDEF);
		}
		
		D.put(LOCAL, 0);
		NB.put(LOCAL, LOCAL);

		for (int w : neighbours.keySet())
		{
			send(w, new MyDist(netwProg.id,0));
		}
		setChanged();
	}
	
	private void initializeNode(int node)
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
			int w = v;
			for(int x : neighbours.keySet())
			{	
					min = Math.min(ndis.get(x).get(v), min);
					w = x;
			}
			
			Integer d = neighbours.get(w) + min;
			
			if (d < MAX)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w);
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
			for(int x : neighbours.keySet())
			{
				send(x, new MyDist(v,D.get(v)));
			}
		}
		
	}
	
	private synchronized void receive(MyDist myDist) {
		if(!nodes.contains(myDist.from))
		{
			initializeNode(myDist.from);
		}
		if(!nodes.contains(myDist.id))
		{
			initializeNode(myDist.id);
		}
		
		Map<Integer, Integer> X = ndis.get(myDist.from);
		X.put(myDist.id,myDist.distance);
		recompute(myDist.id);
		setChanged();
		notifyObservers();
	}
	
	private synchronized void receive(Fail fail) {
		neighbours.remove(fail.neighbour);
		
		for(int neighbour : NB.keySet())
		{
			if(NB.get(neighbour) == fail.neighbour && neighbours.containsKey(neighbour))
				recompute(neighbour);
		}
		setChanged();
		notifyObservers();
	}
	
	private synchronized void receive(Repair repair) {
		int neighbour = repair.neighbour;
		
		if(!nodes.contains(neighbour))
			initializeNode(neighbour);
		
		
		neighbours.put(neighbour, repair.weight);
		
		for(int v : nodes)
		{
			ndis.get(neighbour).put(v,MAX);
			send(neighbour, new MyDist(v,D.get(v)));
		}
		setChanged();
		notifyObservers();
	}

	public synchronized void send(int destination, Message message) {
		message.from = netwProg.id;
		message.to = destination;
		
		// to send the message <mydist, 1103, 2> to neighbour 1101, do:
		// socketHandlers.get(1101).send(new MyDist(1103, 2));
		
		// if the destination is a neighbour
		if(destination == netwProg.id)
		{
			//receive(message);
		}
		
		
		if (socketHandlers.containsKey(destination)) {
			// send the message to the neighbour
			socketHandlers.get(destination).send(message);
			
			// increment the total number of sent messages
			netwProg.messagesSent.increment();
		} else {
			throw new RuntimeException(netwProg.id + ": Message destination is undefined. Tried to send message to: " + destination);
			//System.err.println(netwProg.id + ": Message destination is undefined. Tried to send message to: " + destination);
		}

		
	}

	public void receive(Message message) {
		if (message instanceof MyDist) {
			receive((MyDist)message);
		} else if (message instanceof Repair) {
			receive((Repair)message);
		} else if (message instanceof Fail) {
			receive((Fail)message);
		}
	}
}

package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * This class contains the Netchange algorithm and the routing table.
 * 
 * @author hiram, eric
 *
 */
public class RoutingTable extends Observable{
	private static final Integer UNDEF = 0;
	public final Map<Integer, SocketHandler> socketHandlers;
	private Map<Integer, Integer> D = new HashMap<Integer, Integer>();
	private Collection<Integer> nodes;
	Map<Integer,Integer> neighbours;
	private Map<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private NetwProg netwProg;
	public Map<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
	public RoutingTable(NetwProg netwProg, Map<Integer, SocketHandler> socketHandlers, Map<Integer,Integer> neighbours) {
		this.neighbours = neighbours;
		this.nodes = neighbours.keySet();
		this.socketHandlers = socketHandlers;
		this.netwProg = netwProg;
		
		//initialize();
	}

	public void initialize() {
		for(int w: neighbours.keySet())
		{
			ndis.put(w, new HashMap<Integer, Integer>());

			for(int v : nodes)
			{
				ndis.get(w).put(v, Integer.MAX_VALUE);
			}
		}
		
		for(int v : nodes)
		{
			D.put(v, Integer.MAX_VALUE);
			NB.put(v, UNDEF);
		}
		D.put(netwProg.id, 0);
		NB.put(netwProg.id, netwProg.id);

		for (int w : neighbours.keySet())
		{
			send(w, new MyDist(netwProg.id,0));
		}
		setChanged();
	}
	
	public synchronized void recompute(int v) {		
		boolean dChanged;
		if (v == netwProg.id)
		{
			dChanged = 0 != D.put(v, 0);
			NB.put(v,netwProg.id);
		}
		else
		{
			int min = Integer.MAX_VALUE;
			int w = v;
			for(int x : neighbours.keySet())
			{
					if(!ndis.containsKey(x))
						ndis.put(x, new HashMap<Integer, Integer>());

					if(!ndis.get(x).containsKey(v))
						ndis.get(x).put(v, Integer.MAX_VALUE);
				
					min = Math.min(ndis.get(x).get(v), min);
					w = x;
			}
			
			Integer d = neighbours.get(w) + min;
			
			if (d < Integer.MAX_VALUE)
			{
				dChanged =  d != D.put(v, d);
				NB.put(v,w);
			}
			else
			{
				Integer oldD = D.put(v, Integer.MAX_VALUE);
				dChanged = Integer.MAX_VALUE != oldD && oldD != null;
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
		if(!ndis.containsKey(myDist.from))
		{
			nodes.add(myDist.from);
			ndis.put(myDist.from, new HashMap<Integer, Integer>());
		}
		Map<Integer, Integer> X = ndis.get(myDist.from);
		X.put(myDist.id,myDist.distance);
		recompute(myDist.id);
		setChanged();
		notifyObservers();
	}
	
	private synchronized void receive(Fail fail) {
		neighbours.remove(fail.neighbour);
		
		for(int neighbour : nodes)
		{
			recompute(neighbour);
		}
		setChanged();
		notifyObservers();
	}
	
	private synchronized void receive(Repair repair) {
		neighbours.put(repair.neighbour, repair.weight);
		nodes.add(repair.neighbour);
		
		for(int neighbour : nodes)
		{
			ndis.get(repair.neighbour).put(neighbour,Integer.MAX_VALUE);
			send(repair.neighbour, new MyDist(neighbour,D.get(neighbour)));
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
		if(NB.get(destination) == netwProg.id)
		{
			receive(message);
		}
		
		if ( socketHandlers.containsKey(NB.get(destination)) ) {
			// send the message to the neighbour
			socketHandlers.get(NB.get(destination)).send(message);

			// increment the total number of sent messages
			netwProg.messagesSent.increment();
		} else {
			if (socketHandlers.containsKey(destination) && NB.get(destination) == UNDEF) {
				// send the message to the neighbour
				socketHandlers.get(destination).send(message);
				
				// increment the total number of sent messages
				netwProg.messagesSent.increment();
			} else {
				System.err.println("something wrong! tried to send to: "+ NB.get(destination) + " for: " + destination);
			}
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

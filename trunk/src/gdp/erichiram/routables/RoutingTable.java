package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

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
	Set<Integer> neighbours;
	private Map<Integer, Map<Integer,Integer>> ndis = new HashMap<Integer, Map<Integer,Integer>>();
	private NetwProg netwProg;
	private Map<Integer, Integer> NB = new HashMap<Integer, Integer>();
	
	public RoutingTable(NetwProg netwProg, Map<Integer, SocketHandler> socketHandlers, List<Integer> neighbours) {
		this.neighbours = new HashSet<Integer>(neighbours);
		this.nodes = neighbours;
		this.socketHandlers = socketHandlers;
		this.netwProg = netwProg;
		
		initialize();
	}

	public void initialize() {
		for(int w: neighbours)
		{
			ndis.put(w, new HashMap<Integer, Integer>());

			for(int v : nodes)
			{
				ndis.get(w).put(v, nodes.size());
			}
		}
		
		for(int v : nodes)
		{
			D.put(v, nodes.size());
			NB.put(v, UNDEF);
		}
		D.put(netwProg.id, 0);
		NB.put(netwProg.id, netwProg.id);

		for (int w : neighbours)
		{
			send(w, new MyDist(netwProg.id,0));
		}
		setChanged();
	}
	
	public void recompute(int neighbour) {
		int oldDv = D.get(neighbour);
		
		if (neighbour == netwProg.id)
		{
			D.put(neighbour, nodes.size());
			NB.put(neighbour,netwProg.id);
		}
		else
		{
			int min = 1000;
			for(int w : neighbours)
			{
					min = Math.min(ndis.get(w).get(neighbour), min);
			}
			
			int d = 1 + min;
			
			if (d < nodes.size())
			{
				D.put(neighbour, d);
				int w = neighbour;
				for(int x : neighbours)
				{
					if(ndis.get(x).get(neighbour)+1 == d)
					{
						w = x;
					}
				}
				NB.put(neighbour,w);
			}
			else
			{
				D.put(neighbour, nodes.size());
				NB.put(neighbour,UNDEF);
			}
		}
		if(D.get(neighbour) != oldDv)
		{
			for(int x : neighbours)
			{
				send(x, new MyDist(neighbour,D.get(neighbour)));
			}
		}
		
	}
	
	private void receive(MyDist myDist) {
		ndis.get(myDist.from).put(myDist.id,myDist.distance);
		recompute(myDist.id);
		setChanged();
		notifyObservers();
	}
	
	private void receive(Fail fail) {
		neighbours.remove(fail.neighbour);
		
		for(int neighbour : nodes)
		{
			recompute(neighbour);
		}
		setChanged();
		notifyObservers();
	}
	
	private void receive(Repair repair) {
		neighbours.add(repair.neighbour);
		
		for(int neighbour : nodes)
		{
			ndis.get(repair.neighbour).put(neighbour,nodes.size());
			send(repair.neighbour, new MyDist(neighbour,D.get(neighbour)));
		}
		setChanged();
		notifyObservers();
	}

	public void send(int destination, Message message) {
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
		} else {
			if (NB.get(destination) == UNDEF) {
				// send the message to the neighbour
				socketHandlers.get(destination).send(message);
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

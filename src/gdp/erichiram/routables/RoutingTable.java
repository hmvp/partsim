package gdp.erichiram.routables;

import java.util.Map;

import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Repair;

// TODO implement the Netchange algorithm!
public class RoutingTable {
	Map<Integer, SocketHandler> socketHandlers;
	
	public RoutingTable(Map<Integer, SocketHandler> socketHandlers) {
		this.socketHandlers = socketHandlers;
	}
	
	public void recompute(int neighbour) {
	}
	
	public void receive(MyDist myDist) {
		
	}
	
	public void receive(Fail fail) {
		
	}
	
	public void receive(Repair repair) {
		
	}

	public void send(int destination, Message message) {
		
		// to send the message <mydist, 1103, 2> to neighbour 1101, do:
		// socketHandlers.get(1101).send(new MyDist(1103, 2));
		
		// if the destination is a neighbour
		if ( socketHandlers.containsKey(destination) ) {
			// send the message to the neighbour
			socketHandlers.get(destination).send(message);
		} else {
			// TODO use the routing table to find the right neighbour
			int neighbour = 0;
			// send the message to that neighbour
			send(neighbour, message);
		}
	}
}

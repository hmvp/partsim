package gdp.erichiram.routables;

import java.util.Map;

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
		
		// to send the message <mydist, 1103, 2> to 1101, do:
		// socketHandlers.get(1101).send(new MyDist(1103, 2));
	}
	
	public void receive(MyDist myDist) {
		
	}
	
	public void receive(Fail fail) {
		
	}
	
	public void receive(Repair repair) {
		
	}

}

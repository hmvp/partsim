package gdp.erichiram.routables;

import java.util.Collection;

import gdp.erichiram.routables.Gui;

import javax.swing.SwingUtilities;

public class NetwProg {

	RoutingTable table;
	
	int port;
	
	Collection<Integer> neighbours;
	
	Collection<SocketHandler> socketHandlers;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetwProg netwProg = new NetwProg();
		netwProg.initialize();
	}

	private void initialize() {		
		// create table
		table.setNeighbour(10, 20);
		table.getNeighbour(20);
		
		// create gui
		SwingUtilities.invokeLater(new Gui());
		
		// initialize threads for sockets
		for ( int neighbour : neighbours ) {
			SocketHandler socketHandler = new SocketHandler(Math.min(port, neighbour));
			socketHandlers.add(socketHandler);
			socketHandler.start();
		}
				
	}

}

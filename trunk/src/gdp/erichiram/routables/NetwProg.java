package gdp.erichiram.routables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gdp.erichiram.routables.Gui;

import javax.swing.SwingUtilities;

public class NetwProg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO: remove these sample values when the argument parser is in place
		int argId = 1100;
		int argNeighbours[] = { 1104, 1103 };
		int argWeights[] = { 10, 5 };

		// create the application
		new NetwProg(argId, argNeighbours, argWeights);
	}

	private int id;
	private Collection<Integer> neighbours;
	private Map<Integer, SocketHandler> socketHandlers;
	private RoutingTable routingTable;

	public NetwProg(int argId, int[] argNeighbours, int[] argWeights) {

		this.id = argId;

		initializeNeighbours(argNeighbours, argWeights);
		initializeRoutingTable();
		initializeGui();
		initializeSocketHandlers();
	}

	private void initializeNeighbours(int[] argNeighbours, int[] argWeights) {
		// TODO: initialize neighbours
		for (int neighbour : argNeighbours) {
			neighbours.add(neighbour);
		}
	}

	private void initializeRoutingTable() {
		// TODO initialize routing table
	}

	private void initializeGui() {
		// start a separate thread to create and run the gui
		SwingUtilities.invokeLater(new Gui());
	}

	private void initializeSocketHandlers() {
		// initialize threads for sockets
		socketHandlers = new HashMap<Integer, SocketHandler>();
		for (int neighbour : neighbours) {
			// TODO: this is not the way to decide the port number to use
			int port = Math.min(id, neighbour);
			
			SocketHandler socketHandler = new SocketHandler(port, routingTable);
			socketHandlers.put(neighbour, socketHandler);
			socketHandler.start();
		}
	}
}

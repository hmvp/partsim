package gdp.erichiram.routables;

import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;
import gdp.erichiram.routables.message.Fail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHandler extends Thread {

	private int port;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;

	public SocketHandler(int port, RoutingTable routingTable) {
		this.port = port;
		this.routingTable = routingTable;

		try {
			// create a socket and connect it to the specified port on the loopback interface
			socket = new Socket((String)null, port);
			in = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("[" + port + "] Localhost is an unknown host: "	+ e.getLocalizedMessage());
		} catch (IOException e) {
			System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// read a message object from the input stream
				Object message = in.readObject();
				
				// relay the message to the routing table
				if (message instanceof MyDist) {
					routingTable.receive((MyDist)message);
				} else if (message instanceof Repair) {
					routingTable.receive((Repair)message);
				} else if (message instanceof Fail) {
					routingTable.receive((Fail)message);
				}

			} catch (IOException e) {
				System.err.println("[" + port + "] Something is wrong with the I/O: " + e.getLocalizedMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("[" + port + "] Something strange is happening: " + e.getLocalizedMessage());
			}
		}
	}

}

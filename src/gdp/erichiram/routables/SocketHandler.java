package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHandler extends Thread {

	private int port;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private NetwProg netwProg;

	public SocketHandler(NetwProg netwProg, RoutingTable routingTable) {
		this.netwProg = netwProg;
		this.routingTable = routingTable;
	}
	
	public SocketHandler(NetwProg netwProg, int port, RoutingTable routingTable) {
		this(netwProg, routingTable);
		this.port = port;

		while(socket == null)
		{
			try {
				// create a socket and connect it to the specified port on the loopback interface
				socket = new Socket(InetAddress.getLocalHost(), port);
				
				//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(socket.getInputStream());
			} catch (UnknownHostException e) {
				System.err.println("[" + port + "] Localhost is an unknown host: "	+ e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
			}
		}
	}

	public SocketHandler(NetwProg netwProg, Socket socket, RoutingTable routingTable) {
		this(netwProg, routingTable);
		this.socket = socket;
		this.port = socket.getPort();
		this.routingTable = routingTable;
		
		try {
			//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// read a message object from the input stream
				Object object = in.readObject();				
				// relay the message to the routing table
				if (object instanceof Message) {
					Message message = (Message) object;
					
				
					if(message.to == netwProg.id)
					{
						NetwProg.debug("recieving message");
						if (message instanceof MyDist) {
							routingTable.receive((MyDist)message);
						} else if (message instanceof Repair) {
							routingTable.receive((Repair)message);
						} else if (message instanceof Fail) {
							routingTable.receive((Fail)message);
						}
					}
					else //route to next node
					{
						NetwProg.debug("routing message");
						routingTable.send(message.to, message);
					}
				}

			} catch (IOException e) {
				System.err.println("[" + port + "] Something went wrong when receiving a message: " + e.getLocalizedMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("[" + port + "] Something strange is happening: " + e.getLocalizedMessage());
			}
		}
	}
	
	public void send(Message message) {
		try {
			out.writeObject(message);
		} catch (IOException e) {
			System.err.println("[" + port + "] Something went wrong when sending a message: " + e.getLocalizedMessage());
		}
	}

}

package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Identity;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.util.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHandler extends Thread {

	public int port;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private final NetwProg netwProg;
	
	public SocketHandler(NetwProg netwProg, int port) {
		this.netwProg = netwProg;
		this.port = port;
		initializeSocket();
	}

	public SocketHandler(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.socket = socket;
		initializeSocketServer();
	}

	private void initializeSocket() {
		while(socket == null)
		{
			try {
				// create a socket and connect it to the specified port on the loopback interface
				socket = new Socket(InetAddress.getLocalHost(), port);
				
				//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(socket.getInputStream());
				
				send(new Identity(netwProg.id));
			} catch (UnknownHostException e) {
				System.err.println("[" + port + "] Localhost is an unknown host: "	+ e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
			}
		}
	}
	
	
	private void initializeSocketServer() {
		try {
			//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
		
		
		try {
			Object object = in.readObject();
			if (object instanceof Identity) {
				Identity id = (Identity) object;
				
				this.port = id.from;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (true) {
			Object object = null;
			try {
				// read a message object from the input stream
				object = in.readObject();
			} catch (IOException e) {
				System.err.println("[" + port + "] Something went wrong when receiving a message: " + e.toString());
			} catch (ClassNotFoundException e) {
				System.err.println("[" + port + "] Something strange is happening: " + e.toString());
			}
			
			// relay the message to the routing table
			if (object != null && object instanceof Message) {
				Message message = (Message) object;
				
				if(message.to == netwProg.id)
				{
					Util.debug(netwProg.id, "Processing " + message + ".");
					routingTable.receive(message);
				}
				else
				{
					 //route to next node
					Util.debug(netwProg.id, "Rerouting " + message + ".");
					routingTable.send(message.to, message);
				}
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
	
	public int getPort()
	{
		return port;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

}

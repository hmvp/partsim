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
	private NetwProg netwProg;

	public SocketHandler(NetwProg netwProg, int port) {
		this.netwProg = netwProg;
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
				
				send(new Identity(netwProg.id));
			} catch (UnknownHostException e) {
				System.err.println("[" + port + "] Localhost is an unknown host: "	+ e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
			}
		}
	}

	public SocketHandler(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.socket = socket;
		
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
		if(routingTable == null)
			routingTable = netwProg.routingTable;
		
		while (true) {
			try {
				// read a message object from the input stream
				Object object = in.readObject();				
				// relay the message to the routing table
				if (object instanceof Message) {
					Message message = (Message) object;					

					Util.debug(netwProg.id, "Receiving message: " + message);
					
					if(message.to == netwProg.id)
					{
						Util.debug(netwProg.id, "-> Processing that message.");
						routingTable.receive(message);
					}
					else
					{
						 //route to next node
						Util.debug(netwProg.id, "-> Rerouting that message.");
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
	
	public int getPort()
	{
		return port;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

}

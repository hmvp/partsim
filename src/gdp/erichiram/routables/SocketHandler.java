package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Identity;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.Repair;
import gdp.erichiram.routables.util.Util;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SocketHandler extends Thread {

	public int port;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private final NetwProg netwProg;
	private int startingWeight;
	private boolean running = true;
	private boolean client = false;
	
	public SocketHandler(NetwProg netwProg, int port, int startingWeight) {
		this.netwProg = netwProg;
		this.port = port;
		this.startingWeight = startingWeight;
		client = true;
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
		
		routingTable.receive(new Repair(port,startingWeight));
		send(new Repair(netwProg.id,startingWeight));
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
		if(client)
		{
			initializeSocket();
		}
		
		Util.debug(netwProg.id, "done socket init for: " + port);
		
		while (running) {
			Object object = null;
			try {
				// read a message object from the input stream
				object = in.readObject();
			} catch (EOFException e) {
				running = false ;
				Util.debug(netwProg.id, "end of input, assume socket is dead");
			} catch (SocketException e) {
				running = false ;
				Util.debug(netwProg.id, "socket is closing");
			} catch (IOException e) {
				System.err.println("[" + port + "] Something went wrong when receiving a message: " + e.toString());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println("[" + port + "] Something strange is happening: " + e.toString());
				e.printStackTrace();
			}
			
			// relay the message to the routing table
			if (object != null && object instanceof Message) {
				Message message = (Message) object;
				
				Util.debug(netwProg.id, "Processing " + message + ".");
				routingTable.receive(message);
			}
		}
		
		// close all the sockets
		try {
			routingTable.receive(new Fail(port));
			
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(Message message) {		
		try {
			out.writeObject(message);
			out.flush();
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

	public void die() {
		running = false;
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		if(client)
			return "clientsocket from: "+ netwProg.id + " to: " + port;
		else
			return "serversocket from: "+ netwProg.id + " to: " + port;
	}

}

package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
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

public class Neighbour implements Runnable, Comparable<Neighbour> {

	public final int port;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private final NetwProg netwProg;
	private final int startingWeight;
	private boolean running = true;
	private boolean initDone = false;
	private final boolean client;
	
	public Neighbour(NetwProg netwProg, int port, int startingWeight) {
		this.netwProg = netwProg;
		this.routingTable = netwProg.routingTable;
		this.port = port;
		this.startingWeight = startingWeight;
		this.client = true;
	}

	public Neighbour(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.routingTable = netwProg.routingTable;
		this.socket = socket;
		this.client = false;
		
		try {
			createStreams();
		} catch (IOException e) {
			System.err.println("[ unknown port ] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
		
		int tempPort = 0;
		int tempWeight = 0;
		try {
			Object object = in.readObject();
			if (object instanceof Repair) {
				Repair id = (Repair) object;
				
				tempPort = id.neighbour;
				tempWeight = id.weight;
			}
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		port = tempPort;
		
		//als we nog aan het initializeren zijn dan hebben we info om connecties op te starten
		// die qua weights niet symetrisch zijn, die info moeten we dus gebuiken
		if(!netwProg.startingNeighbours.isEmpty() && netwProg.startingNeighbours.containsKey(port))
		{
			tempWeight = netwProg.startingNeighbours.get(port);
			netwProg.startingNeighbours.remove(port);
		}
		
		startingWeight = tempWeight;
	}

	private void initializeSocket() {
		while(socket == null)
		{
			try {
				// create a socket and connect it to the specified port on the loopback interface
				socket = new Socket(InetAddress.getLocalHost(), port);
				
				createStreams();

			} catch (UnknownHostException e) {
				System.err.println("[" + port + "] Localhost is an unknown host: "	+ e.getLocalizedMessage());
			} catch (IOException e) {
				System.err.println("[" + port + "] Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//TODO: merge this with repair needs merge of neighbour and sockethandlers set
		send(new Repair(netwProg.id, startingWeight));
	}
	
	private void createStreams() throws IOException
	{
		//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		
		in = new ObjectInputStream(socket.getInputStream());
	}
	
	public void run() {
		if(client)
		{
			initializeSocket();
		}
		initDone = true;
		routingTable.repair(this, startingWeight);

		
		
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
			
			try {
				Thread.sleep(netwProg.getT());
			} catch (InterruptedException e) {}
			
			// relay the message to the routing table
			if (object != null && object instanceof MyDist) {
				MyDist message = (MyDist) object;
				
				Util.debug(netwProg.id, "Processing " + message + ".");
				routingTable.receive(message);
			}
		}
		
		// close all the sockets
		try {
			routingTable.fail(this);
			//TODO:  send fail to other side
			
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Neighbour other = (Neighbour) obj;
		if (port != other.port)
			return false;
		return true;
	}

	public void send(Message message) {	
		message.from = netwProg.id;
		message.to = getPort();
		
		if(!initDone && !(message instanceof Repair))
			throw new RuntimeException("Dat mag dus niet! want we moeten eerst klaar zijn met repairen!");
		
		
		try {
			out.writeObject(message);
			out.flush();
			netwProg.messagesSent.increment();
		} catch (IOException e) {
			System.err.println("[" + port + "] Something went wrong when sending a message: " + e.getLocalizedMessage());
			die();
		}
	}
	
	public int getPort()
	{
		return port;
	}

	public void die() {
		running = false;
		netwProg.messagesSent.increment();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return String.valueOf(port);
	}

	public int compareTo(Neighbour o) {
		return port - o.getPort();
	}
}

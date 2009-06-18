package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.MyDist;
import gdp.erichiram.routables.message.Repair;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Neighbour implements Runnable, Comparable<Neighbour> {

	public final int id;
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
		this.id = port;
		this.startingWeight = startingWeight;
		this.client = true;
	}

	public Neighbour(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.routingTable = netwProg.routingTable;
		this.socket = socket;
		this.client = false;
		
		createStreams();
		
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
		id = tempPort;
		
		//als we nog aan het initializeren zijn dan hebben we info om connecties op te starten
		// die qua weights niet symetrisch zijn, die info moeten we dus gebuiken
		if(!netwProg.startingNeighbours.isEmpty() && netwProg.startingNeighbours.containsKey(id))
		{
			//we verwijderen alleen en checken wat we verwijderd hebben.
			//ondanks dat we zeker weten dat deze neighbour dit specefieke id opvraagt 
			//spelen we toch op zeker door niet eerst te checken en dan pas te removen.
			Integer w = netwProg.startingNeighbours.remove(id);
			if(w != null)
				tempWeight = w;
		}
		
		startingWeight = tempWeight;
	}

	private void initializeSocket() {
		
		//we try to connect until we succeed
		while(socket == null)
		{
			try {
				// create a socket and connect it to the specified port on the loopback interface
				socket = new Socket(InetAddress.getLocalHost(), id);
			} catch (IOException e) {
				netwProg.error("something went wrong when connecting to: "+ id +" error: "	+ e.getLocalizedMessage());
			}
							
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		createStreams();
		
		send(new Repair(netwProg.id, startingWeight));
	}
	
	private void createStreams()
	{
		
		try {
			//we moeten out eerst doen omdat in anders blockt tot out (die aan de andere kant) de serializatie header heeft geflusht
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream(socket.getInputStream());
		}  catch (IOException e) {
			netwProg.error("Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
	}
	
	public void run() {
		if(client)
		{
			initializeSocket();
		}
		initDone = true;
		routingTable.repair(id, startingWeight);

		
		
		netwProg.debug("done socket init for: " + id);
		
		while (running) {
			Object object = null;
			try {
				// read a message object from the input stream
				object = in.readObject();
			} catch (EOFException e) {
				netwProg.debug("end of input, assume socket is dead");
				running = false;
				break;
			} catch (SocketException e) {
				netwProg.debug("socket is closing");
				running = false;
				break;
			} catch (IOException e) {
				if(running)
					netwProg.error("Something went wrong when receiving a message: " + e.toString());
				break;
			} catch (ClassNotFoundException e) {
				if(running)
					netwProg.error("Something strange is happening: " + e.toString());
				break;
			}
			
			try {
				Thread.sleep(netwProg.getT());
			} catch (InterruptedException e) {}
			
			// relay the message to the routing table
			if (object != null && object instanceof MyDist) {
				MyDist message = (MyDist) object;
				
				netwProg.debug("Processing " + message + ".");
				routingTable.receive(message);
			}
		}
		
		netwProg.messagesSent.increment();
		routingTable.fail(this);
			
		finalize();
	}
	
	public void send(Message message) {	
		if(running)
		{
		
		message.from = netwProg.id;
		message.to = id;
		
		if(!initDone && !(message instanceof Repair))
			throw new RuntimeException("Dat mag dus niet! want we moeten eerst klaar zijn met repairen!");
		
		
		try {
			out.writeObject(message);
			out.flush();
			netwProg.messagesSent.increment();
		} catch (IOException e) {
			netwProg.error("Something went wrong when sending a message: " + e.getLocalizedMessage());
			die();
		}
		}
	}

	public void die() {
		running = false;
		
		//We need to close the inputstream because readObject is blocking and we need it to stop
		try {
			in.close();
		} catch (IOException e) {}
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		if (id != other.id)
			return false;
		return true;
	}

	
	protected void finalize()
	{
		try {
			out.close();
		} catch (IOException e) {}
		try {
			in.close();
		} catch (IOException e) {}
		try {
			socket.close();
		} catch (IOException e) {}
	}
	
	public String toString()
	{
		return String.valueOf(id);
	}

	public int compareTo(Neighbour o) {
		return id - o.id;
	}
}

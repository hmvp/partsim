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

public class Channel implements Runnable, Comparable<Channel> {

	public final int id;
	private RoutingTable routingTable;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private final NetwProg netwProg;
	private final int startingWeight;
	private boolean running = true;
	private boolean initDone = false;

	public Channel(NetwProg netwProg, int port, int startingWeight) {
		this.netwProg = netwProg;
		this.routingTable = netwProg.routingTable;
		this.id = port;
		this.startingWeight = startingWeight;
	}

	public Channel(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.routingTable = netwProg.routingTable;
		this.socket = socket;

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

		// als we nog aan het initializeren zijn dan hebben we info om
		// connecties op te starten
		// die qua weights niet symetrisch zijn, die info moeten we dus gebuiken
		// we verwijderen alleen en checken wat we verwijderd hebben.
		// ondanks dat we zeker weten dat deze neighbour dit specefieke id
		// opvraagt
		// spelen we toch op zeker door niet eerst te checken en dan pas te
		// removen.
		Integer w = netwProg.neighbours.remove(id);
		if (w != null)
		{
			tempWeight = w;
		}
		
		startingWeight = tempWeight;
	}

	private void initialize() {
		if (socket == null) {//only if we are a client
			// we try to connect until we succeed
			while (socket == null && running) {
				try {
					// create a socket and connect it to the specified port on the loopback interface
					socket = new Socket(InetAddress.getLocalHost(), id);
					
					createStreams();
					
					send(new Repair(netwProg.id, startingWeight));
				} catch (IOException e) {
					netwProg.error(e.getLocalizedMessage() + " when connecting to " + id + ". Retrying in " + (Configuration.retryConnectionTime / 1000.0f) + " seconds.");
					try {
						Thread.sleep(Configuration.retryConnectionTime);
					} catch (InterruptedException e1) {}
				
				}
			}
		}
		initDone = true;
	}

	
	/**
	 * Create in and output streams in this channel.
	 */
	private void createStreams() {

		try {
			// We have to create the OutputStream first because it will
			// otherwise block until the OutputStream on the other side has
			// flushed its serialization header.
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();

			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			netwProg.error("Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
	}

	/**
	 * main loop, listens for messages
	 */
	public void run() {
		initialize();
		routingTable.repair(id, startingWeight);
		netwProg.debug("Finished socket initialization for: " + id);

		while (running) {
			Object object = null;
			try {
				// Read a message object from the InputStream.
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
				if (running)
					netwProg.error("Something went wrong when receiving a message: " + e.toString());
				break;
			} catch (ClassNotFoundException e) {
				if (running)
					netwProg.error("Something strange is happening: " + e.toString());
				break;
			}

			try {
				Thread.sleep(netwProg.getT());
			} catch (InterruptedException e) {
			}

			// Relay the message to the RoutingTable.
			if (object != null && object instanceof MyDist) {
				MyDist message = (MyDist) object;

				routingTable.receive(message);
			}
		}

		netwProg.debug("Channel is done and starts closing: " + id);
		netwProg.messagesSent.increment();
		routingTable.fail(id);
		finalize();
	}

	/**
	 * 
	 * @param message
	 */
	public void send(Message message) {
		if (running) {

			message.from = netwProg.id;
			message.to = id;

			if (!initDone && !(message instanceof Repair))
				throw new RuntimeException("Dat mag dus niet! want we moeten eerst klaar zijn met repairen!");

			netwProg.debug("Sending message to :" + id + " message: " + message);
			try {
				out.writeObject(message);
				out.flush();
				netwProg.messagesSent.increment();
			} catch (IOException e) {
				netwProg.error("Something went wrong when sending a message: " + e.getLocalizedMessage());
				die();
			}
		} else {
			netwProg.debug("tried sending a message while dead. for: " + id);
		}
	}

	public void die() {
		running = false;

		// We need to close the inputstream because readObject is blocking and
		// we need it to stop
		try {
			if(in != null)
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
		Channel other = (Channel) obj;
		if (id != other.id)
			return false;
		return true;
	}

	protected void finalize() {
		try {
			out.close();
		} catch (Exception e) {
		}
		try {
			in.close();
		} catch (Exception e) {
		}
		try {
			socket.close();
		} catch (Exception e) {
		}
	}

	public String toString() {
		return String.valueOf(id);
	}

	public int compareTo(Channel o) {
		return id - o.id;
	}
}

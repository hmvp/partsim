package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.Repair;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles connections to other nodes. Providing a channel over which messages are sent and received.
 * @author Hiram van Paassen, Eric Broersma
 */
public class Channel implements Runnable, Comparable<Channel> {
	
	/**
	 * Reference to the main program.
	 */
	private final NetwProg netwProg;

	/**
	 * Id of the neighbour this channel is connected with.
	 */
	public final int id;
	
	/**
	 * Socket to send and receive.
	 */
	private Socket socket;
	
	/**
	 * Streams to send and receive Objects through the socket.
	 */
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	
	/**
	 * Initial channel weight.
	 */
	private final int initialWeight;
	
	/**
	 * True if we're still running, false otherwise.
	 */
	private boolean running = true;

	/**
	 * Construct a new Channel object for the specified NetwProg, port and channel weight.
	 * @param netwProg 		Reference to the main program.
	 * @param port			Port number of the node we're connecting with.
	 * @param initialWeight	Initial weight for the channel.
	 */
	public Channel(NetwProg netwProg, int port, int initialWeight) {
		this.netwProg = netwProg;
		this.id = port;
		this.initialWeight = initialWeight;
	}

	/**
	 * Construct a new Channel object for the specified NetwProg with the given socket.
	 * @param netwProg 	Reference to the main program.
	 * @param socket	Socket to use.
	 */
	public Channel(NetwProg netwProg, Socket socket) {
		this.netwProg = netwProg;
		this.socket = socket;

		createStreams();

		int tempPort = 0;
		int tempWeight = 0;
		try {
			Object object = inputStream.readObject();
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
		
		initialWeight = tempWeight;
	}

	private void initialize() {
		if (socket == null) {//only if we are a client
			// we try to connect until we succeed
			while (socket == null && running) {
				try {
					// create a socket and connect it to the specified port on the loopback interface
					socket = new Socket(InetAddress.getLocalHost(), id);
					
					createStreams();
					
					send(new Repair(netwProg.id, initialWeight));
				} catch (IOException e) {
					netwProg.error(e.getLocalizedMessage() + " when connecting to " + id + ". Retrying in " + (Configuration.retryConnectionTime / 1000.0f) + " seconds.");
					try {
						Thread.sleep(Configuration.retryConnectionTime);
					} catch (InterruptedException e1) {}
				
				}
			}
		}
	}

	
	/**
	 * Create in and output streams for this channel.
	 */
	private void createStreams() {

		try {
			// We have to create the OutputStream first because it will
			// otherwise block until the OutputStream on the other side has
			// flushed its serialization header.
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();

			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			netwProg.error("Could not create socket or get inputstream from socket: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Listen for messages.
	 */
	public void run() {
		initialize();
		netwProg.routingTable.receive(new Repair(id, initialWeight));
		netwProg.debug("Finished socket initialization for: " + id);

		// Keep listening for messages.
		while (running) {
			Object object = null;
			try {
				// Read a message object from the InputStream.
				object = inputStream.readObject();
			} catch (EOFException e) {
				netwProg.debug("end of input, assume socket is dead");
				running = false;
				break;
			} catch (SocketException e) {
				netwProg.debug("socket is closing");
				running = false;
				break;
			} catch (IOException e) {
				if (running) {
					netwProg.error("Something went wrong when receiving a message: " + e.toString());
				}
				break;
			} catch (ClassNotFoundException e) {
				if (running) {
					netwProg.error("Something strange is happening: " + e.toString());
				}
				break;
			}

			try {
				Thread.sleep(netwProg.getT());
			} catch (InterruptedException e) {
			}

			// Relay the message to the RoutingTable.
			if (object != null && object instanceof Message) {
				Message message = (Message) object;
				
				netwProg.routingTable.receive(message);
			}
		}

		netwProg.debug("Channel is done and starts closing: " + id);
		netwProg.routingTable.receive(new Fail(id));
		
		// Clean up the streams and socket.
		finalize();
	}

	/**
	 * Send a message through this channel.
	 * @param message the message to send
	 */
	public void send(Message message) {
		if (running) {

			//set sender and destination here (mainly debug but mydists also need an sender)
			message.from = netwProg.id;
			message.to = id;

			netwProg.debug("Sending message to: " + id + " message: " + message);
			try {
				outputStream.writeObject(message);
				outputStream.flush();
			} catch (IOException e) {
				netwProg.error("Something went wrong when sending a message: " + e.getLocalizedMessage());
				close();
			}
		} else {
			netwProg.debug("tried sending a message while dead. for: " + id);
		}
	}

	/**
	 * Close this channel.
	 */
	public void close() {
		
		// Stop listening.
		running = false;

		// It's possible ObjectInputStream#readObject() is preventing us from actually closing,
		// in that case force it to close.
		try {
			if(inputStream != null)
				inputStream.close();
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

	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			outputStream.close();
		} catch (Exception e) {
		}
		try {
			inputStream.close();
		} catch (Exception e) {
		}
		try {
			socket.close();
		} catch (Exception e) {
		}
	}

	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public String toString() {
		return String.valueOf(id);
	}

	/**
	 * @see java.lang.Comparable#compareTo
	 */
	public int compareTo(Channel o) {
		return id - o.id;
	}
}

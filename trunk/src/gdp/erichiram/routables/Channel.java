package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Message;
import gdp.erichiram.routables.message.Repair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

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
	private final int id;
	
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

		Object object = null;
		try {
			object = inputStream.readObject();
		} catch (Exception e) {
			
			// Something went wrong, close the socket.
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		if (object instanceof Repair) {
			Repair repair = (Repair) object;

			// Get the socket ID.
			id = repair.neighbour;

			// If we got the weight for this connection through the command line, use it.
			// Otherwise use the weight we got from the Repair message from the other node.
			Integer weight = netwProg.neighboursToWeights.remove(id);
			if (weight != null) {
				initialWeight = weight;
			} else {
				initialWeight = repair.weight;
			}
		} else {
			// Something went wrong.
			// Add some senseless values which probably won't get used anyway because the socket is already closed.
			id = RoutingTable.UNDEF_ID;
			initialWeight = 1000;

			// Just to be sure, we close the socket.
			if ( !socket.isClosed() ) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates a socket if this is a client side node.
	 */
	private void initialize() {
		
		// If we are a client.
		if (socket == null) {
			
			// We try to connect until we succeed.
			while (socket == null && running) {
				try {
					// Create a socket and connect it to the specified port on the loopback interface.
					socket = new Socket(InetAddress.getLocalHost(), id);
					
					// Create the object streams.
					createStreams();
					
					// Send the other node a Repair message.
					send(new Repair(netwProg.id, initialWeight));
				} catch (IOException e) {
					// Something went wrong. Perhaps the other node isn't up yet, so let the user know we're working on it.
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
		
		//only if we are really running
		if(running)
			netwProg.routingTable.receive(new Repair(id, initialWeight));
		netwProg.debug("Finished socket initialization for: " + id);

		// Keep listening for messages.
		while (running) {
			Object object = null;
			
			try {
				// Read a message object from the InputStream.
				object = inputStream.readObject();
			} catch (Exception e) {
				netwProg.debug("something went wrong, we assume we can close. Error:" + e.getLocalizedMessage());
				netwProg.failConnection(id);
			}

			try {
				Thread.sleep(netwProg.getT());
			} catch (InterruptedException e) {}

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
				netwProg.messagesSent.increment();
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

	public int getId() {
		return id;
	}
}

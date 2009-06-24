package gdp.erichiram.routables.message;

/**
 * Encode a ChangeWeight message. This message is delivered to the RoutingTable
 * when the weight for an existing connection was changed through the GUI.
 * 
 * @author eric, hiram
 */
public class ChangeWeight extends Message {

	public final int node;
	public final int weight;

	public ChangeWeight(int id, int weight) {
		this.node = id;
		this.weight = weight;
	}

	private static final long serialVersionUID = 8630877900338384631L;

	public String toString() {
		return super.toString() + " <changeweight, " + node + ", " + weight + ">";
	}
}

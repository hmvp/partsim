// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

package gdp.erichiram.routables.message;

/**
 * Encode a Repair message. This message is delivered to the RoutingTable when a
 * new connection with a specific weight was added through the GUI.
 * @author Hiram van Paassen, Eric Broersma
 */
public class Repair extends Message {
	private static final long serialVersionUID = -8051333547523995993L;
	
	public final int neighbour;
	public final int weight;
	
	public Repair(int neighbour, int weight) {
		this.neighbour = neighbour;
		this.weight = weight;
	}
	
	public String toString() {
		return super.toString() + " <repair, " + neighbour + ", " + weight + ">";
	}
}

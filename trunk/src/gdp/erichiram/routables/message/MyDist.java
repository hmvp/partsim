// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

package gdp.erichiram.routables.message;

/**
 * Encode a MyDist message. This message is delivered to the RoutingTable after
 * it is received from a neighbouring NetwProg that probably just recomputed its
 * routing table.
 * @author Hiram van Paassen, Eric Broersma
 */
public class MyDist extends Message {	
	private static final long serialVersionUID = -1196856163917315605L;
	
	public final int id;
	public final int distance;
	
	public MyDist(int id, int distance) {
		this.id = id;
		this.distance = distance;
	}
	
	public String toString() {
		return super.toString() + " <mydist, " + id + ", " + distance + ">";
	}
}

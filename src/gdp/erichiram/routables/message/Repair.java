package gdp.erichiram.routables.message;

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

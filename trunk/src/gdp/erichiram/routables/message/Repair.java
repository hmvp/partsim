package gdp.erichiram.routables.message;

public class Repair implements Message {
	public int neighbour;
	
	public Repair(int neighbour) {
		this.neighbour = neighbour;
	}
}

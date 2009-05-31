package gdp.erichiram.routables.message;

public class Repair extends Message {
	private static final long serialVersionUID = -8051333547523995993L;
	
	public int neighbour;
	public int weight;
	
	public Repair(int neighbour, int weight) {
		this.neighbour = neighbour;
		this.weight = weight;
	}
}

package gdp.erichiram.routables.message;

public class Repair extends Message {
	private static final long serialVersionUID = -8051333547523995993L;
	
	public int neighbour;
	
	public Repair(int neighbour) {
		this.neighbour = neighbour;
	}
}

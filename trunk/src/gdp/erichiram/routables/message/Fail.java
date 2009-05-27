package gdp.erichiram.routables.message;

public class Fail implements Message {
	public int neighbour;
	
	public Fail(int neighbour) {
		this.neighbour = neighbour;
	}
}

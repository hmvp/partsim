package gdp.erichiram.routables.message;

public class Fail extends Message {

	public final int neighbour;
	
	public Fail(int neighbour) {
		this.neighbour = neighbour;
	}

	private static final long serialVersionUID = 1514286767179700747L;

	public String toString() {
		return super.toString() + " <fail, " + neighbour + " >";
	}
}

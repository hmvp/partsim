package gdp.erichiram.routables.message;

public class Fail extends Message {
	private static final long serialVersionUID = 3405177360478514587L;
	
	public int neighbour;
	
	public Fail(int neighbour) {
		this.neighbour = neighbour;
	}
	
	public String toString() {
		return super.toString() + " <fail, " + neighbour + ">";
	}
}

package gdp.erichiram.routables.message;

public class ChangeWeight extends Message {

	public final int node;
	public final int weight;

	public ChangeWeight(int id, int weight) {
		this.node = id;
		this.weight = weight;
	}

	private static final long serialVersionUID = 8630877900338384631L;

}

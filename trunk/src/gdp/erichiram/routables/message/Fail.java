package gdp.erichiram.routables.message;

public class Fail extends Message {

	public Fail(int id) {
		this.to = id;
	}

	private static final long serialVersionUID = 1514286767179700747L;

}

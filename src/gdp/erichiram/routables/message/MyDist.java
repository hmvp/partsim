package gdp.erichiram.routables.message;

public class MyDist extends Message {	
	private static final long serialVersionUID = -1196856163917315605L;
	
	public int id;
	public int distance;
	
	public MyDist(int id, int distance) {
		this.id = id;
		this.distance = distance;
	}
}

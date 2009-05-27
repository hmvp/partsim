package gdp.erichiram.routables.message;

public class MyDist implements Message {	
	public int id;
	public int distance;
	
	public MyDist(int id, int distance) {
		this.id = id;
		this.distance = distance;
	}
}

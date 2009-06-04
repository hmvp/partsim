package gdp.erichiram.routables.message;

import java.io.Serializable;

public abstract class Message implements Serializable{
	private static final long serialVersionUID = -7622270218128154257L;
	
	public int from;
	public int to;
	
	public String toString() {
		return "(" + from + " -> " + to + ")";
	}
}

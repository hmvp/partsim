package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.Queue;

public class Main {

	/**
	 * max number of threads
	 */
	private int pMax;
	
	/**
	 * queue of particles waiting to be computed
	 */
	private Queue<Particle> q;
	
	/**
	 * number of running threads
	 */
	private int p;
	
	/**
	 * number of particles
	 */
	private int n;
	
	/**
	 * collection of threads
	 */
	private Collection<Thread> pool;
	
	/**
	 * time var to slow simulation down
	 */
	private int t;
	
	/**
	 * gui thread
	 */
	private Thread gui;
	
	
	public Main()
	{
		gui = new Gui(this);
		
	}
	
	
	public void runProgram(){
		gui.start();
		
		for (Thread t : pool)
		{
			t.start();
		}
	}
	
	public static void main(String[] args){
		Main m = new Main();
		m.runProgram();
	}
	
}

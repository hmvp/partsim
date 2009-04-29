package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

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
	private long t = 100;
	
	/**
	 * rectangle dimensions
	 */
	public static final int rWidth = 800;
	public static final int rHeight = 600;
	public static final int guiSpeed = 100 ;

	
	
	public Main()
	{		
		q = new LinkedBlockingQueue<Particle>();		
		pool = new HashSet<Thread>();

		
	}
	
	
	public void runProgram(){
		SwingUtilities.invokeLater(new Gui(this));
		
		// Load the particles.
		
		File file = new File("particles.txt");
		try {
			q.addAll(ConfigurationReader.readFile(file));
		} catch (FileNotFoundException e) {
			System.out.println("Het bestand 'particles.txt' kon niet worden gevonden.");
			System.exit(1);
		}
		
		// Fill the thread pool.
		pool.add(new Animation(this));
		pool.add(new Animation(this));
		pool.add(new Animation(this));
		
		
		for (Thread t : pool)
		{
			t.start();
		}
	}
	
	public static void main(String[] args){
		Main m = new Main();
		m.runProgram();
	}
	
	public Queue<Particle> getQ() {
		return q;
	}


	public long getT() {
		return t;
	}


	public void setPMax(int pMax) {
		this.pMax = pMax;
		debug("PMax changed to: "+ pMax);
	}
	
	public static void debug(String message)
	{
		System.out.println(Thread.currentThread().getId()+":"+message);
	}


	public void setT(int t) {
		this.t = t;
		debug("T changed to: "+t);
	}
	
}

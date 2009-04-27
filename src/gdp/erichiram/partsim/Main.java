package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;

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
	
	
	public Main()
	{		
		// Load the particles.
		q = new SynchronizedQueue<Particle>();		
		
		File file = new File("particles.txt");
		try {
			q.addAll(ConfigurationReader.readFile(file));
		} catch (FileNotFoundException e) {
			System.out.println("Het bestand 'particles.txt' kon niet worden gevonden.");
			System.exit(1);
		}
		
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		q.add(new Particle());
		
		// Fill the thread pool.
		pool = new HashSet<Thread>();
		pool.add(new Animation(this));
		pool.add(new Animation(this));
		pool.add(new Animation(this));

		
	}
	
	
	public void runProgram(){
		SwingUtilities.invokeLater(new Gui(this));

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
	
}

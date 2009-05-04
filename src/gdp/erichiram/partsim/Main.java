package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

public class Main {
	


	/**
	 * max number of threads
	 */
	private int pMax = 0;
	
	/**
	 * queue of particles waiting to be computed
	 */
	private LinkedBlockingQueue<Particle> q;
	
	/**
	 * number of running threads
	 */
	private int p = 0;
	
	/**
	 * number of particles
	 */
	private int n;
	
	/**
	 * collection of threads
	 */
	private ThreadPool pool = new ThreadPool(this);
	
	/**
	 * time var to slow simulation down
	 */
	private volatile long t = 0;
	
	/**
	 * rectangle dimensions
	 */
	public static final int rWidth = 800;
	public static final int rHeight = 600;
	public static final int guiSpeed = 100 ;

	/**
	 * current executing round
	 */
	public static final int initialRound = 0;
	private int round = initialRound;
	
	
	public int getRound() {
		return round;
	}


	public Main()
	{		
		q = new LinkedBlockingQueue<Particle>();		
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
		
		//setPMax(4);
		
	}
	
	public static void main(String[] args){
		Main m = new Main();
		m.runProgram();
	}
	
	public LinkedBlockingQueue<Particle> getQ() {
		return q;
	}


	public long getT() {
		return t;
	}

	public int getP()
	{
		return pool.size();
	}

	public void setPMax(int pMax) {
		pool.setPmax(pMax);
	}
	
	public static void debug(String message)
	{
		System.out.println(Thread.currentThread().getId()+":"+message);
	}


	public void setT(int t) {
		this.t = t;
		debug("T changed to: "+t);
	}


	public void nextRound() {
		
		++round;
		debug("============== Round " + round + " ===================");
	}


	public ThreadPool getPool() {
		return pool;
	}
	
}

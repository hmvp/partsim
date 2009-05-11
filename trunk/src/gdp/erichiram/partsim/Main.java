package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

public class Main {
	



	
	/**
	 * queue of particles waiting to be computed
	 */
	private LinkedBlockingQueue<Particle> q;
	
	/**
	 * number of particles
	 */
	private int n;
	
	private Collection<Particle> particles;
	
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
			particles = ConfigurationReader.readFile(file);
			n = particles.size();
			q.addAll(particles);
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




	/**
	 * go to the next round if all particles are in the queue
	 * (not currently being updated by a thread), wait otherwise
	 */
	public void nextRound() {
		
		
		if ( q.size() == n ) {
			++round;
			debug("============== Round " + round + " ===================");
		} else {
			debug("-------------- Just wait a bit! --------------------");
		}
	}


	public ThreadPool getPool() {
		return pool;
	}
	public boolean queueIsFilled() {
		// the queue is "filled" when all particles are in it
		Main.debug("q.size()="+q.size()+" n=" +n);
		
		return q.size() == n;
	}
	
	
	
	/**
	 * adds a particle object to the queue and increases n accordingly
	 * @param particle the particle object to be added
	 */
	public void addParticle(Particle particle){
		q.offer(particle);
		particles.add(particle);
		n++;
	}


	/**
	 * removes all particles named name and decreases n accordingly
	 * @param name the name of the particles to be removed
	 */
	public synchronized void removeParticles(char name) {
		for ( Particle p : particles)
		{
			if(p.getName() == name)
			{
				p.die();
				particles.remove(p);
				n--;
			}
		}
	}



	public  Collection<Particle> getParticles() {
		return particles;
	}
	
}

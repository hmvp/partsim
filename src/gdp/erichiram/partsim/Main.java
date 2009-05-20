/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Queue;

import javax.swing.SwingUtilities;

public class Main {
	



	
	/**
	 * queue of particles waiting to be computed
	 */
	private final Queue<Particle> q = new BlockingQueue<Particle>();
	
	/**
	 * collection of particles
	 */	
	private final Collection<Particle> particles;
	
	/**
	 * collection of threads
	 */
	private final ThreadPool pool = new ThreadPool(this);
	
	/**
	 * time var to slow simulation down
	 */
	private volatile int t = 0;
	public volatile int k = 1;


	/**
	 * rectangle dimensions
	 */
	public static final int width = 800;
	public static final int height = 600;
	
	public static final int guiSpeed = 100 ;

	public static final int initialRound = 0;

	private static final boolean DEBUG = false;
	
	
	protected final Round round = new Round(this);	
	
	public Main(Collection<Particle> particles){
		SwingUtilities.invokeLater(new Gui(this,pool));
		this.particles = particles;
		q.addAll(particles);
	}
	
	public static void main(String[] args){
		
		// Load the particles.
		File file = new File("particles.txt");
		try {
			new Main(ConfigurationReader.readFile(file));
		} catch (FileNotFoundException e) {
			System.out.println("Het bestand 'particles.txt' kon niet worden gevonden.");
			System.exit(1);
		}	
	}
	
	public Queue<Particle> getQ() {
		return q;
	}

	public  Collection<Particle> getParticles() {
		return particles;
	}



	public void setT(int t) {
		this.t = t;
		debug("T changed to: "+t);
	}

	public long getT() {
		return t;
	}

	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}
	
	
	/**
	 * adds a particle object to the queue and increases n accordingly
	 * @param particle the particle object to be added
	 */
	private void addParticle(Particle particle){
		q.offer(particle);
		particles.add(particle);
		pool.update();
	}

	public void addParticle(int x, int y, int dx, int dy, char name)
	{
		addParticle(new Particle(x,y,dx,dy,name,round.getRoundNumber()+1));
	}


	public void addParticle() {
		addParticle(new Particle(round.getRoundNumber()+1));
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
			}
		}
	}
	
	
	public static void debug(String message)
	{
		if (DEBUG)
		{
			System.out.println(Thread.currentThread().getId()+": "+message);
		}
	}
}

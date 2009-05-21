/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import gdp.erichiram.partsim.util.BlockingQueue;
import gdp.erichiram.partsim.util.ConfigurationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Queue;

import javax.swing.SwingUtilities;

/**
 * Main class, entry point for execution and the place to get the shared data
 *
 */
public class Main {
	
	/**
	 * queue of particles waiting to be computed
	 * this queue is special, instead of being a normal synchronized queue 
	 * like we use as particle store (the one we did build ourself)
	 * this one overrides poll and takes care of dead particles 
	 * and particles that are to new
	 */
	public final Queue<Particle> q = new BlockingQueue<Particle>()
	{
		/**
		 * overiden poll method to make sure threads cannot take particles not in this round
		 * or dead
		 */
		public synchronized Particle poll(){
			
			//if peek is null or particle not in round return null
			if(peek() == null || round.getRoundNumber() != peek().getRound())
			{
				return null;
			}
			
			//get the particle, check if its processable 
			//if not try this method again to make sure we get a particle if it is there
			Particle p = super.poll();
			if(!p.process())
			{
				return poll();
			}
			
			
			return p;
		}
	};
	
	/**
	 * collection of particles
	 */	
	protected final Collection<Particle> particles = new BlockingQueue<Particle>();
	
	/**
	 * collection of threads
	 */
	protected final ThreadPool pool = new ThreadPool(this);
	
	/**
	 * keeps track of the rounds, makes sure threads dont work on particles for next round
	 */
	protected final Round round = new Round(this);	
	
	/**
	 * time var to slow simulation down
	 * its thread safe since we only write or read and 
	 * those are atomic on (volatile) integers 
	 */
	private volatile int t = 0;
	
	/**
	 * number of particles a thread takes from the queue
	 * its thread safe since we only write or read and 
	 * those are atomic on (volatile) integers 
	 */
	private volatile int k = 1;


	/**
	 * rectangle dimensions
	 */
	public static final int width = 800;
	public static final int height = 600;
	
	/**
	 * timer speed for gui updates
	 */
	protected static final int guiSpeed = 100 ;

	/**
	 * starting round
	 */
	public static final int initialRound = 0;

	/**
	 * display debug statements
	 */
	private static final boolean DEBUG = true;
		
	/**
	 * constructor
	 * Starts up Gui and needs an collection of particles
	 * @param particles
	 */
	public Main(Collection<Particle> particles){
		SwingUtilities.invokeLater(new Gui(this));
		this.particles.addAll(particles);
		q.addAll(particles);
	}
	
	/**
	 * Startup method
	 * arguments are not used
	 * @param args
	 */
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
	
	/**
	 * @see Main#t t
	 * @param t the new number
	 */
	public void setT(int t) {
		this.t = t;
		debug("T changed to: "+t);
	}

	/**
	 * @see Main#t t
	 * @return the number
	 */
	public long getT() {
		return t;
	}

	/**
	 * @see Main#k k
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @see Main#k k
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
		debug("K changed to: "+k);
	}
	
	
	/**
	 * adds a particle object to the queue and the particle pool
	 * also tells the threadpool to check if it needs start new threads
	 * @param particle the particle object to be added
	 */
	private void addParticle(Particle particle){
		q.offer(particle);
		particles.add(particle);
		pool.update();
	}

	/**
	 * add new parameterized {@link Particle}
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 * @param name
	 * @see Particle#Particle(int, int, int, int, char, int)
	 * @see Main#addParticle(Particle)
	 */
	public void addParticle(int x, int y, int dx, int dy, char name)
	{
		addParticle(new Particle(x,y,dx,dy,name,round.getRoundNumber()+1));
	}


	/**
	 * add new random {@link Particle}
	 * @see Particle#Particle(int)
	 * @see Main#addParticle(Particle)
	 */
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
	
	/**
	 * debug messages switchable with DEBUG boolean
	 * @param message
	 */
	public static void debug(String message)
	{
		if (DEBUG)
		{
			System.out.println(Thread.currentThread().getId()+": "+message);
		}
	}
}

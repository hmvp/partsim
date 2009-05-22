// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ThreadPool extends Thread {

	/**
	 * collection of animation threads
	 * not thread safe but since where inside this monitor all is fine
	 */
	private final Collection<Animation> pool = new HashSet<Animation>();
	
	/**
	 * maximum number of animation threads in this pool
	 */
	private volatile int pMax = 0;

	/**
	 * reference to main program used to pass on to animation threads
	 */
	private final Main main;

	/**
	 * default constructor
	 * @param main reference to main
	 */
	public ThreadPool(Main main) {
		this.main = main;
		start();
	}

	/** 
	 * set the maximum number of threads
	 * @param i the maximum number of threads
	 */
	public synchronized void setPMax(int i) {
		Main.debug("Pmax changed to:" + i);
		pMax = i;
		notify();
	}

	/**
	 * update threadpool, start new threads if necessary or stop them if necessary
	 */
	public synchronized void run() {
		while (true) {
			
			//check of we wat moeten doen, order is het aantal threads dat er bij of af moet
			int order = pMax - pool.size();

			//moeten er threads af? iterate over the threads until we killed enough
			if (order <= 0) {
				Main.debug("update p, decrease threads: " + order);
				for (Iterator<Animation> iter = pool.iterator(); order < 0 && iter.hasNext(); order++) {
					iter.next().finish();
				}
			//moeten er threads bij? maak er net zoveel aan tot we genoeg hebben of
			//we aan het aantal particles zitten
			} else {
				Main.debug("update p, increase threads: " + order);
				for (; order > 0 && pool.size() < main.particles.size(); order--) {
					Animation a = new Animation(main);
					pool.add(a);
					a.start();
				}
			}
			
			//ready? sleep until someone tells us to wake
			try {
				wait();
			} catch (InterruptedException e) {}

		}
	}

	/**
	 * remove a thread from the pool, only used by animation threads who need to stop
	 * @param t
	 */
	protected synchronized void removeThread(Animation t) {
		Main.debug("thread removes itself, thread: " + t.getId());
		if (!pool.remove(t)) {
			throw new RuntimeException("Thread is niet in pool!!!!");
		}
	}

	/**
	 * Get the number of threads in the pool used for painting that number
	 * @return number of threads
	 */
	public synchronized int size() {
		return pool.size();
	}
	
	
	/**
	 * geef andere code de gelegenheid om de threadpool thread wakker te maken. 
	 * Bijvoorbeeld als er een particle is toegevoegd en er wellicht een thread bij mag.
	 */
	public synchronized void update()
	{
		notify();
	}
}

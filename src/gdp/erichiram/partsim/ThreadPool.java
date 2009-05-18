/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ThreadPool extends Thread {

	/**
	 * collection of animation threads
	 */
	private final Collection<Animation> pool = new LinkedList<Animation>();
	
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
			
			int order = pMax - pool.size();

			if (order <= 0) {
				Main.debug("update p, decrease threads: " + order);
				for (Iterator<Animation> iter = pool.iterator(); order < 0 && iter.hasNext(); order++) {
					iter.next().finish();
				}

			} else {
				Main.debug("update p, increase threads: " + order);
				for (; order > 0 && pool.size() < main.getParticles().size(); order--) {
					Animation a = new Animation(main, this);
					pool.add(a);
					a.start();
				}
			}
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

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

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.LinkedList;

public class Animation extends Thread {
	private Main main;
	private volatile boolean run = true;
	private ThreadPool pool;
	private Round round;

	public Animation(Main main, ThreadPool pool) {
		this.main = main;
		this.pool = pool;
		this.round = main.round;
	}

	/**
	 * Main animation method, updates the particles
	 */
	public void run() {
		while (run) {
			Collection<Particle> workingset = new LinkedList<Particle>();
			boolean nextround = true;
			for (int i = main.getK(); i > 0; i--) {
				Particle p = main.q.poll();
				if (p != null) {
					nextround = false;

					workingset.add(p);
					Main.debug("got particle" + p);
				}
			}
			if(main.getK() > workingset.size())
			{
				nextround = true;
			}
			
			// check the first particle in the queue
			for (Particle current : workingset) {
				// update the particle
				current.move();
				// have some sleep
				try {
					sleep(main.getT());
				} catch (InterruptedException ignore) {
				}
			}

			//save the round number for the next round
			int nextroundnr = main.round.getRoundNumber()+1;
			
			main.q.addAll(workingset);
			Main.debug("done working on particles");
			
			//call for next round if we didn't get any particles
			if (nextround)
				round.nextRound(nextroundnr);
		}
		pool.removeThread(this);
	}

	public void finish() {
		run = false;
	}
}

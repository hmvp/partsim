/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Worker thread which updates the particles 
 *
 */
public class Animation extends Thread {
	
	/**
	 * reference to main, needed for the shared data
	 */
	private Main main;
	
	/**
	 * boolean used to determine if the thread needs to keep running
	 */
	private volatile boolean run = true;

	/**
	 * constructor needs a reference to main
	 * @see Animation#main
	 * @param main
	 */
	public Animation(Main main) {
		this.main = main;
	}

	/**
	 * Main animation method, updates the particles
	 * runs while run is true. 
	 * As soon as it is false it finishes its business and removes itself from the threadpool
	 */
	public void run() {
		Collection<Particle> workingset;
		
		while (run) {			
			//create an workingset and iterate over k until we have enough particles
			workingset = new LinkedList<Particle>();
			for (int i = main.getK(); i > 0; i--) {
				Particle p = main.q.poll();
				
				//check if its null add to the queue otherwise
				if (p != null) {
					workingset.add(p);
					Main.debug("got particle" + p);
				}
			}
			
			//iterate over the particles to do the moves
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
			
			//call for next round if we didn't get particles
			if (workingset.isEmpty())
				main.round.nextRound(nextroundnr);
		}
		
		//where finished, do cleanup
		main.tpool.removeThread(this);
		
		//there is a possibility that this is the last thread not sleeping. 
		//just to make sure we wake one
		synchronized (main.round) {
			main.round.notify();
		}
	}

	/**
	 * use this to kill this {@link Animation} thread
	 */
	public void finish() {
		run = false;
	}
}

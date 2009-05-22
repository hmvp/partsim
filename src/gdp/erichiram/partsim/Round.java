// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

/**
 * Keeps track of the rounds to make sure that threads do go 
 * to the next while there is work to be done in this one
 *
 */
public class Round {
	
	/**
	 * the round number
	 */
	private volatile int round = Main.initialRound;
	
	/**
	 * reference to main
	 */
	private final Main m;
	
	/**
	 * number of threads that sleep.
	 */
	private int sleepers = 0;
	
	/**
	 * constructor
	 * @param m {@link Main}
	 * 
	 */
	public Round(Main m)
	{
		this.m = m;
	}

	/**
	 * go to the next round if all particles are in the queue
	 * (not currently being updated by a thread), wait otherwise
	 * we check if everything is in the queue by checking if 
	 * every thread is sleeping except this one.
	 * @param nextroundnr 
	 */
	public synchronized void nextRound(int nextroundnr) {
		
		//if a thread wakes up it is possible that it still wants to check the round
		//this way we tell it that doesnt need to increase the round nr
		if(round == nextroundnr)
		{
			return;
		}
		
		//check if we are the last thread awake and bump the round
		//we also need to wake everyone up
		if (sleepers+1 == m.tpool.size()) {
			round++;
			notifyAll();
			Main.debug("================= Round " + round + " ====================");
		//to bad, not the last one we count ourself as sleeper and go sleep
		} else {
			Main.debug("-------------- Just wait a bit! --------------------");
			
			//increment the sleepers count
			sleepers++;
			try {
				wait();
			} catch (InterruptedException e) {}
			//we are done sleeping
			sleepers--;
		}
	}

	/**
	 * return the roundnumber so we can use it
	 * @return
	 */
	public int getRoundNumber() {
		return round;
	}

}

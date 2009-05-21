/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

public class Round {
	
	private volatile int round = Main.initialRound;
	private final Main m;
	private int sleepers = 0;
	
	public Round(Main m)
	{
		this.m = m;
	}

	/**
	 * go to the next round if all particles are in the queue
	 * (not currently being updated by a thread), wait otherwise
	 * @param nextroundnr 
	 */
	public synchronized void nextRound(int nextroundnr) {
		if(round == nextroundnr)
		{
			return;
		}
		if (sleepers+1 == m.pool.size()) {
			round = m.q.peek().getRound();
			notifyAll();
			Main.debug("================= Round " + round + " ====================");
		} else {
			Main.debug("-------------- Just wait a bit! --------------------");
			sleepers++;
			try {
				wait();
			} catch (InterruptedException e) {}
			sleepers--;
		}
	}

	public int getRoundNumber() {
		return round;
	}

}

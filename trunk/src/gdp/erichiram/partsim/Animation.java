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

	public void run() {
		while (run) {
			Collection<Particle> workingset = new LinkedList<Particle>();
			for(int i = Main.k;i>0;i--)
			{
				Particle p = main.getQ().poll();
				if(p != null && p.process())
				{
					workingset.add(p);
					Main.debug("got particle" + p);
				}
			}
			
			boolean nextround = workingset.isEmpty();
			
			int nextroundnr = 0;
			// check the first particle in the queue
			for(Particle current : workingset)
			{
				// move a particle if it's in the current round
				if ( round.getRoundNumber() == current.getRound() ) {
					// update the particle
					current.move();
				} else {
					nextround  = true;
					nextroundnr = current.getRound();
				}
				
				// have some sleep
				try {
					sleep(main.getT());
				} catch (InterruptedException ignore) {}
			}
			
			main.getQ().addAll(workingset);
			Main.debug("done working on particles");
			if(nextround)
				round.nextRound(nextroundnr);
		}
		pool.removeThread(this);
	}

	public void finish() {
		run = false;
	}
}

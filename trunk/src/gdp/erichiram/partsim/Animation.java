/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.LinkedList;


public class Animation extends Thread {
	private Main m;
	private boolean run = true;

	public Animation(Main main) {
		m = main;
	}

	public void run() {
		while (run) {
			Collection<Particle> workingset = new LinkedList<Particle>();
			for(int i = m.getK();i>0;i--)
			{
				Particle p = m.getQ().poll();
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
				if ( m.getRound().getRoundNumber() == current.getRound() ) {
					// update the particle
					current.move();
				} else {
					nextround  = true;
					nextroundnr = current.getRound();
				}
				
				// have some sleep
				try {
					sleep(m.getT());
				} catch (InterruptedException ignore) {}
			}
			
			m.getQ().addAll(workingset);
			Main.debug("done working on particles");
			if(nextround)
				m.nextRound(nextroundnr);
		}
		m.getPool().removeThread(this);
	}

	public void finish() {
		run = false;
	}
}

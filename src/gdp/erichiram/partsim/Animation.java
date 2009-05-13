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
				}
			}
			
			boolean nextround = false;
			
			// check the first particle in the queue
			for(Particle current : workingset)
			{
				// move a particle if it's in the current round
				if ( m.getRound().isCurrentRound(current)) {
						// update the particle
						current.move();
				} else {
					nextround  = true;
				}
				
				// have some sleep
				try {
					sleep(m.getT());
				} catch (InterruptedException ignore) {}
			}
			
			m.getQ().addAll(workingset);
			if(nextround)
				m.nextRound();
		}
		m.getPool().removeThread(this);
	}

	public void finish() {
		run = false;
	}
}

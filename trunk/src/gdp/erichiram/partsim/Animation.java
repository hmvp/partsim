package gdp.erichiram.partsim;

import java.util.LinkedList;
import java.util.Queue;

public class Animation extends Thread {
	private Main m;

	public Animation(Main main) {
		m = main;
	}

	public void run() {
		while (true) {
			Particle current = null;

			// get a particle from the queue
			current = m.getQ().poll();

			// check for empty queue
			if (current != null) {

				// move a particle if it's in the current round
				if ( m.getRound() == current.getRound() ) { // || (m.getRound() - 1) == current.getRound() ) {
					current.move();
				} else {
					// let the main method know we're going to the next round,
					// if all is well this happens once per round (TODO echhhttt???)
					m.nextRound();
				}

				// whatever happens put the particle back into the queue
				m.getQ().offer(current);
				

				// have some sleep
				try {
					sleep(m.getT());
				} catch (InterruptedException ignore) {

				}
			}
		}
	}
}

package gdp.erichiram.partsim;

public class Animation extends Thread {
	private Main m;
	private boolean run = true;

	public Animation(Main main) {
		m = main;
	}

	public void run() {
		while (run) {
			Particle current = null;

			// check the first particle in the queue
			current = m.getQ().peek();

			// check if it was there
			if (current != null) {

				// move a particle if it's in the current round
				if ( m.getRound() == current.getRound() ) {

					// get the particle from the queue
					current = m.getQ().poll();
					
					// check if the queue has gone empty in the mean time
					if ( current != null ) {
						
						// update the particle
						current.move();
					}

					// put the particle back into the queue
					m.getQ().offer(current);
					
				} else {
					// if all particles have been put back into the queue
					// let the main method know we're going to the next round
					m.nextRound();
					
					// TODO there is a bug here where we go to the next round
					// when the particles for the current round haven't been updated
					// most likely when nextRound gets called multiple times from different threads
					// within the same round => need synchronizing
				}
				

				// have some sleep
				try {
					sleep(m.getT());
				} catch (InterruptedException ignore) {

				}
			} else {
				Main.debug("Queue is empty when peeking!");
			}
		}
		m.getPool().removeThread(this);
	}

	public void finish() {
		run = false;
	}
}

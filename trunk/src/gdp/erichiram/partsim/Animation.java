package gdp.erichiram.partsim;

public class Animation extends Thread {
	private Main m;

	public Animation(Main main) {
		m = main;
	}

	public void run() {
		while (true) {
			Particle current = null;

			// try {
			current = m.getQ().poll();
			// } catch (InterruptedException e) {
			// System.err.println("Animation thread with id [" + getId() +
			// "] was interrupted!");
			// }

			/*
			 * TODO
			 * Maintain a queue Q of N particles and let at the beginning of a
			 * round each thread take k particles from Q. If the thread has done
			 * the computation for the assigned particles, it puts them back
			 * into Q, and if some particles must still be dealt with in this
			 * round, the thread proceeds with at least one of them. Make sure
			 * that all particles are dealt with in a given round.
			 */

			if (current != null) {
				current.move();
				m.getQ().offer(current);
				try {
					sleep(m.getT());
				} catch (InterruptedException ignore) {
				}
			}
		}
	}
}

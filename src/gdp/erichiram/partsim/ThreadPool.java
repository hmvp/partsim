package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ThreadPool extends Thread {

	private Collection<Animation> pool = new LinkedList<Animation>();
	private int pMax = 0;

	private Main main;

	public ThreadPool(Main main) {
		this.main = main;
		this.start();
	}

	public synchronized void setPmax(int i) {
		Main.debug("Pmax changed to:" + i);
		pMax = i;
		notify();
	}

	public synchronized void run() {
		while (true) {
			
			int order = pMax - pool.size();

			if (order <= 0) {
				Main.debug("update p, decrease order: " + order);
				for (Iterator<Animation> iter = pool.iterator(); order < 0
						&& iter.hasNext(); order++) {
					iter.next().finish();
				}

			} else {
				Main.debug("update p, increase order: " + order);
				for (; order > 0 && pool.size() < main.getParticles().size(); order--) {
					Animation a = new Animation(main);
					pool.add(a);
					a.start();
				}
			}
			try {
				wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public synchronized void removeThread(Animation t) {
		Main.debug("thread removes itself, thread: " + t.getId());
		if (!pool.remove(t)) {
			throw new RuntimeException("Thread is niet in pool!!!!");
		}
	}

	public synchronized int size() {
		return pool.size();
	}
}

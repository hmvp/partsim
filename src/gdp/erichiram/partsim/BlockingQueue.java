/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<T> {
	private Queue<T> q = new LinkedList<T>();
	private int polls = 0;

	public synchronized void addAll(Collection<T> c) {
		q.addAll(c);
	}

	public synchronized int size() {
		return q.size();
	}

	public synchronized void offer(T t) {
		q.offer(t);		
	}

	public synchronized T poll() {
		
		return q.poll();
	}

	public synchronized T peek() {
		return q.peek();
	}

}

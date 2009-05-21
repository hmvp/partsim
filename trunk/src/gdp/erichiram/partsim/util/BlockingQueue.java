/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<T> implements Queue<T> {
	private Queue<T> q = new LinkedList<T>();

	public synchronized boolean addAll(Collection<? extends T> c) {
		return q.addAll(c);
	}

	public synchronized int size() {
		return q.size();
	}

	public synchronized boolean offer(T t) {
		return q.offer(t);
	}

	public synchronized T poll() {
		
		return q.poll();
	}

	public synchronized T peek() {
		return q.peek();
	}

	public synchronized boolean add(T o) {
		return offer(o);
	}
	

	public synchronized Iterator<T> iterator() {
		return q.iterator();
	}
	
	public synchronized Object[] toArray() {
		return q.toArray();
	}

	public synchronized <U> U[] toArray(U[] a) {
		return q.toArray(a); 
	}
	
	public T element() {
		throw new UnsupportedOperationException();
	}

	public T remove() {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
		
	}

	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
		
	}

	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
		
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
		
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
		
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
		
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
		
	}
}

package gdp.erichiram.partsim;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class SynchronizedQueue<T> implements Queue<T>{
	private Queue<T> q;

	public SynchronizedQueue() {
		q = new LinkedList<T>();
	}
	
	public SynchronizedQueue(Collection<? extends T> c) {
		q = new LinkedList<T>(c);
	}
	
//	public SynchronizedQueue(int i) {
//		q = new LinkedList<T>();
//	}
	
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Queue#add(java.lang.Object)
	 */
	public synchronized boolean add(T arg0) {
		return q.add(arg0);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public synchronized boolean addAll(Collection<? extends T> c) {
		return q.addAll(c);
	}

	/**
	 * 
	 * @see java.util.Collection#clear()
	 */
	public synchronized void clear() {
		q.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public synchronized boolean contains(Object o) {
		return q.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public synchronized boolean containsAll(Collection<?> c) {
		return q.containsAll(c);
	}

	/**
	 * @return
	 * @see java.util.Queue#element()
	 */
	public synchronized T element() {
		return q.element();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Collection#equals(java.lang.Object)
	 */
	public synchronized boolean equals(Object o) {
		return q.equals(o);
	}

	/**
	 * @return
	 * @see java.util.Collection#hashCode()
	 */
	public synchronized int hashCode() {
		return q.hashCode();
	}

	/**
	 * @return
	 * @see java.util.Collection#isEmpty()
	 */
	public synchronized boolean isEmpty() {
		return q.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Collection#iterator()
	 */
	public synchronized Iterator<T> iterator() {
		return q.iterator();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	public synchronized boolean offer(T o) {
		return q.offer(o);
	}

	/**
	 * @return
	 * @see java.util.Queue#peek()
	 */
	public synchronized T peek() {
		return q.peek();
	}

	/**
	 * @return
	 * @see java.util.Queue#poll()
	 */
	public synchronized T poll() {
		return q.poll();
	}

	/**
	 * @return
	 * @see java.util.Queue#remove()
	 */
	public synchronized T remove() {
		while(q.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		return q.remove();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public synchronized boolean remove(Object o) {
		return q.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public synchronized boolean removeAll(Collection<?> c) {
		return q.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public synchronized boolean retainAll(Collection<?> c) {
		return q.retainAll(c);
	}

	/**
	 * @return
	 * @see java.util.Collection#size()
	 */
	public synchronized int size() {
		return q.size();
	}

	/**
	 * @return
	 * @see java.util.Collection#toArray()
	 */
	public synchronized Object[] toArray() {
		return q.toArray();
	}

	/**
	 * @param <T>
	 * @param a
	 * @return
	 * @see java.util.Collection#toArray(T[])
	 */
	public synchronized <T> T[] toArray(T[] a) {
		return q.toArray(a);
	}
}

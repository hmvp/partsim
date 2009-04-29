package gdp.erichiram.partsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SynchronizedQueue<T> implements java.lang.Iterable<T> {
	private volatile Node<T> head, tail;

	public SynchronizedQueue() {
	}

	/**
	 * @param o
	 */
	public synchronized void offer(T o) {
		Node<T> node = new Node<T>(o);

		if (head == null) {
			head = tail = node;
		} else {
			tail.setNext(node);
			tail = node;
		}

		notify();
	}

	/**
	 * @return
	 * @throws InterruptedException
	 * @see java.util.Queue#poll()
	 */
	public synchronized T poll() throws InterruptedException {
		while (head == null) {
			wait();
		}

		T data = head.getData();
		head = head.getNext();
		return data;
	}

	public void addAll(Collection<T> c) {
		for (T o : c) {
			offer(o);
		}
	}

	public Iterator<T> iterator() {

		return new Iterator<T>() {
			private Iterator<T> iterator;

			{
				ArrayList<T> array = new ArrayList<T>();
				Node<T> current = head;
				while (current != null) {
					array.add(current.getData());
					current = current.getNext();
				}
				iterator = array.iterator();
			}

			public boolean hasNext() {
				return iterator.hasNext();
			}

			public T next() {
				return iterator.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}
		};
	}
}

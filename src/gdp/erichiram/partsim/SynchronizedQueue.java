package gdp.erichiram.partsim;

public class SynchronizedQueue<T> {
	private volatile Node<T> head, tail;

	public SynchronizedQueue() {
	}

	/**
	 * @param o
	 */
	public synchronized void offer(T o) {
		Node<T> node = new Node<T>(o);
		
		if ( head == null ) {
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
		while ( head == null ) {
			wait();
		}
		
		T data = head.getData();
		head = head.getNext();
		return data;
	}
}

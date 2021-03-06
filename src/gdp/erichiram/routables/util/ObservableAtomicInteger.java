// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

package gdp.erichiram.routables.util;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is an encapsulated AtomicInteger that inherits from Observable.
 * it is ok that the methods are not synchronized since it is not important that 
 * the observers get the value for that increment but the most uptodat value.
 * AtomicInteger is ofcourse in itself already thread safe.
 * @author Hiram van Paassen, Eric Broersma
 *
 */
public class ObservableAtomicInteger extends Observable {

	private static final long serialVersionUID = -347239202648154176L;

	private AtomicInteger atomicInteger;

	public ObservableAtomicInteger(int value) {
		atomicInteger = new AtomicInteger(value);
	}

	public final int get() {
		return atomicInteger.get();
	}

	public final void set(int newValue) {
		atomicInteger.set(newValue);
		setChanged();
		notifyObservers(get());
	}

	public void increment() {
		atomicInteger.incrementAndGet();
		setChanged();
		notifyObservers(get());
	}
}

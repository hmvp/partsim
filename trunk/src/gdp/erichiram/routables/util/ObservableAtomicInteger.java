package gdp.erichiram.routables.util;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

public class ObservableAtomicInteger extends Observable {

	private static final long serialVersionUID = -347239202648154176L;

	private AtomicInteger atomicInteger;

	public ObservableAtomicInteger(int value) {
		atomicInteger = new AtomicInteger(value);
	}

	public synchronized final int get() {
		return atomicInteger.get();
	}

	public synchronized final void set(int newValue) {
		atomicInteger.set(newValue);
		setChanged();
		notifyObservers(get());
	}

	public synchronized void increment() {
		atomicInteger.incrementAndGet();
		setChanged();
		notifyObservers(get());
	}
}

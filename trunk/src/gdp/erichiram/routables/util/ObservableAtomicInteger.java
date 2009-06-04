package gdp.erichiram.routables.util;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

public class ObservableAtomicInteger {

	/**
	 * 
	 */
	private static final long serialVersionUID = -347239202648154176L;

	private AtomicInteger atomicInteger;

	private class MyObservable extends Observable {
		public void clearChanged() {
			super.clearChanged();
		}

		public void setChanged() {
			super.setChanged();
		}
	};

	private MyObservable observable = new MyObservable();

	public ObservableAtomicInteger(int value) {
		atomicInteger = new AtomicInteger(value);
	}

	public final int get() {
		return atomicInteger.get();
	}

	public final void set(int newValue) {
		atomicInteger.set(newValue);
		observable.setChanged();
		observable.notifyObservers(this);
	}

	// new method to get access to observable
	public Observable observable() {
		return observable;
	}

	public void increment() {
		atomicInteger.incrementAndGet();
		observable.setChanged();
		observable.notifyObservers(this);
	}
}

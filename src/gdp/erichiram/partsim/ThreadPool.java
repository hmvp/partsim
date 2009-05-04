package gdp.erichiram.partsim;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool extends Thread{

	private Collection<Animation> pool = new LinkedList<Animation>();
	private int pMax = 0;
	
	private LinkedBlockingQueue<Integer> orders = new LinkedBlockingQueue<Integer>();
	private Main main;
	
	public ThreadPool(Main main){
		this.main = main;
		this.start();
	}
	
	public synchronized void setPmax(int i){
		Main.debug("Pmax changed to:" + i);
		orders.offer(pMax - i);
		pMax = i;
		notify();
	}
	
	public synchronized void run(){
		while(true)
		{
			Integer order = orders.poll();
			if(order != null)
			{
				
				if(order >= 0)
				{
					for(Iterator<Animation> iter = pool.iterator(); order > 0 && iter.hasNext(); order--)
					{
						iter.next().finish();
					}
				} else 
				{
					for(;order<0;order++)
					{
						Animation a = new Animation(main);
						pool.add(a);
						a.start();
					}
				}
			} else
			{
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void removeThread(Animation t)
	{
		Main.debug("thread removes itself, thread: "+ t.getId());
		if (!pool.remove(t))
		{
			throw new RuntimeException("Thread is niet in pool!!!!");
		}
	}

	public synchronized int size() {
		return pool.size();
	}
}

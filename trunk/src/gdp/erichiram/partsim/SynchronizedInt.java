package gdp.erichiram.partsim;

public class SynchronizedInt {

	private int i;

	public synchronized int decrease()
	{
		return --i;
	}
	
	public synchronized int increase()
	{
		return ++i;
	}
	
	public synchronized int get()
	{
		return i;
	}
	
	public synchronized int testIncrAndDo(TestIncrAndDo tiad){
		if (tiad.test(i))
		{
			increase();
			tiad.exec();
		}
		return i;
	}
		
}


abstract class TestIncrAndDo {

	public abstract boolean test(int i);

	public abstract void exec();
	
}
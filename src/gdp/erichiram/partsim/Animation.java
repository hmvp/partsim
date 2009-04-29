package gdp.erichiram.partsim;

public class Animation extends Thread{
	private Main m;

	public Animation(Main main) {
		m = main;
	}

	public void run (){
		while (true)
		{
			Particle current = m.getQ().poll();
			if (current != null) {
			current.move();
			m.getQ().offer(current);
			try {
				sleep(m.getT());
			} catch (InterruptedException ignore) {}}
		}
	}
}

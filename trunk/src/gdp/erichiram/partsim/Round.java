package gdp.erichiram.partsim;

public class Round {
	
	private int round;
	private Main m;
	
	public Round(Main m)
	{
		this.m = m;
	}

	public synchronized void nextRound() {
		if ( m.getQ().size() == m.getParticles().size() ) {
			round = m.getQ().peek().getRound();
			notifyAll();
			Main.debug("============== Round " + round + " ===================");
		} else {
			Main.debug("-------------- Just wait a bit! --------------------");
			try {
				wait();
			} catch (InterruptedException e) {}
		}
	}

	public boolean isCurrentRound(Particle p) {
		// TODO Auto-generated method stub
		return round == p.getRound();
	}

	public int getRoundNumber() {
		return round;
	}

}

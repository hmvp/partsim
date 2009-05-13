package gdp.erichiram.partsim;

public class Round {
	
	private int round;
	private Main m;
	
	public Round(Main m)
	{
		this.m = m;
	}

	public synchronized void nextRound(int nextroundnr) {
		if(round == nextroundnr)
		{
			return;
		}
		if ( m.getQ().size() >= m.getParticles().size()) {
			round = m.getQ().peek().getRound();
			notifyAll();
			Main.debug("================= Round " + round + " ====================");
		} else {
			Main.debug("-------------- Just wait a bit! --------------------");
			try {
				wait();
			} catch (InterruptedException e) {}
		}
	}

	public int getRoundNumber() {
		return round;
	}

}

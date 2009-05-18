/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

public class Particle {

	private volatile int x;
	private volatile int y;
	private volatile int dx;
	private volatile int dy;
	private final char name;
	
	private volatile int round;
	
	private volatile long threadId;
	private volatile boolean dead = false;
	
	public Particle(int x, int y, int dx, int dy, char name, int round) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.name = name;
		
		this.threadId = 0;
		this.round = round;
	}
	
	public Particle(int round) {
		x = (int) (Math.random() * Main.width);
		y = (int) (Math.random() * Main.height);

		// minimum value = -799
		// maximum value = 799
		dx = (int) (Math.random() * (Main.width * 2 - 1)) - Main.width + 1;
		dy = (int) (Math.random() * (Main.height * 2 - 1)) - Main.height + 1;
		
		if ( Math.random() > 0.5 ) {
			name = (char) ('a' + Math.random() * 26);
		} else {
			name = (char) ('A' + Math.random() * 26);
		}
		
		this.round = round;
		
	}
		
	public synchronized void move() {

		Main.debug("Moving particle " + this);
		
		////// change x and dx
		x += dx;		
		
		// get "remainder" of x (rx) and the "number of bounces" (nx) using %
		int rx = x % Main.width;				
		
		// TODO this is probably borked when dx is negative and x > 0 at this point
		int nx = (x - rx) / Main.width;
		
		x = Math.abs(rx);
		dx = Math.abs(dx);
		
		// if nx is odd
		if ( (nx & 1) == 1 ) {
			// the direction is flipped and the position is mirrored
			x = (Main.width - 1) - x;
			dx = -dx;
		}

		////// change y and dy
		// (should be equivalent, mutatis mutandi, to the code block above)
		y += dy;
		
		// get "remainder" of y (ry) and the "number of bounces" (ny) using % 
		int ry = y % Main.height;
		int ny = (y - ry) / Main.height;
		
		y = Math.abs(ry);
		dy = Math.abs(dy);
		
		// if ny is odd
		if ( (ny & 1) == 1 ) {
			// the direction is flipped and the position is mirrored
			y = (Main.height - 1) - y;			
			dy = -dy;
		}
	
		++round;
		
		threadId = Thread.currentThread().getId();
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDx() {
		return dx;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public int getDy() {
		return dy;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public char getName() {
		return name;
	}
	
	public synchronized long getThreadId() {
		return threadId;
	}
	
	public String toString() {
		return "\""+ name + "\"@(" + x + "," + y + ")/" + round;
	}

	public int getRound() {
		return round;
	}

	public void die() {
		dead = true;
	}
	public boolean process()
	{
		return !dead;
	}
}

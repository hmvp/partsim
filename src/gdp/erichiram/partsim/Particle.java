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
	
	public Particle(Particle other) {
		this.x = other.x;
		this.y = other.y;
		this.dx = other.dx;
		this.dy = other.dy;
		this.name = other.name;
		
		this.threadId = 0;
		this.round = other.round;
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
		int rx = x % (Main.width - 1);
		int nx = (x - rx) / (Main.width - 1);
		
		if ( x < 0 ) {
			dx = Math.abs(dx);
		}
		
		x = Math.abs(rx);
		
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
		int ry = y % (Main.height - 1);	
		int ny = (y - ry) / (Main.height - 1);

		if ( y < 0 ) {
			dy = Math.abs(dy);
		}
		
		y = Math.abs(ry);
		
		// if ny is odd
		if ( (ny & 1) == 1 ) {
			// the direction is flipped and the position is mirrored
			y = (Main.height - 1) - y;			
			dy = -dy;
		}
	
		++round;
		
		threadId = Thread.currentThread().getId();
	}
	
	public synchronized void stupidMove() {

		Main.debug("sMoving particle " + this);
		
		////// change x and dx
		int rdx = dx;
		
		do {
			x += rdx;
			
			if ( x < 0 ) {
				rdx = -x + 1;
				x = -1;
				dx = -dx;
			} else if ( x >= Main.width ) {
				rdx = - (x - (Main.width - 2));
				x = Main.width;
				dx = -dx;
			}
			
		} while ( x < 0 || x >= Main.width );

		////// change y and dy
		int rdy = dy;
		
		do {
			y += rdy;
			
			if ( y < 0 ) {
				rdy = -y + 1;
				y = -1;
				dy = -dy;
			} else if ( y >= Main.height ) {
				rdy = - (y - (Main.height - 2));
				y = Main.height;
				dy = -dy;
			}
			
		} while ( y < 0 || y >= Main.height );
	
		++round;
		
		threadId = Thread.currentThread().getId();
	}
	
	public synchronized void smartMove() {

		Main.debug("Moving particle " + this);
		
		////// change x and dx
		x += dx;		
		
		if ( x < 0 || x >= Main.width ) {
			
			// get "remainder" of x (rx) and the "number of bounces" (nx) using %
			int rx = x % (Main.width - 1);
			int nx = (x - rx) / (Main.width - 1);
			
			if ( x < 0 ) {
				dx = Math.abs(dx);
			}
			
			x = Math.abs(rx);
			
			// if nx is odd
			if ( (nx & 1) == 1 ) {
				// the direction is flipped and the position is mirrored
				x = (Main.width - 1) - x;
				dx = -dx;
			}
		}
		
		////// change y and dy
		// (should be equivalent, mutatis mutandi, to the code block above)
		y += dy;
		
		if ( y < 0 || y >= Main.height ) {
				
			// get "remainder" of y (ry) and the "number of bounces" (ny) using % 
			int ry = y % (Main.height - 1);	
			int ny = (y - ry) / (Main.height - 1);
	
			if ( y < 0 ) {
				dy = Math.abs(dy);
			}
			
			y = Math.abs(ry);
			
			// if ny is odd
			if ( (ny & 1) == 1 ) {
				// the direction is flipped and the position is mirrored
				y = (Main.height - 1) - y;			
				dy = -dy;
			}
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
		return "\""+ name + "\"@(" + x + "," + y + ")+(" + dx + "," + dy + ")/" + round;
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

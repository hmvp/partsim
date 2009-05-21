/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;


/**
 * Particle is the class that represents a particle in the simulation.
 * it is very simple. 
 * It can be accessed concurrently by the animation threads that move it
 * and by the Gui for rendering.
 * the funny thing is that although none of the fields need the synchronized statements
 * (all read and writes to volatile fields or simple types (except long and double) are atomic)
 * we do need the synchronization to keep the particle from displaying strange
 * e.g. gui shows old X with new Y 
 */
public class Particle {

	/**
	 * private vars of particle
	 */
	
	/**
	 * the x coordinate
	 */
	private volatile int x;
	
	/**
	 * the y coordinate
	 */
	private volatile int y;
	
	/**
	 * the speed in x direction (horizontal)
	 */
	private volatile int dx;
	
	/**
	 * the speed in y direction (vertical)
	 */
	private volatile int dy;
	
	/**
	 * the not so unique name of the particle
	 */
	private final char name;
	
	/**
	 * the round the particle is in atm.
	 */
	private volatile int round;
	
	/**
	 * the id of the last thread that has moved this particle
	 */
	private volatile long threadId;
	
	/**
	 * boolean that tells if the particle is pending for removal and thus should be removed
	 */
	private volatile boolean dead = false;
	
	/**
	 * create particle with every parameter possible
	 * @param x coordinate
	 * @param y coordinate
	 * @param dx the speed in horizontal direction
	 * @param dy the speed in vertical direction
	 * @param name of the particle
	 * @param round the particle starts in
	 */
	public Particle(int x, int y, int dx, int dy, char name, int round) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.name = name;
		
		this.threadId = 0;
		this.round = round;
	}
	
	/**
	 * create a particle form another particle
	 * @param other particle
	 */
	public Particle(Particle other) {
		this.x = other.x;
		this.y = other.y;
		this.dx = other.dx;
		this.dy = other.dy;
		this.name = other.name;
		
		this.threadId = 0;
		this.round = other.round;
	}
	
	/**
	 * create a random particle
	 * @param round the particle is starting in
	 */
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
		
	/**
	 * the move of this particle, 
	 * this method updates almost every bit of this particle
	 * some heavy math is going on here ;-)
	 */
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
	
	/**
	 * another {@link Particle#move() move} operation 
	 * this one uses a loop to calculate
	 * @see Particle#move() move
	 */
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
	
	/**
	 * another move operation
	 * this one is very similar to {@link Particle#move() move} 
	 * but uses an extra test to check if all the heavy math is needed
	 * @see Particle#move() move
	 */
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
	
	public synchronized int getX() {
		return x;
	}

	public synchronized int getY() {
		return y;
	}

	public synchronized void setDx(int dx) {
		this.dx = dx;
	}
	
	public synchronized int getDx() {
		return dx;
	}
	
	public synchronized void setDy(int dy) {
		this.dy = dy;
	}
	
	public synchronized int getDy() {
		return dy;
	}

	public char getName() {
		return name;
	}
	
	public synchronized long getThreadId() {
		return threadId;
	}
	
	public synchronized String toString() {
		return "\""+ name + "\"@(" + x + "," + y + ")+(" + dx + "," + dy + ")/" + round;
	}

	public synchronized int getRound() {
		return round;
	}

	/**
	 * mark particle for removal
	 */
	public void die() {
		dead = true;
	}
	
	/**
	 * check if particle is not pending for removal
	 * @return true if the particle is alive and needs to be processed
	 */
	public boolean process()
	{
		return !dead;
	}
}

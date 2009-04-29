package gdp.erichiram.partsim;

public class Particle {

	private int x;
	private int y;
	private int dx;
	private int dy;
	private final char name;
	
	private int round = 0;
	
	private long threadId;
	
	public Particle(int x, int y, int dx, int dy, char name) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.name = name;
		
		this.threadId = 0;
	}
	
	public Particle() {
		x = (int) (Math.random() * Main.rWidth);
		y = (int) (Math.random() * Main.rHeight);

		dx = (int) (Math.random() * (Main.rWidth * 2 - 1)) - Main.rWidth + 1;
		dy = (int) (Math.random() * (Main.rHeight * 2 - 1)) - Main.rHeight + 1;
		
		if ( Math.random() > 0.5 ) {
			name = (char) ('a' + Math.random() * 26);			
		} else {
			name = (char) ('A' + Math.random() * 26);
		}
	}
	
	public void move() {
		Main.debug("moving particle: " + this);
		
		assert dx < Main.rWidth;
		assert dy < Main.rHeight;
		
		x += dx;
		y += dy;
		
		
		if ( x < 0 ) {
			x = -x;
			dx = -dx;
		}
		
		if ( y < 0 ) {
			y = -y;
			dy = -dy;
		}
		
		if ( x > Main.rWidth ) {
			x = Main.rWidth - (x - Main.rWidth); 
			dx = -dx;
		}
		
		if ( y > Main.rHeight ) {
			y = Main.rHeight - (y - Main.rHeight);
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
	
	public long getThreadId() {
		return threadId;
	}
	
	public String toString() {
		return name + "[" + x + ", " + y + "] ROUND=" + round;
	}
}

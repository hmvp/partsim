package gdp.erichiram.partsim;

public class Particle {

	private int x;
	private int y;
	private int dx;
	private int dy;
	private final char name;
	
	private int round;
	
	private long threadId;
	private boolean dead = false;
	
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

		dx = (int) (Math.random() * (Main.width * 2 - 1)) - Main.width + 1;
		dy = (int) (Math.random() * (Main.height * 2 - 1)) - Main.height + 1;
		
		if ( Math.random() > 0.5 ) {
			name = (char) ('a' + Math.random() * 26);			
		} else {
			name = (char) ('A' + Math.random() * 26);
		}
		
		this.round = round;
		
	}
		
	public void move() {
		Main.debug("Moving particle " + this);

		// change x and dx
		x += dx;		
		
		int rx = x % Main.width;				
		int ax = (x - rx) / Main.width;
		
		x = Math.abs(rx);
		dx = Math.abs(dx);
		
		// if ax is odd
		if ( (ax & 1) == 1 ) {
			x = Main.width - x;
			dx = -dx;
		}

		// change y and dy (should be equivalent, mutatis mutandi to the code block above)
		y += dy;
		
		int ry = y % Main.height;
		int ay = (y - ry) / Main.height;
		
		y = Math.abs(ry);
		dy = Math.abs(dy);
		
		// if ay is odd
		if ( (ay & 1) == 1 ) {
			y = Main.height - y;			
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

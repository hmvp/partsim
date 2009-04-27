package gdp.erichiram.partsim;

public class Particle {

	private int x;
	private int y;
	private int dx;
	private int dy;
	private char name;
	
	public Particle(int x, int y, int dx, int dy, char name) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.name = name;
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

	public void setName(char name) {
		this.name = name;
	}
}

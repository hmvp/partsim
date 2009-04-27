package gdp.erichiram.partsim;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

public class Gui implements Runnable, ActionListener {

	private Main m;
	private JFrame frame;
	private Canvas canvas;
	private Timer timer;

	public Gui(Main m) {
		this.m = m;
	}

	private void initializeGui() {
		// TODO Auto-generated method stub
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel emptyLabel = new JLabel("Blah!");
		emptyLabel.setPreferredSize(new Dimension(m.rWidth, m.rHeight));

		canvas = new Canvas() {

			private static final long serialVersionUID = 5772443503354772693L;

			/**
			 * @see java.awt.Canvas#paint(java.awt.Graphics)
			 */
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				for (Particle p : m.getQ()) {					
					Random random = new Random(p.getThreadId());					
					g.setColor(Color.getHSBColor(1.0f, random.nextFloat(), 1.0f));
					g.fillRect(p.getX(), p.getY(), 2, 2);
				}
			}

		};
		frame.getContentPane().add(canvas);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		
		int speed = 100 ;
		timer = new Timer(speed, this);
		timer.setInitialDelay(0);
		timer.start();
	}

	public void run() {
		initializeGui();
	}

	public void actionPerformed(ActionEvent e) {
		canvas.repaint();
	}

}

package gdp.erichiram.partsim;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui implements Runnable {

	private Main m;
	private JFrame frame;

	public Gui(Main m) {
		this.m = m;
	}

	private void initializeGui() {
		// TODO Auto-generated method stub
		frame = createFrame();

		Canvas canvas = createCanvas();
		JSpinner pMaxSpin = createPMaxSpinner();
		JSpinner tSpin = createTSpinner();

		
		JPanel panel = new JPanel();
		panel.add(pMaxSpin);
		panel.add(tSpin);

		
		
		frame.add(canvas, BorderLayout.CENTER);
		frame.add(panel,BorderLayout.LINE_START);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	
	}

	private JSpinner createTSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(0,0,1000,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				m.setT(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the delay for the worker threads here.");
		return spin;
	}

	private JSpinner createPMaxSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(0,0,1000,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				m.setPMax(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the maximum number of worker threads here.");
		return spin;
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		return frame;
	}

	private Canvas createCanvas() {
		Canvas canvas = new Canvas() {

			private static final long serialVersionUID = 5772443503354772693L;

			{//Constructor
				Timer timer = new Timer(Main.guiSpeed, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						repaint();
					}
				});
				timer.start();
			}
			
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
		canvas.setPreferredSize(new Dimension(Main.rWidth, Main.rHeight));		
		return canvas;
	}

	public void run() {
		initializeGui();
	}


}

package gdp.erichiram.partsim;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Gui extends Thread {
	
	private Main m;
	private JFrame frame;
	private Canvas canvas;
	
	
	public Gui(Main m)
	{
		this.m = m;
		initializeGui();
		
	}

	private void initializeGui() {
		// TODO Auto-generated method stub
		frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel emptyLabel = new JLabel("Blah!");
        emptyLabel.setPreferredSize(new Dimension(m.rWidth, m.rHeight));
        
        canvas = new Canvas();
        frame.getContentPane().add(canvas);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	public void run()
	{
		Graphics g = canvas.getGraphics();
		while ( true ) {
			for ( Particle p : m.getQ() ) {
				System.out.println("Painting " + p);
				
				g.drawRect(p.getX(), p.getY(), 2, 2);
			}
		}
	}

}

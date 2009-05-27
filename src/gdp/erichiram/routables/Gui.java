// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui implements Runnable {
	
	/**
	 * initalize the Gui, 
	 */
	private void initializeGui() {
		JFrame frame = createFrame();	

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private JFrame createFrame() {
		// TODO: create the gui
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(createCanvas(), BorderLayout.CENTER);
		return frame;
	}

	private Canvas createCanvas() {
		Canvas canvas = new Canvas() {

			private static final long serialVersionUID = 5772443503354772693L;

			private Map<Long, Color> colorMap = new HashMap<Long, Color>();

			private Image offscreenImage;
			private Graphics offscreenGraphics;

			{//Constructor				
				Timer timer = new Timer(Configuration.guiSpeed , new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						repaint();
					}
				});
				timer.start();
			}
			
			@Override
			public void paint(Graphics g) {

			}
		};
		
		canvas.setBackground(Color.BLACK);
		
		canvas.setPreferredSize(new Dimension(Configuration.width, Configuration.height));		
		return canvas;
	}

	public void run() {
		initializeGui();
	}


}

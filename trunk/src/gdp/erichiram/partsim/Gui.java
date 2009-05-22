/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

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

	private final Main main;
	
	private final static int SpinnerMax = 9999;
	
	public Gui(Main main) {
		this.main = main;
	}

	/**
	 * initalize the Gui, 
	 */
	private void initializeGui() {
		JFrame frame = createFrame();	

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	
	}

	private JComponent createSidePane() {
		JPanel x = new JPanel(new BorderLayout());
		JComponent p = Box.createVerticalBox();
		p.add(createParamPane());
		p.add(Box.createVerticalGlue());
		p.add(createAddPane());
		p.add(Box.createVerticalGlue());
		p.add(createRemovePane());
		p.add(Box.createVerticalGlue());
		x.add(p,BorderLayout.NORTH);
		return x;
	}

	private JPanel createRemovePane() {
		JPanel p = new JPanel(new GridBagLayout()); 
		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		
		JLabel name = new JLabel("Remove one or more particles");
		JButton remove = new JButton("Remove particle");
		final JSpinner nameSpin = createNameSpinner();
		
		remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				char myName = (nameSpin.getValue().toString()).charAt(0);
				main.removeParticles(myName);
			}
			
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 0;
		

		c.gridy++;
		p.add(remove,c);
		c.gridx++;
		p.add(nameSpin,c);
		
		c.gridy = c.gridx = 0;
		c.gridwidth = 2;
		c.insets = new Insets(0,0,10,0);
		p.add(name,c);
		
		return p;
	}
	
	private JSpinner createNameSpinner()
	{
		String[] array = new String[52];
		for(char c = 'a'; c <= 'z'; ++c)
		{
			array[c - 'a'] = String.valueOf(c);
		}
		for(char c = 'A'; c <= 'Z'; ++c)
		{
			array[c - 'A' + 26] = String.valueOf(c);
		}
		return new JSpinner(new SpinnerListModel(Arrays.asList(array)));
	}

	private JComponent createAddPane() {
		

		JLabel name = new JLabel("Add a particle");

		final JSpinner xSpin = new JSpinner(new SpinnerNumberModel(0,0,Main.width-1,1));
		final JSpinner ySpin = new JSpinner(new SpinnerNumberModel(0,0,Main.height-1,1));
		final JSpinner dxSpin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner dySpin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner nameSpin = createNameSpinner();
		
		JLabel xLabel = new JLabel("X");
		xLabel.setLabelFor(xSpin);
		JLabel yLabel = new JLabel("Y");
		yLabel.setLabelFor(ySpin);
		JLabel dxLabel = new JLabel("dX");
		dxLabel.setLabelFor(dxSpin);
		JLabel dyLabel = new JLabel("dY");
		dyLabel.setLabelFor(dySpin);
		JLabel nameLabel = new JLabel("Name");
		nameLabel.setLabelFor(nameSpin);

		
		JButton addnew = new JButton("Add new Particle");
		addnew.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Main.debug("Adding parametrized particle!");
				main.addParticle(
						((Number) xSpin.getValue()).intValue(), 
						((Number) ySpin.getValue()).intValue(), 
						((Number) dxSpin.getValue()).intValue(), 
						((Number) dySpin.getValue()).intValue(), 
						((String) nameSpin.getValue()).charAt(0)
						
				);
			}
			
		});
		
		JButton addrand = new JButton("Add new random Particle");
		addrand.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Main.debug("Adding random particle!");
				main.addParticle();
			}
			
		});
		
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.gridx = 1;
		c.gridx = 0;
		c.gridy = c1.gridy = 1;
		c.weightx = c1.weightx = 0.1;		

		
		
		p.add(xLabel,c);
		p.add(xSpin,c1);
		
		c1.gridy = ++c.gridy;
		p.add(yLabel,c);
		p.add(ySpin,c1);
		
		c1.gridy = ++c.gridy;
		p.add(dxLabel,c);
		p.add(dxSpin,c1);
		
		c1.gridy = ++c.gridy;
		p.add(dyLabel,c);
		p.add(dySpin,c1);
		
		c1.gridy = ++c.gridy;
		p.add(nameLabel,c);
		p.add(nameSpin,c1);
		c.fill = GridBagConstraints.NONE;
		
		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(addnew,c);

		c.gridy++;
		c.weighty = 0.5;
		c.insets = new Insets(10,0,0,0);
		p.add(addrand,c);
		
		c.gridy = 0;
		c.insets = new Insets(0,0,10,0);
		p.add(name,c);
		
		return p;
	}

	private JPanel createParamPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JSpinner pMaxSpin = createPMaxSpinner();
		JLabel pMaxLabel = new JLabel("P");
		pMaxLabel.setLabelFor(pMaxSpin);	
		
		JSpinner tSpin = createTSpinner();
		JLabel tLabel = new JLabel("t");
		tLabel.setLabelFor(tSpin);
		
		JSpinner kSpin = createKSpinner();
		JLabel kLabel = new JLabel("k");
		tLabel.setLabelFor(tSpin);
		
		JLabel name = new JLabel("Pas parameters aan");
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		

		
		panel.add(pMaxLabel,c);
		c.gridx++;
		panel.add(pMaxSpin,c);	
		
		c.gridy++;
		c.gridx = 0;
		panel.add(tLabel,c);
		c.gridx++;
		panel.add(tSpin,c);
		
		c.gridy++;
		c.gridx = 0;
		panel.add(kLabel,c);
		c.gridx++;
		panel.add(kSpin,c);

		c.gridwidth = 2;
		c.gridy = 0;
		c.insets = new Insets(0,0,10,0);
		panel.add(name,c);

		return panel;
	}

	private JSpinner createTSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(main.getT(),0,SpinnerMax,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				main.setT(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the delay for the worker threads here.");
		return spin;
	}
	
	private JSpinner createKSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(main.getK(),1,SpinnerMax,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				main.setK(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the number of particles the worker threads get here.");
		return spin;
	}

	private JSpinner createPMaxSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(0,0,SpinnerMax,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				main.tpool.setPMax(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the maximum number of worker threads here.");
		return spin;
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(createCanvas(), BorderLayout.CENTER);
		frame.add(createSidePane(),BorderLayout.LINE_START);
		return frame;
	}

	private Canvas createCanvas() {
		Canvas canvas = new Canvas() {

			private static final long serialVersionUID = 5772443503354772693L;

			private Map<Long, Color> colorMap = new HashMap<Long, Color>();

			private Image offscreenImage;
			private Graphics offscreenGraphics;


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
				if(offscreenImage == null)
				{
					offscreenImage = createImage(getSize().width, getSize().height);
					offscreenGraphics = offscreenImage.getGraphics();
				}
				
				g.drawImage(offscreenImage, 0, 0, this);
				offscreenGraphics.setColor(Color.BLACK);
				offscreenGraphics.fillRect(0, 0, getSize().width, getSize().height);
				
				for (Particle p : main.particles) {
					//make sure we get data from the same round
					synchronized (p) {
						if ( colorMap.containsKey(p.getThreadId()) ) {
							offscreenGraphics.setColor(colorMap.get(p.getThreadId()));
						} else {
							Color randomColor = Color.getHSBColor((float)Math.random(), Math.min(1.0f, (float)Math.random() + 0.8f), Math.min(1.0f, (float)Math.random() + 0.8f));
							
							colorMap.put(p.getThreadId(), randomColor);
							offscreenGraphics.setColor(randomColor);
						}
						
						offscreenGraphics.fillRect(p.getX(), p.getY(), 2, 2);
					}
				}
				offscreenGraphics.setColor(Color.WHITE);
				offscreenGraphics.drawString("p: "+String.valueOf(main.tpool.size()), 1, 20);
				offscreenGraphics.drawString("n: "+String.valueOf(main.particles.size()), 1, 35);
			}
		};
		
		canvas.setBackground(Color.BLACK);
		
		canvas.setPreferredSize(new Dimension(Main.width, Main.height));		
		return canvas;
	}

	public void run() {
		initializeGui();
	}


}

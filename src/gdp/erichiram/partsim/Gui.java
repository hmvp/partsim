/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BorderFactory; 
import javax.swing.border.EtchedBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui implements Runnable {

	private final Main main;
	private final ThreadPool pool;
	
	private final static int SpinnerMax = Integer.MAX_VALUE;
	
	public Gui(Main main, ThreadPool pool) {
		this.main = main;
		this.pool = pool;
	}

	private void initializeGui() {
		JFrame frame = createFrame();

		Canvas canvas = createCanvas();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JPanel pp = createParamPane();
		JPanel ap = createAddPane();
		JPanel rp = createRemovePane();
		
		p.add(pp);
		p.add(ap);
		p.add(rp);
		p.add(Box.createVerticalGlue());
		p.setPreferredSize(new Dimension(150,0));

		frame.add(canvas, BorderLayout.CENTER);
		frame.add(p,BorderLayout.LINE_START);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	
	}

	private JPanel createRemovePane() {
		JPanel p = new JPanel(); 
		JButton remove = new JButton("Remove particle");
		final JSpinner name = createNameSpinner();
		
		remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				char myName = (name.getValue().toString()).charAt(0);
				main.removeParticles(myName);
			}
			
		});
		
		p.add(remove);
		p.add(name);
		
		return p;
	}
	
	private JSpinner createNameSpinner()
	{
		String[] array = new String[52];
		for(char c = 'a'; c <= 'z'; ++c) {
			array[c - 'a'] = String.valueOf(c);
		}
		for(char c = 'A'; c <= 'Z'; ++c) {
			array[c - 'A' + 26] = String.valueOf(c);
		}
		System.out.println(Arrays.toString(array));
		
		return new JSpinner(new SpinnerListModel(Arrays.asList(array)));
	}

	private JPanel createAddPane() {
		
		final JSpinner xspin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner yspin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner dxspin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner dyspin = new JSpinner(new SpinnerNumberModel(0,0,SpinnerMax,1));
		final JSpinner name = createNameSpinner();
		
		JButton addnew = new JButton("Add new Particle");
		addnew.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Main.debug("Adding parametrized particle!");
				main.addParticle(
						((Number) xspin.getValue()).intValue(), 
						((Number) yspin.getValue()).intValue(), 
						((Number) dxspin.getValue()).intValue(), 
						((Number) dyspin.getValue()).intValue(), 
						((String) name.getValue()).charAt(0)
						
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
		
		JPanel p1 = new JPanel();
		p1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		
		JPanel xPanel = new JPanel();
		JLabel xLabel = new JLabel("X");
		xLabel.setLabelFor(dyspin);
		xPanel.add(xLabel);
		xPanel.add(xspin);
		p1.add(xPanel);
		
		JPanel yPanel = new JPanel();
		JLabel yLabel = new JLabel("Y");
		yLabel.setLabelFor(dyspin);
		yPanel.add(yLabel);
		yPanel.add(yspin);
		p1.add(yPanel);
		
		JPanel dXPanel = new JPanel();
		JLabel dXLabel = new JLabel("dX");
		dXLabel.setLabelFor(dyspin);
		dXPanel.add(dXLabel);
		dXPanel.add(dxspin);
		p1.add(dXPanel);
		
		JPanel dYPanel = new JPanel();
		JLabel dyLabel = new JLabel("dY");
		dyLabel.setLabelFor(dyspin);
		dYPanel.add(dyLabel);
		dYPanel.add(dyspin);
		p1.add(dYPanel);
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Name"));
		namePanel.add(name);
		p1.add(namePanel);		
		p1.add(addnew);
		
		JPanel p2 = new JPanel();
		p2.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		p2.add(addrand);

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(p1);
		p.add(p2);
		
		return p;
	}

	private JPanel createParamPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JPanel pMaxSpinPanel = new JPanel();
		JSpinner pMaxSpin = createPMaxSpinner();
		JLabel pMaxLabel = new JLabel("P");
		pMaxLabel.setLabelFor(pMaxSpin);		
		pMaxSpinPanel.add(pMaxLabel);
		pMaxSpinPanel.add(pMaxSpin);
		panel.add(pMaxSpinPanel);

		JPanel tSpinPanel = new JPanel();
		JSpinner tSpin = createTSpinner();
		JLabel tLabel = new JLabel("t");
		tLabel.setLabelFor(tSpin);
		tSpinPanel.add(tLabel);
		tSpinPanel.add(tSpin);
		panel.add(tSpinPanel);

		panel.add(Box.createVerticalGlue());

		return panel;
	}

	private JSpinner createTSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(0,0,SpinnerMax,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				main.setT(snm.getNumber().intValue());
			}
			
		});
		
		spin.setToolTipText("Set the delay for the worker threads here.");
		return spin;
	}

	private JSpinner createPMaxSpinner() {
		final SpinnerNumberModel snm = new SpinnerNumberModel(0,0,SpinnerMax,1);
		
		JSpinner spin = new JSpinner(snm);
		spin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				pool.setPMax(snm.getNumber().intValue());
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

			private Map<Long, Color> colorMap = new HashMap<Long, Color>();

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
				for (Particle p : main.getParticles()) {
					if ( colorMap.containsKey(p.getThreadId()) ) {
						g.setColor(colorMap.get(p.getThreadId()));
					} else {
						Color randomColor = Color.getHSBColor((float)Math.random(), Math.min(1.0f, (float)Math.random() + 0.8f), Math.min(1.0f, (float)Math.random() + 0.8f));
						
						colorMap.put(p.getThreadId(), randomColor);
						g.setColor(randomColor);
					}
					
					g.fillRect(p.getX(), p.getY(), 2, 2);
				}
				g.setColor(Color.white);
				g.drawString("p: "+String.valueOf(pool.size()), 1, 20);
				g.drawString("n: "+String.valueOf(main.getParticles().size()), 1, 35);
				
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

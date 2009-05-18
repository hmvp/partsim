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

	private Main m;
	private JFrame frame;

	public Gui(Main m) {
		this.m = m;
	}

	private void initializeGui() {
		frame = createFrame();

		Canvas canvas = createCanvas();
		JPanel p = new JPanel(new GridLayout(3, 1));
		JPanel pp = createParamPane();
		JPanel ap = createAddPane();
		JPanel rp = createRemovePane();
		
		p.add(pp);
		p.add(ap);
		p.add(rp);

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
				m.removeParticles(myName);
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
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		
		JPanel p1 = new JPanel();
		p1.setPreferredSize(new Dimension(100,100));

		
		final JSpinner xspin = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
		final JSpinner yspin = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
		final JSpinner dxspin = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
		final JSpinner dyspin = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
		final JSpinner name = createNameSpinner();
		
		JButton addnew = new JButton("Add new Particle");
		addnew.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Main.debug("Adding parametrized particle!");
				m.addParticle(
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
				m.addRandomParticle();
			}
			
		});

		//p.getRootPane().setDefaultButton(addnew);		
		p1.add(xspin);
		p1.add(yspin);
		p1.add(dxspin);
		p1.add(dyspin);
		p1.add(name);
		p1.add(addnew);
		p.add(p1);
		p.add(addrand);


		
		
		return p;
	}

	private JPanel createParamPane() {
		JPanel panel = new JPanel(new GridLayout(2,2));
		JPanel p = new JPanel();
		p.add(panel);
		
		JSpinner pMaxSpin = createPMaxSpinner();
		JSpinner tSpin = createTSpinner();
		
		panel.add(new JLabel("P"));
		panel.add(pMaxSpin);
		
		panel.add(new JLabel("t"));
		panel.add(tSpin);
		return p;
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
				for (Particle p : m.getParticles()) {
					if ( colorMap.containsKey(p.getThreadId()) ) {
						g.setColor(colorMap.get(p.getThreadId()));
					} else {
						Color randomColor = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
						colorMap.put(p.getThreadId(), randomColor);
						g.setColor(randomColor);
					}
					
					g.fillRect(p.getX(), p.getY(), 2, 2);
				}
				g.setColor(Color.white);
				g.drawString("p: "+String.valueOf(m.getP()), 1, 20);
				g.drawString("n: "+String.valueOf(m.getParticles().size()), 1, 35);
				
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

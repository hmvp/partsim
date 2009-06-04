// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables;

import gdp.erichiram.routables.message.Fail;
import gdp.erichiram.routables.message.Repair;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

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

public class Gui implements Runnable, Observer {
	
	private NetwProg netwProg;

	public Gui(NetwProg netwProg) {
		this.netwProg = netwProg;
		netwProg.routingTable.addObserver(this);
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

	private JFrame createFrame() {
		JFrame frame = new JFrame(){

			private static final long serialVersionUID = 5772443503354772693L;

			{//Constructor				
				Timer timer = new Timer(Configuration.guiSpeed , new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						repaint();
					}
				});
				timer.start();
			}
		};
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(createParamPane(), BorderLayout.PAGE_START);
		frame.add(createInfoPane(), BorderLayout.LINE_END);
		frame.add(createNetworkPane(), BorderLayout.CENTER);

		return frame;
	}
	
	

	private Component createNetworkPane() {
		JPanel p = new JPanel();
		
		final SpinnerListModel spm = new SpinnerListModel(new LinkedList<Integer>(netwProg.routingTable.neighbours));

		final JSpinner nSpin = new JSpinner(spm);
		
		
		JButton fail = new JButton("fail");
		fail.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.routingTable.send((Integer) spm.getValue(), new Fail(netwProg.id));
				netwProg.routingTable.send(netwProg.id, new Fail((Integer) spm.getValue()));
				
				if(netwProg.routingTable.neighbours.size() < 1)
				{
					nSpin.setEnabled(false);
				}
				else
				{
					nSpin.setEnabled(true);
					spm.setList(new LinkedList<Integer>(netwProg.routingTable.neighbours));
				}
			}
			
		});
		
		JButton repair = new JButton("repair");
		fail.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.routingTable.send(1100, new Repair(1101,3));
				netwProg.routingTable.send(1101, new Repair(1100,3));
			}
			
		});
		
		p.add(fail);
		p.add(nSpin);
		p.add(repair);
		
		
		return p;
	}

	private Component createInfoPane() {
		JPanel p = new JPanel();
		
		JLabel id = new JLabel("id: " + netwProg.id);
		
		p.add(id);
		
		return p;
	}

	private Component createParamPane() {
		JPanel p = new JPanel();
		
		final SpinnerNumberModel tModel = new SpinnerNumberModel(0,0,9999,1);
		JSpinner tSpin = new JSpinner(tModel);
		tSpin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				netwProg.setT((Integer) tModel.getNumber());
			}
			
		});
		p.add(tSpin);
		
		
		return p;
	}

	public void run() {
		initializeGui();
	}

	public void update(Observable observable, Object obj) {
		
	}


}

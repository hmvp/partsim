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
	private JLabel messagesSentLabel;
	private SpinnerListModel spm;
	private JSpinner nSpin;
	private JButton fail;
	private JSpinner rSpin;
	private JSpinner wSpin;
	private JButton repair;

	public Gui(NetwProg netwProg) {
		this.netwProg = netwProg;
		netwProg.routingTable.addObserver(this);
		netwProg.messagesSent.observable().addObserver(this);
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
		
		spm = new SpinnerListModel(new LinkedList<Integer>(netwProg.routingTable.neighbours.keySet()));

		nSpin = new JSpinner(spm);
		
		
		fail = new JButton("fail");
		fail.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.routingTable.send((Integer) spm.getValue(), new Fail(netwProg.id));
				netwProg.routingTable.send(netwProg.id, new Fail((Integer) spm.getValue()));
			}
			
		});
		
		
		final SpinnerNumberModel snpm = new SpinnerNumberModel(1100,1100,1120,1);

		rSpin = new JSpinner(snpm);
		
		final SpinnerNumberModel wspm = new SpinnerNumberModel(1,1,9999,1);

		wSpin = new JSpinner(wspm);
		
		repair = new JButton("repair");
		fail.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.routingTable.send(netwProg.id, new Repair((Integer) snpm.getNumber(),(Integer) wspm.getNumber()));
				netwProg.routingTable.send((Integer) snpm.getNumber(), new Repair(netwProg.id,(Integer) wspm.getNumber()));
			}
			
		});

		
		
		p.add(fail);
		p.add(nSpin);
		p.add(repair);
		p.add(rSpin);
		p.add(wSpin);
		
		
		return p;
	}

	private Component createInfoPane() {
		JPanel p = new JPanel();
		
		JLabel idLabel = new JLabel("id: " + netwProg.id);
		messagesSentLabel = new JLabel(Configuration.msgString + netwProg.messagesSent.get());
		
		p.add(idLabel);
		p.add(messagesSentLabel);
		
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
		if ( observable == netwProg.messagesSent.observable() ) {
			int messagesSent = netwProg.messagesSent.get();
			messagesSentLabel.setText(Configuration.msgString + messagesSent);
		}
		
		if (observable instanceof RoutingTable) {
			try{
				if(netwProg.routingTable.neighbours.size() < 1)
				{
					nSpin.setEnabled(false);
					fail.setEnabled(false);
				}
				else
				{
					nSpin.setEnabled(true);
					fail.setEnabled(true);
					spm.setList(new LinkedList<Integer>(netwProg.routingTable.neighbours.keySet()));
				}
				
				if(netwProg.routingTable.neighbours.size() < 20)
				{
					wSpin.setEnabled(false);
					rSpin.setEnabled(false);
					repair.setEnabled(false);
				}
				else
				{
					wSpin.setEnabled(true);
					rSpin.setEnabled(true);
					repair.setEnabled(true);
				}
			} catch (Exception e){}
		}
	}


}

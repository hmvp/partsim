// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables.gui;

import gdp.erichiram.routables.NetwProg;
import gdp.erichiram.routables.util.ObservableAtomicInteger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Runnable for the Swing GUI.
 * @author Hiram van Paassen, Eric Broersma
 */
public class Gui implements Runnable, Observer {
	
	/**
	 * Reference to the main program.
	 */
	private NetwProg netwProg;
	
	/**
	 * GUI elements.
	 */
	private JFrame frame;
	private JLabel messagesSentLabel = new JLabel();
	private JSpinner failIdSpinner;
	private JButton failButton;
	
	/**
	 * GUI element models.
	 */
	private RoutingTableTableModel tableModel;
	private SpinnerListModel neighbourIdSpinnerModel;
	
	/**
	 * Constructor.
	 * @param netwProg Reference to the main program.
	 */
	public Gui(NetwProg netwProg) {
		
		this.netwProg = netwProg;
		
		// Keep us posted about changes in the routingTable or the messagesSent.
		netwProg.routingTable.addObserver(this);
		netwProg.addObserver(this);
	}

	/**
	 * Initalize the GUI. 
	 * @see http://java.sun.com/docs/books/tutorial/uiswing/concurrency/index.html
	 */
	public void run() {
		frame = createFrame();	

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	
		// Compute a nice location for the GUI frame, based on its height, width and netwProg.id.
        setNiceLocation(frame);
	}

	/**
	 * Receive updates for NetwProg, RoutingTable and MessagesSent.
	 * 
	 * @see Observer#update(Observable, Object)
	 */
	public void update(final Observable observable, final Object obj) {

		// Use SwingUtilities.invokeLater to make sure the GUI has already been initialized before it's being updated.
		SwingUtilities.invokeLater(new Runnable() {

			@SuppressWarnings("unchecked")
			public void run() {

				if (observable instanceof ObservableAtomicInteger && obj instanceof Integer) {
					int messagesSent = (Integer) obj;
					messagesSentLabel.setText("Number of messages sent: " + messagesSent);
				}

				if (observable instanceof NetwProg && obj instanceof Set) {
					Set<Integer> s = (Set<Integer>) obj;
					if (s.size() < 1) {
						failIdSpinner.setEnabled(false);
						failButton.setEnabled(false);
					} else {
						failIdSpinner.setEnabled(true);
						failButton.setEnabled(true);
						neighbourIdSpinnerModel.setList(new LinkedList<Integer>(s));
					}
				}

				// Repack the frame.
				frame.pack();
			}
		});
	}
	
	/**
	 * Move the frame to a nice location.
	 * @param frame
	 */
	private void setNiceLocation(JFrame frame) {
		
		// Set the index of this frame based on NetwProg.id.
		int index = netwProg.id - 1100;
		
		// A variable containing the menu bar height (Mac OS X and certain Linux window managers only).
		int menuBarHeight = 22;
		
		// Get the height and width of this frame.
		int width = frame.getWidth();
		int height = frame.getHeight();
		
		// Decide how many windows fit next to each other on the current screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();		
		int numberOfWindowsPerRow = (int) Math.floor(screenSize.getWidth() / width);
		
		// Calculate the x and y positions for this window.
		int x = (index % numberOfWindowsPerRow) * width;
		int y = (int) (Math.floor(index / numberOfWindowsPerRow) * height);

		// Set the location (modified by the menuBarHeight).
	    frame.setLocation(x, menuBarHeight + y); 
	}

	/**
	 * Create the frame for the GUI and all of its subcomponents.
	 * @return frame of the GUI
	 */
	private JFrame createFrame() {
		
		// Create the frame and set its caption, layout manager and window closing code.
		JFrame frame = new JFrame("NetwProg " + netwProg.id);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        // closing netwProg
		        netwProg.close();
		    }
		});
		
		// Create and add subcomponents.
		frame.add(createNetworkPanel(), BorderLayout.CENTER);
		frame.add(createInfoPanel(), BorderLayout.LINE_END);

		return frame;
	}

	/**
	 * Create the network panel GUI component.
	 * @return the network panel
	 */
	private Component createNetworkPanel() {
		
		// Create the network panel and set its layout manager.
		JPanel networkPanel = new JPanel();
		networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));
		
		// Add the t spinner panel, the fail panel and the change weight / repair panel to the network panel.
		networkPanel.add(createTSpinnerPanel());
		networkPanel.add(createFailPanel());
		networkPanel.add(createChangeWeightRepairPanel());
		
		return networkPanel;
	}

	/**
	 * Create the Change Weight/Repair panel.
	 * @return the Change Weight/Repair panel
	 */
	private JPanel createChangeWeightRepairPanel() {
		JPanel changeRepairPanel = new JPanel();

		// Initialize change weight / repair ID spinner.
		List<Integer> list = new ArrayList<Integer>();
		for(Integer i = 1100; i < 1121 ; i++)
		{
			// Add all node ids that ain't this node
			if ( i != netwProg.id ) {
				list.add(i)	;
			}
		}
		final SpinnerListModel changeRepairIdSpinnerModel = new SpinnerListModel(list);
		JSpinner changeRepairIdSpinner = new JSpinner(changeRepairIdSpinnerModel);

		// Create change weight / repair weight spinner.
		final SpinnerNumberModel repairWeightSpinnerModel = new SpinnerNumberModel(1,1,9999,1);
		JSpinner changeRepairWeightSpinner = new JSpinner(repairWeightSpinnerModel);
		
		// Create change weight / repair button.
		JButton changeRepairButton = new JButton("Change/Repair");
		changeRepairButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.changeWeightOrRepairConnection((Integer) changeRepairIdSpinnerModel.getValue(),(Integer) repairWeightSpinnerModel.getNumber());
			}
		});
		
		// Add change weight / repair components to changeRepairPanel.	
		changeRepairPanel.add(changeRepairButton);
		changeRepairPanel.add(changeRepairIdSpinner);
		changeRepairPanel.add(changeRepairWeightSpinner);
		return changeRepairPanel;
	}

	/**
	 * Create the Fail panel.
	 * @return the Fail panel
	 */
	private JPanel createFailPanel() {
		JPanel failPanel = new JPanel();

		// Create fail button.
		failButton = new JButton("Fail");
		failButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionEvent) {
				netwProg.failConnection((Integer)neighbourIdSpinnerModel.getValue());
			}
			
		});

		// Create fail ID spinner.
		neighbourIdSpinnerModel = new SpinnerListModel();
		failIdSpinner = new JSpinner(neighbourIdSpinnerModel);
		
		//we start without neighbours so we should disable until we have some
		failIdSpinner.setEnabled(false);
		failButton.setEnabled(false);
		
		// Add fail components to failPanel.
		failPanel.add(failButton);
		failPanel.add(failIdSpinner);
		return failPanel;
	}

	/**
	 * Create the T spinner panel.
	 * @return the T spinner panel
	 */
	private JPanel createTSpinnerPanel() {
		JPanel tSpinnerPanel = new JPanel();

		// Create a label for messagesSent.
		JLabel tSpinnerLabel = new JLabel("T");
		tSpinnerPanel.add(tSpinnerLabel);
		
		// Create a model and spinner for NetwProg.t.
		final SpinnerNumberModel tModel = new SpinnerNumberModel(0,0,9999,1);
		JSpinner tSpin = new JSpinner(tModel);
		tSpin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				netwProg.setT((Integer) tModel.getNumber());
			}
			
		});
		tSpinnerPanel.add(tSpin);
		return tSpinnerPanel;
	}

	/**
	 * Create the information panel GUI component.
	 * @return the information panel
	 */
	private Component createInfoPanel() {

		// Create the information panel and set its layout manager.
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		
		// Add the label for messagesSent to the infoPanel.
		infoPanel.add(messagesSentLabel = new JLabel());
				
		// Add the tablePane to the infoPanel.
		infoPanel.add(createTablePanel(), BorderLayout.WEST);
		
		return infoPanel;
	}

	/**
	 * Create the table panel.
	 * @return the table panel
	 */
	private JPanel createTablePanel() {
		// Create a new TableModel.
		tableModel = new RoutingTableTableModel(netwProg);
		netwProg.routingTable.addObserver(tableModel);

		// Add a tablePanel to contain both the table and its header.
		JPanel tablePanel = new JPanel();		
		tablePanel.setLayout(new BorderLayout());
		
		// Create a table backed by tableModel.
		JTable routingTable = new JTable(tableModel);
		
		// Add the table and its header to tablePane.
		tablePanel.add(routingTable.getTableHeader(), BorderLayout.PAGE_START);
		tablePanel.add(routingTable, BorderLayout.CENTER);
		return tablePanel;
	}
}

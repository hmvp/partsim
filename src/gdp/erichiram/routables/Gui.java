// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables;

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
 * @author eric, hiram
 *
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
		netwProg.messagesSent.addObserver(this);
		netwProg.addObserver(this);
	}

	/**
	 * Initalize the GUI. 
	 */
	private void initializeGui() {
		frame = createFrame();	

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	
		// Compute a nice location for the GUI frame, based on its height, width and netwProg.id.
        setNiceLocation(frame);
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
		frame.add(createParamPanel(), BorderLayout.PAGE_START);
		frame.add(createInfoPanel(), BorderLayout.LINE_END);
		frame.add(createNetworkPanel(), BorderLayout.CENTER);

		return frame;
	}
	
	/**
	 * Move the frame to a nice location.
	 * @param frame
	 */
	private void setNiceLocation(JFrame frame) {
		
		// Set the index of this frame based on NetwProg.id.
		int index = netwProg.id - 1100;
		
		// A variable containing the menu bar height (Mac OS X and Linux Gnome only).
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
	 * Create the network panel GUI component.
	 * @return the network panel
	 */
	private Component createNetworkPanel() {
		
		// Create the network panel and set its layout manager.
		JPanel networkPanel = new JPanel();
		networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));
		
		// Create fail button panel.
		JPanel failPanel = new JPanel();

		// Create fail button.
		failButton = new JButton("fail");
		failButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionEvent) {
				netwProg.failConnection((Integer)neighbourIdSpinnerModel.getValue());
			}
			
		});

		// Create fail ID spinner.
		neighbourIdSpinnerModel = new SpinnerListModel();
		failIdSpinner = new JSpinner(neighbourIdSpinnerModel);
		
		// Add fail components to failPanel.
		failPanel.add(failButton);
		failPanel.add(failIdSpinner);

		// Create change weight / repair button panel.
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
		JButton changeRepairButton = new JButton("change / repair");
		changeRepairButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.changeWeightOrRepairConnection((Integer) changeRepairIdSpinnerModel.getValue(),(Integer) repairWeightSpinnerModel.getNumber());
			}
		});
		
		// Add change weight / repair components to changeRepairPanel.	
		changeRepairPanel.add(changeRepairButton);
		changeRepairPanel.add(changeRepairIdSpinner);
		changeRepairPanel.add(changeRepairWeightSpinner);	

		// Add the fail panel and the change weight / repair panel to the network panel.
		networkPanel.add(failPanel);
		networkPanel.add(changeRepairPanel);
		
		return networkPanel;
	}

	/**
	 * Create the information panel GUI component.
	 * @return the information panel
	 */
	private Component createInfoPanel() {

		// Create the information panel and set its layout manager.
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		
		// Create a new TableModel.
		tableModel = new RoutingTableTableModel();
		netwProg.routingTable.addObserver(tableModel);
		
		// Create a label for messagesSent.
		messagesSentLabel = new JLabel(Configuration.messagesSentString + netwProg.messagesSent.get());
		infoPanel.add(messagesSentLabel);

		// Add a tablePanel to contain both the table and its header.
		JPanel tablePanel = new JPanel();		
		tablePanel.setLayout(new BorderLayout());
		
		// Create a table backed by tableModel.
		JTable routingTable = new JTable(tableModel);
		
		// Add the table and its header to tablePane.
		tablePanel.add(routingTable.getTableHeader(), BorderLayout.PAGE_START);
		tablePanel.add(routingTable, BorderLayout.CENTER);	
		
		// Add the tablePane to the infoPanel.
		infoPanel.add(tablePanel, BorderLayout.WEST);
		
		// TODO: remove this code?
		//graph = createGraphComponent();
		//infoPane.add(graph , BorderLayout.EAST);
		
		return infoPanel;
	}

	/**
	 * Create the parameters panel GUI component.
	 * @return the parameters panel
	 */
	private Component createParamPanel() {

		// Create the parameter panel.
		JPanel paramPanel = new JPanel();
		
		final SpinnerNumberModel tModel = new SpinnerNumberModel(0,0,9999,1);
		JSpinner tSpin = new JSpinner(tModel);
		tSpin.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				netwProg.setT((Integer) tModel.getNumber());
			}
			
		});
		paramPanel.add(tSpin);
		
		return paramPanel;
	}

	// TODO: remove this one?
//	private Component createGraphComponent() {
//		
//		return new GraphPanel(netwProg.routingTable.ndis, netwProg.id, netwProg.routingTable.neighbours);
//	}

	public void run() {
		initializeGui();
	}

	public void update(final Observable observable, final Object obj) {
		
		SwingUtilities.invokeLater(new Runnable(){
			
			public void run(){
				if ( observable == netwProg.messagesSent) {
					int messagesSent = netwProg.messagesSent.get();
					messagesSentLabel.setText(Configuration.messagesSentString + messagesSent);
				}
				
				if (observable instanceof NetwProg) {
					try{
						if(netwProg.idsToSocketHandlers.size() < 1)
						{
							failIdSpinner.setEnabled(false);
							failButton.setEnabled(false);
						}
						else
						{
							failIdSpinner.setEnabled(true);
							failButton.setEnabled(true);
							neighbourIdSpinnerModel.setList(new LinkedList<Integer>(netwProg.idsToSocketHandlers.keySet()));
						}
					} catch (Exception e){}
				}
				
				//graph.repaint();
				frame.pack();
		        setNiceLocation(frame);
			}
		});
	}
}

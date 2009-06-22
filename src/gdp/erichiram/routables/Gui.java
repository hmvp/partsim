// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.table.AbstractTableModel;

public class Gui implements Runnable, Observer {
	
	private NetwProg netwProg;
	private JLabel messagesSentLabel = new JLabel();
	private SpinnerListModel neighbourIdSpinnerModel;
	private JSpinner failIdSpinner;
	private JButton failButton;
	private JSpinner changeRepairIdSpinner;
	private JSpinner changeRepairWeightSpinner;
	private AbstractTableModel tableModel;
	private JFrame frame;

	private JButton changeRepairButton;
//	private Component graph;

	public Gui(NetwProg netwProg) {
		this.netwProg = netwProg;
		netwProg.routingTable.addObserver(this);
		netwProg.messagesSent.addObserver(this);
	}

	/**
	 * initalize the Gui, 
	 */
	private void initializeGui() {
		frame = createFrame();	

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame("NetwProg " + netwProg.id);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        // closing netwProg
		        netwProg.die();
		    }
		});		
		
		frame.add(createParamPane(), BorderLayout.PAGE_START);
		frame.add(createInfoPane(), BorderLayout.LINE_END);
		frame.add(createNetworkPane(), BorderLayout.CENTER);

		return frame;
	}
	
	

	private Component createNetworkPane() {
		JPanel networkPane = new JPanel();
		networkPane.setLayout(new BoxLayout(networkPane, BoxLayout.Y_AXIS));
		
		// Fail button panel.
		JPanel failPanel = new JPanel();

		// Fail button.
		failButton = new JButton("fail");
		failButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionEvent) {
				netwProg.failConnection((Integer)neighbourIdSpinnerModel.getValue());
			}
			
		});

		// Fail ID spinner.
		neighbourIdSpinnerModel = new SpinnerListModel();
		failIdSpinner = new JSpinner(neighbourIdSpinnerModel);
		
		// Add components to failPanel.
		failPanel.add(failButton);
		failPanel.add(failIdSpinner);

		// Change weight / repair button panel.
		JPanel changeRepairPanel = new JPanel();

		// Change weight / repair ID spinner.
		List<Integer> list = new ArrayList<Integer>();
		for(Integer i = 1100; i < 1121 ; i++)
		{
			// Add all node ids that ain't this node
			if ( i != netwProg.id ) {
				list.add(i)	;
			}
		}
		final SpinnerListModel changeRepairIdSpinnerModel = new SpinnerListModel(list);
		changeRepairIdSpinner = new JSpinner(changeRepairIdSpinnerModel);

		// Change weight / repair weight spinner.
		final SpinnerNumberModel repairWeightSpinnerModel = new SpinnerNumberModel(1,1,9999,1);
		changeRepairWeightSpinner = new JSpinner(repairWeightSpinnerModel);
		
		// Change weight / repair button.
		changeRepairButton = new JButton("change / repair");
		changeRepairButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.changeWeightOrRepairConnection((Integer) changeRepairIdSpinnerModel.getValue(),(Integer) repairWeightSpinnerModel.getNumber());
			}
		});
		
		// Add components to changeRepairPanel.	
		changeRepairPanel.add(changeRepairButton);
		changeRepairPanel.add(changeRepairIdSpinner);
		changeRepairPanel.add(changeRepairWeightSpinner);	

		networkPane.add(failPanel);
		networkPane.add(changeRepairPanel);
		
		return networkPane;
	}

	private Component createInfoPane() {
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.Y_AXIS));
		
		tableModel = new AbstractTableModel() {

			private static final long serialVersionUID = 3441755023701740847L;

			private final String[] columnNames = { "Node", "Neighbour to use", "Length of path" };
			private int[][] nodes = new int[1][3];

			public String getColumnName(int col) {
				return columnNames[col].toString();
			}

			public int getRowCount() {
				return nodes.length;
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public Object getValueAt(int row, int col) {
				
				if ( col == 1  && nodes[row][col] == RoutingTable.UNDEF_ID) {
					return "UNDEFINED";
				} else if ( col == 2  && nodes[row][col] == RoutingTable.MAX_DIST) {
					return "MAX DIST";
				} else {
					return nodes[row][col];
				}
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}

			@Override
			public void fireTableDataChanged() {
				nodes = netwProg.routingTable.getNodesData();
				super.fireTableDataChanged();
			}
			
			
		};
				
		messagesSentLabel = new JLabel(Configuration.messagesSentString + netwProg.messagesSent.get());
		infoPane.add(messagesSentLabel);

		JPanel tablePane = new JPanel();		
		tablePane.setLayout(new BorderLayout());
		
		JTable routingTable = new JTable(tableModel);
		tablePane.add(routingTable.getTableHeader(), BorderLayout.PAGE_START);
		tablePane.add(routingTable, BorderLayout.CENTER);	
		
		infoPane.add(tablePane, BorderLayout.WEST);		
		//graph = createGraphComponent();
		//infoPane.add(graph , BorderLayout.EAST);
		
		return infoPane;
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


//	private Component createGraphComponent() {
//		
//		return new GraphPanel(netwProg.routingTable.ndis, netwProg.id, netwProg.routingTable.neighbours);
//	}

	public void run() {
		initializeGui();
	}

	public void update(final Observable observable, Object obj) {
		
		SwingUtilities.invokeLater(new Runnable(){
			
			public void run(){
				if ( observable == netwProg.messagesSent) {
					int messagesSent = netwProg.messagesSent.get();
					messagesSentLabel.setText(Configuration.messagesSentString + messagesSent);
				}
				
				if (observable instanceof RoutingTable) {
					if(tableModel != null)
						tableModel.fireTableDataChanged();
					
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
						
						
						if(netwProg.idsToSocketHandlers.size() > 20)
						{
							changeRepairWeightSpinner.setEnabled(false);
							changeRepairIdSpinner.setEnabled(false);
							changeRepairButton.setEnabled(false);
						}
						else
						{
							changeRepairWeightSpinner.setEnabled(true);
							changeRepairIdSpinner.setEnabled(true);
							changeRepairButton.setEnabled(true);
						}
					} catch (Exception e){}
					
					
				}
				
				//graph.repaint();
				frame.pack();
			}
		});
	}
}

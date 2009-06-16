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
import java.util.TreeSet;

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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

public class Gui implements Runnable, Observer {
	
	private NetwProg netwProg;
	private JLabel messagesSentLabel = new JLabel();
	private SpinnerListModel spm;
	private JSpinner nSpin;
	private JButton fail;
	private JSpinner rSpin;
	private JSpinner wSpin;
	private JButton repair;
	private AbstractTableModel tableModel;
	private JFrame frame;

	private JButton changew;

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
		JFrame frame = new JFrame("NetwProg " + netwProg.id){

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
		JPanel p = new JPanel();
		
		spm = new SpinnerListModel();

		nSpin = new JSpinner(spm);
		
		
		fail = new JButton("fail");
		fail.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.failConnection((Neighbour) spm.getValue());
			}
			
		});
		List<String> list = new ArrayList<String>();
		for(Integer i = 1100; i < 1121 ; i++)
		{
			//if(!netwProg.startingNeighbours.contains(i))
				list.add(i.toString());
		}
		
		final SpinnerListModel snpm = new SpinnerListModel(list);

		rSpin = new JSpinner(snpm);
		
		final SpinnerNumberModel wspm = new SpinnerNumberModel(1,1,9999,1);

		wSpin = new JSpinner(wspm);
		
		repair = new JButton("repair");
		changew = new JButton("change weight");
		changew.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.changeWeight(((Neighbour) spm.getValue()).id,(Integer) wspm.getNumber());
			}
		});
		
		repair.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				netwProg.startRepairConnection(Integer.valueOf((String) snpm.getValue()),(Integer) wspm.getNumber());
			}
		});
		
		p.add(fail);
		p.add(nSpin);
		p.add(repair);
		p.add(changew);
		p.add(rSpin);
		p.add(wSpin);
		
		return p;
	}

	private Component createInfoPane() {
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.Y_AXIS));
		
		tableModel = new AbstractTableModel() {

			private static final long serialVersionUID = 3441755023701740847L;

			String[] columnNames = { "Node", "Neighbour to use", "Length of path" };

			public String getColumnName(int col) {
				return columnNames[col].toString();
			}

			public int getRowCount() {
				return netwProg.routingTable.nodes.size();
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public Object getValueAt(int row, int col) {
				
				if ( col == 0 ) {
					return new TreeSet<Integer>(netwProg.routingTable.nodes).toArray()[row];
				} else if ( col == 1 ) {
					return netwProg.routingTable.NB.get(new TreeSet<Integer>(netwProg.routingTable.nodes).toArray()[row]);
				} else {
					return netwProg.routingTable.D.get(new TreeSet<Integer>(netwProg.routingTable.nodes).toArray()[row]);
				}
			}

			public boolean isCellEditable(int row, int col) {
				return false;
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
		
		infoPane.add(createGraphComponent(), BorderLayout.EAST);
		
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


	private Component createGraphComponent() {
		
		return new GraphPanel(netwProg.routingTable.ndis, netwProg.id);
	}

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
						if(netwProg.routingTable.neighbours.size() < 1)
						{
							nSpin.setEnabled(false);
							fail.setEnabled(false);
						}
						else
						{
							nSpin.setEnabled(true);
							fail.setEnabled(true);
							spm.setList(new LinkedList<Neighbour>(new TreeSet<Neighbour>(netwProg.socketHandlers)));
						}
						
						
						if(netwProg.routingTable.neighbours.size() > 20)
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
				//frame.pack();
			}
		});
	}
}

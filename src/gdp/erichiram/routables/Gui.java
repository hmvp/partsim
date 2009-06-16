// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.routables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;
import java.util.Map.Entry;

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

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

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

    private JGraphModelAdapter<Integer, DefaultWeightedEdge> jGraphModelAdapter;
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
		infoPane.add(tablePane);
		
		infoPane.add(createGraphComponent());
		
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
		
        // create a JGraphT graph
		ListenableDirectedWeightedGraph<Integer, DefaultWeightedEdge> g = new ListenableDirectedWeightedGraph<Integer, DefaultWeightedEdge>( DefaultWeightedEdge.class );

        // create a visualization using JGraph, via an adapter
        jGraphModelAdapter = new JGraphModelAdapter<Integer, DefaultWeightedEdge>( g );

        JGraph jgraph = new JGraph( jGraphModelAdapter);
        
        // TODO implementeer de onderstaande methods stubs zodat we de weight labels kunnen tekenen ipv "(1101 : 1104)" ofzo
//        JGraph jgraph = new JGraph( jGraphModelAdapter, new GraphLayoutCache( jGraphModelAdapter, new DefaultCellViewFactory() {
//
//			private static final long serialVersionUID = 1405514043325026419L;
//
//			@Override
//			protected EdgeView createEdgeView(Object arg0) {
//				return new EdgeView() {
//					private static final long serialVersionUID = 8033174952670878036L;
//
//					@Override
//					public CellViewRenderer getRenderer() {
//						return new EdgeRenderer() {
//							private static final long serialVersionUID = 6499146125873141228L;
//							
//							@Override
//							protected void paintLabel(Graphics g,
//									String label, Point2D p, boolean mainLabel) {
//								
//								g.drawString(label, (int)p.getX(), (int)p.getY());
//							}
//
//							@Override
//							public Rectangle getBounds() {
//								// TODO Auto-generated method stub
//								return super.getBounds();
//							}
//							
//						};
//					}
//
//					@Override
//					public Rectangle2D getBounds() {
//						// TODO Auto-generated method stub
//						return super.getBounds();
//					}
//
//					@Override
//					public Rectangle2D getExtraLabelBounds(int arg0) {
//						// TODO Auto-generated method stub
//						return super.getExtraLabelBounds(arg0);
//					}
//
//					@Override
//					public Rectangle2D getLabelBounds() {
//						// TODO Auto-generated method stub
//						return super.getLabelBounds();
//					}
//
//					@Override
//					public Shape getShape() {
//						// TODO Auto-generated method stub
//						return super.getShape();
//					}
//
//					@Override
//					public boolean intersects(JGraph arg0, Rectangle2D arg1) {
//						// TODO Auto-generated method stub
//						return super.intersects(arg0, arg1);
//					}
//				};
//			}
//        	
//        }) );
        jgraph.setBackground(Color.WHITE);
        
        // make graph uneditable
        jgraph.setSelectionEnabled(false);
        jgraph.setSelectionModel(new JGraph.EmptySelectionModel() {
			private static final long serialVersionUID = 1237119486257125129L;

			public Object[] getSelectables() {
        		return new Object[]{};
        	}
        });

        // add some sample data (graph manipulated via JGraphT)
        for (Integer x : netwProg.routingTable.ndis.keySet() ) {
            g.addVertex( x);
            positionVertexAt(x, (int)(Math.random() * 400), (int)(Math.random() * 400) );
           
        }
        
        for ( Entry<Integer, Map<Integer, Integer>> x : netwProg.routingTable.ndis.entrySet() ) {
            Map<Integer, Integer> nodeDistances = x.getValue();
            
            for ( Entry<Integer, Integer> nodeDistance : nodeDistances.entrySet() ) {

            	if ( !x.getKey().equals(nodeDistance.getKey()) ) {
            		DefaultWeightedEdge e = g.addEdge( x.getKey(), nodeDistance.getKey() );
            		g.setEdgeWeight(e, nodeDistance.getValue());
            	}
            }
           
        }

        // that's all there is to it!...
		return jgraph;
	}

    private void positionVertexAt( Object vertex, int x, int y ) {
        DefaultGraphCell cell = jGraphModelAdapter.getVertexCell(vertex);
		AttributeMap attributes = cell.getAttributes();

		GraphConstants.setBounds(attributes, new Rectangle(x, y, 40, 20));
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

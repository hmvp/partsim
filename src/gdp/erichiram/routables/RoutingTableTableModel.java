package gdp.erichiram.routables;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * This model is used to show the information from RoutingTable in the GUI.
 * @author Hiram van Paassen, Eric Broersma
 */
public class RoutingTableTableModel extends AbstractTableModel implements Observer{

	private static final long serialVersionUID = 3441755023701740847L;

	/**
	 * List of column names.
	 */
	private final String[] columnNames = { "Node", "Neighbour to use", "Length of path" };
	
	/**
	 * Cache for nodes data.
	 */
	private Map<Integer, Integer[]> nodes = new HashMap<Integer, Integer[]>(20);

	/**
	 * Constructor for RoutingTableTablemodel.
	 * @param netwProg Reference to the main program.
	 */
	public RoutingTableTableModel(NetwProg netwProg) {
		
		// Load initial nodes data.
		Integer[][] data = netwProg.routingTable.getNodesData();
		for(Integer[] d : data)
		{
			nodes.put(d[0]-1100, d);
		}
	}

	/**
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	/**
	 * @see AbstractTableModel#getRowCount()
	 */
	public int getRowCount() {
		return nodes.size();
	}

	/**
	 * @see AbstractTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * @see AbstractTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		
		if (	( col == 1  && nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col] == RoutingTable.UNDEF_ID)
		|| 		( col == 2  && nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col] == RoutingTable.MAX_DIST) ){
			return "-";
		} else {
			return nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col];
		}
	}
	
	/**
	 * @see AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	/**
	 * @see Observer#update(Observable, Object)
	 */
	public void update(final Observable observable, final Object message) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if (observable instanceof RoutingTable && message instanceof Integer[]) {					
					Integer[] array = (Integer[]) message;
					nodes.put(array[0]-1100, array);
					fireTableDataChanged();
				}
			}
		});
	}
}

package gdp.erichiram.routables;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class RoutingTableTableModel extends AbstractTableModel implements Observer{

	private static final long serialVersionUID = 3441755023701740847L;

	private final String[] columnNames = { "Node", "Neighbour to use", "Length of path" };
	private Map<Integer, Integer[]> nodes = new HashMap<Integer, Integer[]>(20);

	public RoutingTableTableModel(NetwProg netwProg) {
		Integer[][] data = netwProg.routingTable.getNodesData();
		for(Integer[] d : data)
		{
			nodes.put(d[0]-1100, d);
		}
	}

	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	public int getRowCount() {
		return nodes.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int row, int col) {
		
		if ( col == 1  && nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col] == RoutingTable.UNDEF_ID) {
			return "undefined";
		} else if ( col == 2  && nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col] == RoutingTable.MAX_DIST) {
			return "max distance";
		} else {
			return nodes.get(nodes.keySet().toArray(new Integer[0])[row])[col];
		}
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

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

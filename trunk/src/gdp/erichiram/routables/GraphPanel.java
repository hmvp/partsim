package gdp.erichiram.routables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.QuadCurve2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {


	private static final long serialVersionUID = 3664551611257035887L;

	private Map<Integer, GraphNode> nodes = new HashMap<Integer, GraphNode>();
	private Set<GraphEdge> edges = new HashSet<GraphEdge>();

	private HashMap<Integer, Map<Integer, Integer>> ndis;

	private int specialNodeId;

	public GraphPanel(HashMap<Integer, Map<Integer, Integer>> ndis, Integer specialNodeId) {
		this.ndis = ndis;
		this.specialNodeId = specialNodeId;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(400,400);
	}

	@Override
	public void paint(Graphics g) {
		if(nodes.size() < 1)
			updateGraph();

		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.WHITE);		
		g.fillRect(0,0,bounds.width,bounds.height);

		Graphics g2 = g.create(bounds.x, bounds.y, bounds.width, bounds.height);
		
		paintGraph(g2);
	}


	@Override
	public void update(Graphics g) {
		updateGraph();
		super.update(g);
	}

	private void paintGraph(Graphics g) {

	    Graphics2D g2 = (Graphics2D) g;
		
		// draw edges
		g.setColor(Color.BLACK);
		for (GraphEdge edge : edges ) {
			// draw line
			int controlX = (edge.getFirst().x + edge.getSecond().x) / 2 + (int)(Math.random() * 100) - 50;
			int controlY = (edge.getFirst().y + edge.getSecond().y) / 2 + (int)(Math.random() * 100) - 50;
			QuadCurve2D q = new QuadCurve2D.Float();
			q.setCurve(edge.getFirst().x, edge.getFirst().y, controlX, controlY, edge.getSecond().x, edge.getSecond().y);
			g2.draw(q);			
			
			// draw label
			g.drawString(""+edge.getWeight(), controlX - 15, controlY);
		}		
		
		// draw nodes
		for (GraphNode node : nodes.values() ) {
			
			if ( node.id == specialNodeId ) {
				
				// draw circle
				g.setColor(Color.WHITE);
				g.fillOval(node.x-20, node.y-20, 40, 40);
				
				// draw circle
				g.setColor(Color.BLACK);
				g.drawOval(node.x-20, node.y-20, 40, 40);
				
				// draw id
				g.drawString(String.valueOf(node.id), node.x-15, node.y+5);
			} else {

				// draw circle
				g.setColor(Color.BLACK);
				g.fillOval(node.x-20, node.y-20, 40, 40);
				
				// draw id
				g.setColor(Color.WHITE);
				g.drawString(String.valueOf(node.id), node.x-15, node.y+5);
			}
		}

	}

	private void updateGraph() {

		nodes = new HashMap<Integer, GraphNode>();
		// add the nodes (set the positions on a circle)		
		int numberOfNodes = ndis.keySet().size();
		double radianPeriod = 2 * Math.PI / numberOfNodes;		
		double radians = 0.0;
		for (Integer x : ndis.keySet()) {
			GraphNode node = new GraphNode(x, (int)(Math.cos(radians) * 150) + 200, (int)(Math.sin(radians) * 150) + 200);
			if (!nodes.containsKey(node.id) ) {
				nodes.put(node.id, node);
			}
			radians += radianPeriod;
		}
		
		edges = new HashSet<GraphEdge>();
		// add the edges
		for (Entry<Integer, Map<Integer, Integer>> x : ndis.entrySet()) {
			Map<Integer, Integer> nodeDistances = x.getValue();

			for (Entry<Integer, Integer> nodeDistance : nodeDistances
					.entrySet()) {

				if (!x.getKey().equals(nodeDistance.getKey())) {
					
					GraphEdge edge = new GraphEdge(nodes.get(x.getKey()), nodes.get(nodeDistance.getKey()), nodeDistance.getValue());
					if ( edges.contains(edge)) {


						// TODO update the edge's weight
						edge.setWeight(nodeDistance.getValue());
					} else {
						if ( edge.getWeight() < 20001 ) {
							edges.add(edge);
						}
					}
				}
			}
		}
	}

	class GraphNode {
		private final int id;
		private int x;
		private int y;

		GraphNode(Integer id, int x, int y) {
			this.id = id;
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GraphNode other = (GraphNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		private GraphPanel getOuterType() {
			return GraphPanel.this;
		}
	}

	class GraphEdge {

		private final GraphNode a;
		private final GraphNode b;
		private int weight;

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public GraphEdge(GraphNode a, GraphNode b, int weight) {
			this.a = a;
			this.b = b;
			this.weight = weight;
		}

		public GraphNode getFirst() {
			return a;
		}

		public GraphNode getSecond() {
			return b;
		}

		public String toString() {
			return "(" + a + ", " + b + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GraphEdge other = (GraphEdge) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}

		private GraphPanel getOuterType() {
			return GraphPanel.this;
		}


	}
}

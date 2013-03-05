package org.cflgraph.cfl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cflgraph.cfl.CFLGraph.Vertex;
import org.cflgraph.cfl.NormalCfl.BinaryProduction;
import org.cflgraph.cfl.NormalCfl.Element;
import org.cflgraph.cfl.NormalCfl.UnaryProduction;
import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;
import org.cflgraph.utility.Utility.Pair;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = -8194089350311320116L;

	public static class Vertex {		
		private String name;

		public Vertex(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				Vertex vertex = (Vertex)object;
				return this.name.equals(vertex.getName());
			}
		}
	}

	protected Map<Edge,EdgeData> edges = new HashMap<Edge,EdgeData>();
	protected MultivalueMap<Vertex,Edge> edgesBySource = new MultivalueMap<Vertex,Edge>();
	protected MultivalueMap<Vertex,Edge> edgesBySink = new MultivalueMap<Vertex,Edge>();

	/*
	private Counter<UnaryProduction> unaryProductionCounts = new Counter<UnaryProduction>();
	private Counter<BinaryProduction> binaryProductionCounts = new Counter<BinaryProduction>();

	public Counter<BinaryProduction> getBinaryProductionCounts() {
		return this.binaryProductionCounts;
	}

	public Counter<UnaryProduction> getUnaryProductionCounts() {
		return this.unaryProductionCounts;
	}
	*/

	private Map<Pair<Vertex,Element>,Edge> edgesBySourceElement = new HashMap<Pair<Vertex,Element>,Edge>();

	public void addEdge(Vertex source, Vertex sink, Element element, int weight) {
		super.add(source);
		super.add(sink);
		
		Edge edge = new Edge(source, sink, element);
		
		this.edges.put(edge, new EdgeData(edge, weight));
		this.edgesBySink.add(sink, edge);
		this.edgesBySource.add(source, edge);

		this.edgesBySourceElement.put(new Pair<Vertex,Element>(source,element), edge);
	}

	public void addEdge(Vertex source, Vertex sink, Element Element) {
		this.addEdge(source, sink, Element, 0);
	}
	
	public void addEdge(Edge edge, int weight) {
		this.addEdge(edge.source, edge.sink, edge.element, weight);
	}

	public static class Edge {
		private Vertex source;
		private Vertex sink;
		private Element element;
		
		public Edge(Vertex source, Vertex sink, Element element) {
			this.source = source;
			this.sink = sink;
			this.element = element;
		}

		public Vertex getSource() {
			return this.source;
		}

		public Vertex getSink() {
			return this.sink;
		}

		public Element getElement() {
			return this.element;
		}

		@Override
		public int hashCode() {
			return 37*(17*this.source.hashCode() + this.sink.hashCode()) + this.element.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				Edge graphElement = (Edge)object;
				return this.source.equals(graphElement.getSource())
						&& this.sink.equals(graphElement.getSink())
						&& this.element.equals(graphElement.getElement());
			}		
		}

		@Override
		public String toString() {
			return this.element.getName() + "(" + this.source.getName() + "," + this.sink.getName() + ")"; 
		}
	}
	
	public static class EdgeData {
		private Edge edge;
		
		private boolean forward;
		
		private EdgeData firstInput;
		private EdgeData secondInput;
		private int weight;
		
		public EdgeData(Edge edge, EdgeData firstInput, EdgeData secondInput, boolean forward) {
			this.edge = edge;
			
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.forward = forward;
			
			this.weight = firstInput.weight + secondInput.weight;
		}
		
		public EdgeData(Edge edge, EdgeData firstInput, EdgeData secondInput) {
			this(edge, firstInput, secondInput, true);
		}
		
		public EdgeData(Edge edge, EdgeData input, boolean forward) {
			this.edge = edge;
			
			this.firstInput = input;
			this.secondInput = null;
			this.forward = forward;
			
			this.weight = input.weight;
		}
		
		public EdgeData(Edge edge, EdgeData input) {
			this(edge, input, true);
		}

		public EdgeData(Edge edge, int weight) {
			this.edge = edge;
			
			this.firstInput = null;
			this.secondInput = null;
			this.forward = true;
			
			this.weight = weight;
		}
		
		public EdgeData(Edge edge) {
			this(edge, 0);
		}

		public EdgeData getInput() {
			return this.firstInput;
		}

		public EdgeData getFirstInput() {
			return this.firstInput;
		}
		
		public EdgeData getSecondInput() {
			return this.secondInput;
		}
		
		public int getWeight() {
			return this.weight;
		}
		
		public boolean isTerminal() {
			return this.firstInput == null && this.secondInput == null;
		}
		
		public boolean isUnary() {
			return this.firstInput != null && this.secondInput == null;
		}
		
		public boolean isBinary() {
			return this.firstInput != null && this.secondInput != null;
		}
		
		public String getPath(boolean forward) {
			if(this.isTerminal()) {
				return forward ? this.edge.element.getName() + "(" + this.edge.source.getName() + "," + this.edge.sink.getName() + ")"
						: this.edge.element.getName() + "Bar" + "(" + this.edge.sink.getName() + "," + this.edge.source.getName() + ")";
			} else if(this.isUnary()) {
				return this.firstInput.getPath(this.forward == forward);
			} else if(this.isBinary()) {
				if(this.forward == forward) {
					return this.firstInput.getPath(true) + " " + this.secondInput.getPath(true);
				} else {
					return this.secondInput.getPath(false) + " " + this.firstInput.getPath(false);
				}
			} else {
				return "";
			}
		}
		
		public String getPath() {
			return this.getPath(true);
		}
	}

	// run Knuth's algorithms
	public Map<Edge,EdgeData> getClosure(NormalCfl cfl) {

		// maps from the minimum graph elements to their paths
		MultivalueMap<Pair<Vertex,Element>,Edge> minEdgesBySourceElement = new MultivalueMap<Pair<Vertex,Element>,Edge>();
		MultivalueMap<Pair<Vertex,Element>,Edge> minEdgesBySinkElement = new MultivalueMap<Pair<Vertex,Element>,Edge>();

		Heap<Edge> curMinEdgeQueue = new Heap<Edge>(); // stores the current minimum graph elements left to be processed
		Map<Edge,EdgeData> curMinEdgeData = new HashMap<Edge,EdgeData>(); // stores the current weight of a given edge		

		// step 1: fill in minGraphElementPaths and minGraphElementQueue with the initial Elements
		for(Vertex source : this) {
			for(Edge edge : this.edgesBySource.get(source)) {
				curMinEdgeQueue.update(edge, this.edges.get(edge).weight);
				curMinEdgeData.put(edge, this.edges.get(edge));
			}
		}

		// step 2: for each element in minGraphElementQueue, iterate through productions and check if they are smaller
		int i=0;
		while(!curMinEdgeQueue.isEmpty()) {

			// step 2a: get the minimum element
			Edge minEdge = curMinEdgeQueue.pop();

			// TODO: remove printing
			if(i%10000 == 0) {
				System.out.println("Current iteration: " + i);
				System.out.println("Current heap size: " + curMinEdgeQueue.size());
			}
			i++;

			// step 2b: add the minimum element to the map
			minEdgesBySourceElement.add(new Pair<Vertex,Element>(minEdge.getSource(), minEdge.getElement()), minEdge);
			minEdgesBySinkElement.add(new Pair<Vertex,Element>(minEdge.getSink(), minEdge.getElement()), minEdge);

			// TODO: fix temporary hack
			if(minEdge.getElement().equals(new Element("flowsTo"))) {
				Edge barEdge = new Edge(minEdge.getSink(), minEdge.getSource(), new Element(minEdge.getElement().getName() + "Bar"));
				curMinEdgeQueue.update(barEdge, curMinEdgeData.get(minEdge).weight);
				curMinEdgeData.put(barEdge, new EdgeData(barEdge, curMinEdgeData.get(minEdge), false));
			}

			// step 2c: update the minimum path for all single productions using that element
			for(UnaryProduction unaryProduction : cfl.getUnaryProductionsByInput(minEdge.getElement())) {
				Edge newEdge = new Edge(minEdge.getSource(), minEdge.getSink(), unaryProduction.getOutput());
				EdgeData curData = curMinEdgeData.get(newEdge);
				EdgeData newData = curMinEdgeData.get(minEdge);
				/*
				if(curData == null) {
					this.unaryProductionCounts.incrementCount(unaryProduction);
				}
				*/
				if(curData == null || newData.weight < curData.weight) {
					curMinEdgeQueue.update(newEdge, newData.weight);
					curMinEdgeData.put(newEdge, new EdgeData(newEdge, newData));
				}
			}

			// step 2d: update the minimum path for all pair productions using that element as the first input
			for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByFirstInput(minEdge.getElement())) {
				for(Edge secondEdge : minEdgesBySourceElement.get(new Pair<Vertex,Element>(minEdge.getSink(), binaryProduction.getSecondInput()))) {
					Edge newEdge = new Edge(minEdge.getSource(), secondEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(minEdge), curMinEdgeData.get(secondEdge));

					if(curData == null) {
						if(binaryProduction.getFirstInput().getName().startsWith("flowsToField_") && !binaryProduction.getFirstInput().getName().contains("^")) {
							String field = binaryProduction.getFirstInput().getName().substring(13);
							if(this.edgesBySourceElement.get(new Pair<Vertex,Element>(newEdge.getSink(), new Element("load_" + field))) == null) {
								continue;
							}
							//this.binaryProductionCounts.incrementCount(binaryProduction);
						}
					}

					if(curData == null || newData.weight < curData.weight) {
						curMinEdgeQueue.update(newEdge, newData.weight);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}

			// step 2e: update the minimum path for all pair productions using that element as the second input
			for(BinaryProduction binaryProduction : cfl.getBinaryProductionsBySecondInput(minEdge.getElement())) {
				for(Edge firstEdge : minEdgesBySinkElement.get(new Pair<Vertex,Element>(minEdge.getSource(), binaryProduction.getFirstInput()))) {
					Edge newEdge = new Edge(firstEdge.getSource(), minEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(firstEdge), curMinEdgeData.get(minEdge));

					if(curData == null) {
						if(binaryProduction.getFirstInput().getName().startsWith("flowsToField_") && !binaryProduction.getFirstInput().getName().contains("^")) {
							String field = binaryProduction.getFirstInput().getName().substring(13);
							if(this.edgesBySourceElement.get(new Pair<Vertex,Element>(newEdge.getSink(), new Element("load_" + field))) == null) {
								continue;
							}
						}
						//this.binaryProductionCounts.incrementCount(binaryProduction);
					}

					if(curData == null || newData.weight < curData.weight) {
						curMinEdgeQueue.update(newEdge, newData.weight);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}
		}

		return curMinEdgeData;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Edge edge : this.edges.keySet()) {
			result.append(edge.getElement() + " " + edge.getSource() + " " + edge.getSink() + "\n");
		}
		return result.toString();
	}
}

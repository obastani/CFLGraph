package org.cflgraph.cfl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cflgraph.cfl.NormalCfl.BinaryProduction;
import org.cflgraph.cfl.NormalCfl.UnaryProduction;
import org.cflgraph.utility.Utility.Counter;
import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;

public class CFLGraph {
	protected Map<Edge,EdgeData> edges = new HashMap<Edge,EdgeData>();
	protected MultivalueMap<String,Edge> edgesBySource = new MultivalueMap<String,Edge>();

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

	//private Map<Pair<String,String>,Edge> edgesBySourceString = new HashMap<Pair<String,String>,Edge>();

	public void addEdge(String source, String sink, String element, int weight) {
		Edge edge = new Edge(source, sink, element);
		
		this.edges.put(edge, new EdgeData(edge, weight));
		this.edgesBySource.add(source, edge);

		//this.edgesBySourceString.put(new Pair<String,String>(source,element), edge);
	}

	public void addEdge(String source, String sink, String String) {
		this.addEdge(source, sink, String, 0);
	}
	
	public void addEdge(Edge edge, int weight) {
		this.addEdge(edge.source, edge.sink, edge.element, weight);
	}

	public static class Edge {
		private String source;
		private String sink;
		private String element;
		
		public Edge(String source, String sink, String element) {
			this.source = source;
			this.sink = sink;
			this.element = element;
		}

		public String getSource() {
			return this.source;
		}

		public String getSink() {
			return this.sink;
		}

		public String getElement() {
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
				Edge graphString = (Edge)object;
				return this.source.equals(graphString.getSource())
						&& this.sink.equals(graphString.getSink())
						&& this.element.equals(graphString.getElement());
			}		
		}

		@Override
		public String toString() {
			return this.element + "(" + this.source + "," + this.sink + ")"; 
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
				return forward ? this.edge.element + "(" + this.edge.source + "," + this.edge.sink + ")"
						: this.edge.element + "Bar" + "(" + this.edge.sink + "," + this.edge.source + ")";
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

	private Counter<String> elementCounts = new Counter<String>();
	
	// run Knuth's algorithms
	public Map<Edge,EdgeData> getClosure(NormalCfl cfl) {

		// maps from the minimum graph elements to their paths
		MultivalueMap<String,Edge> minEdgesBySource = new MultivalueMap<String,Edge>();
		MultivalueMap<String,Edge> minEdgesBySink = new MultivalueMap<String,Edge>();

		Heap<Edge> curMinEdgeQueue = new Heap<Edge>(); // stores the current minimum graph elements left to be processed
		Map<Edge,EdgeData> curMinEdgeData = new HashMap<Edge,EdgeData>(); // stores the current weight of a given edge		

		// step 1: fill in minGraphStringPaths and minGraphStringQueue with the initial Strings
		for(Map.Entry<Edge,EdgeData> entry : this.edges.entrySet()) {
			curMinEdgeQueue.update(entry.getKey(), entry.getValue().weight);
			curMinEdgeData.put(entry.getKey(), entry.getValue());
		}

		// step 2: for each element in minGraphStringQueue, iterate through productions and check if they are smaller
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
			minEdgesBySource.add(minEdge.getSource(), minEdge);
			minEdgesBySink.add(minEdge.getSink(), minEdge);
			elementCounts.incrementCount(minEdge.getElement());

			// TODO: fix temporary hack
			if(minEdge.element.equals("flowsTo")) {
				//Edge barEdge = new Edge(minEdge.getSink(), minEdge.getSource(), "flowsToBar");
				//curMinEdgeQueue.update(barEdge, curMinEdgeData.get(minEdge).weight);
				//curMinEdgeData.put(barEdge, new EdgeData(barEdge, curMinEdgeData.get(minEdge), false));
				
				for(Edge firstEdge : minEdgesBySink.get(minEdge.sink)) {
					for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByInputs(firstEdge.element, "flowsToBar")) {
						Edge newEdge = new Edge(firstEdge.source, minEdge.source, binaryProduction.getOutput());
						EdgeData curData = curMinEdgeData.get(newEdge);
						EdgeData newData = new EdgeData(newEdge, 0);

						if(curData == null || newData.weight < curData.weight) {
							curMinEdgeQueue.update(newEdge, newData.weight);
							curMinEdgeData.put(newEdge, newData);
						}
					}
				}
			}
			
			if(minEdge.element.startsWith("store_") && !minEdge.element.contains("^")) {
				for(Edge secondEdge : minEdgesBySink.get(minEdge.sink)) {
					for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByInputs(minEdge.element, "flowsToBar")) {
						Edge newEdge = new Edge(minEdge.source, secondEdge.source, binaryProduction.getOutput());
						EdgeData curData = curMinEdgeData.get(newEdge);
						EdgeData newData = new EdgeData(newEdge, 0);

						if(curData == null || newData.weight < curData.weight) {
							curMinEdgeQueue.update(newEdge, newData.weight);
							curMinEdgeData.put(newEdge, newData);
						}
					}
				}
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
			for(Edge secondEdge : minEdgesBySource.get(minEdge.getSink())) {
				for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByInputs(minEdge.getElement(), secondEdge.getElement())) {
					Edge newEdge = new Edge(minEdge.getSource(), secondEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(minEdge), curMinEdgeData.get(secondEdge));

					/*
					if(curData == null) {
						if(binaryProduction.getFirstInput().startsWith("flowsToField_") && !binaryProduction.getFirstInput().contains("^")) {
							String field = binaryProduction.getFirstInput().substring(13);
							if(this.edgesBySourceString.get(new Pair<String,String>(newEdge.getSink(), new String("load_" + field))) == null) {
								continue;
							}
							this.binaryProductionCounts.incrementCount(binaryProduction);
						}
					}
					*/

					if(curData == null || newData.weight < curData.weight) {
						curMinEdgeQueue.update(newEdge, newData.weight);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}

			// step 2e: update the minimum path for all pair productions using that element as the second input
			for(Edge firstEdge : minEdgesBySink.get(minEdge.getSource())) {
				for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByInputs(firstEdge.getElement(), minEdge.getElement())) {
					Edge newEdge = new Edge(firstEdge.getSource(), minEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(firstEdge), curMinEdgeData.get(minEdge));

					/*
					if(curData == null) {
						if(binaryProduction.getFirstInput().startsWith("flowsToField_") && !binaryProduction.getFirstInput().contains("^")) {
							String field = binaryProduction.getFirstInput().substring(13);
							if(this.edgesBySourceString.get(new Pair<String,String>(newEdge.getSink(), new String("load_" + field))) == null) {
								continue;
							}
						}
						this.binaryProductionCounts.incrementCount(binaryProduction);
					}
					*/

					if(curData == null || newData.weight < curData.weight) {
						curMinEdgeQueue.update(newEdge, newData.weight);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}
		}

		int k=0;
		for(Entry<String, Integer> e : elementCounts.sortedKeySet()) {
			System.out.println(e.getKey() + ": " + e.getValue());
			k++;
			if(k > 3) break;
		}
		System.out.println(i);

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

package org.cflgraph.cfl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.NormalCFL.BinaryProduction;
import org.cflgraph.cfl.NormalCFL.UnaryProduction;
import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;
import org.cflgraph.utility.Utility.PairString;

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
	
	public void addEdge(Edge edge, int weight) {
		this.edges.put(edge, new EdgeData(edge, weight));
		this.edgesBySource.add(edge.source, edge);
	}

	public void addEdge(String source, String sink, String element, int weight) {
		this.addEdge(new Edge(source, sink, element), weight);
	}

	public void addEdge(String source, String sink, String String) {
		this.addEdge(source, sink, String, 0);
	}

	public static class Edge {
		private final String source;
		private final String sink;
		private final String element;
		
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
			return this.source + " " + this.sink + " " + this.element; 
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
	
	public static CFLGraph toCFLGraph(Map<Edge,EdgeData> data) {
		CFLGraph cflGraph = new CFLGraph();
		for(Map.Entry<Edge,EdgeData> entry : data.entrySet()) {
			cflGraph.addEdge(entry.getKey(), entry.getValue().weight);
		}
		return cflGraph;
	}
	
	// run Knuth's algorithms
	public Map<Edge,EdgeData> getClosure(NormalCFL cfl) {

		// maps from the minimum graph elements to their paths
		MultivalueMap<String,Edge> minEdgesBySource = new MultivalueMap<String,Edge>();
		MultivalueMap<String,Edge> minEdgesBySink = new MultivalueMap<String,Edge>();
		
		MultivalueMap<String,Edge> minBarEdgesBySource = new MultivalueMap<String,Edge>();

		Heap<Edge> curMinEdgeQueue = new Heap<Edge>(); // stores the current minimum graph elements left to be processed
		Map<Edge,EdgeData> curMinEdgeData = new HashMap<Edge,EdgeData>(); // stores the current weight of a given edge		

		Set<String> inputs = cfl.getInputs();
		
		// step 1: fill in minGraphStringPaths and minGraphStringQueue with the initial Strings
		for(Map.Entry<Edge,EdgeData> entry : this.edges.entrySet()) {
			if(inputs.contains(entry.getKey().element)) {
				curMinEdgeQueue.update(entry.getKey(), entry.getValue().weight);
				curMinEdgeData.put(entry.getKey(), entry.getValue());
			}
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

			// TODO: fix temporary hack
			if(minEdge.element.equals("flowsTo")) {
				//Edge barEdge = new Edge(minEdge.getSink(), minEdge.getSource(), "flowsToBar");
				//curMinEdgeQueue.update(barEdge, curMinEdgeData.get(minEdge).weight);
				//curMinEdgeData.put(barEdge, new EdgeData(barEdge, curMinEdgeData.get(minEdge), false));
				
				minBarEdgesBySource.add(minEdge.sink, new Edge(minEdge.sink, minEdge.source, "flowsToBar"));
				
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
				for(Edge secondEdge : minBarEdgesBySource.get(minEdge.sink)) {
					for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByInputs(minEdge.element, "flowsToBar")) {
						Edge newEdge = new Edge(minEdge.source, secondEdge.sink, binaryProduction.getOutput());
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
						this.binaryProductionCounts.incrementCount(binaryProduction);
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
		System.out.println(i);

		return curMinEdgeData;
	}

	
	// run Knuth's algorithms
	public Map<Edge,EdgeData> getClosure2(NormalCFL cfl) {

		// maps from the minimum graph elements to their paths
		MultivalueMap<PairString,Edge> minEdgesBySourceElement = new MultivalueMap<PairString,Edge>();
		MultivalueMap<PairString,Edge> minEdgesBySinkElement = new MultivalueMap<PairString,Edge>();
		
		Heap<Edge> curMinEdgeQueue = new Heap<Edge>(); // stores the current minimum graph elements left to be processed
		Map<Edge,EdgeData> curMinEdgeData = new HashMap<Edge,EdgeData>(); // stores the current weight of a given edge		

		Set<String> inputs = cfl.getInputs();
		
		// step 1: fill in minGraphStringPaths and minGraphStringQueue with the initial Strings
		for(Map.Entry<Edge,EdgeData> entry : this.edges.entrySet()) {
			if(inputs.contains(entry.getKey().element)) {
				curMinEdgeQueue.update(entry.getKey(), entry.getValue().weight);
				curMinEdgeData.put(entry.getKey(), entry.getValue());
			}
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
			minEdgesBySourceElement.add(new PairString(minEdge.source, minEdge.element), minEdge);
			minEdgesBySinkElement.add(new PairString(minEdge.sink, minEdge.element), minEdge);

			// TODO: fix temporary hack
			if(minEdge.element.equals("flowsTo")) {
				Edge barEdge = new Edge(minEdge.getSink(), minEdge.getSource(), "flowsToBar");
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
				for(Edge secondEdge : minEdgesBySourceElement.get(new PairString(minEdge.getSink(), binaryProduction.getSecondInput()))) {
					Edge newEdge = new Edge(minEdge.getSource(), secondEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(minEdge), curMinEdgeData.get(secondEdge));

					/*
					if(curData == null) {
						this.binaryProductionCounts.incrementCount(binaryProduction);
					}
					*/

					if(curData == null || newData.weight < curData.weight) {
						curMinEdgeQueue.update(newEdge, newData.weight);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}

			// step 2e: update the minimum path for all pair productions using that element as the second input
			for(BinaryProduction binaryProduction : cfl.getBinaryProductionsBySecondInput(minEdge.getElement())) {
				for(Edge firstEdge : minEdgesBySinkElement.get(new PairString(minEdge.getSource(), binaryProduction.getFirstInput()))) {
					Edge newEdge = new Edge(firstEdge.getSource(), minEdge.getSink(), binaryProduction.getOutput());
					EdgeData curData = curMinEdgeData.get(newEdge);
					EdgeData newData = new EdgeData(newEdge, curMinEdgeData.get(firstEdge), curMinEdgeData.get(minEdge));

					/*
					if(curData == null) {
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
		System.out.println(i);

		return curMinEdgeData;
	}
	
	public String toString(Set<String> outputs) {
		StringBuilder result = new StringBuilder();
		for(Edge edge : this.edges.keySet()) {
			if(outputs != null && outputs.contains(edge.element)) {
				result.append(edge.toString() + " " + this.edges.get(edge).weight + "\n");
			}
		}
		return result.toString();
		
	}

	@Override
	public String toString() {
		return this.toString(null);
	}
}

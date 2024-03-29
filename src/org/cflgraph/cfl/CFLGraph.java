package org.cflgraph.cfl;

import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cflgraph.cfl.NormalCfl.BinaryProduction;
import org.cflgraph.cfl.NormalCfl.UnaryProduction;
import org.cflgraph.utility.Utility.Factory;
import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;
import org.cflgraph.utility.Utility.MultivalueMapInt;
import org.cflgraph.utility.Utility.PairInt;

public abstract class CFLGraph extends HashSet<Integer> {
	private static final long serialVersionUID = -8194089350311320116L;

	/*
	public CFLGraph(BufferedReader input) throws IOException {
		String line;
		while((line = input.readLine()) != null) {
			String[] params = line.split(" ");
			if(params.length == 3) {
				this.addEdge(new Vertex(params[0]), new Vertex(params[1]), new Element(params[2]));
			} else if(params.length == 4) {
				int weight = Integer.parseInt(params[3]);
				this.addEdge(new Vertex(params[0]), new Vertex(params[1]), new Element(params[2]), weight);
			}		    
		}
	}
	*/
	
	public abstract NormalCfl getNormalCfl();
	
	public Factory<String> vertices = new Factory<String>();

	protected TObjectByteHashMap<Edge> edges = new TObjectByteHashMap<Edge>();
	protected MultivalueMapInt<Edge> edgesBySource = new MultivalueMapInt<Edge>();
	protected MultivalueMapInt<Edge> edgesBySink = new MultivalueMapInt<Edge>();

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

	private Map<PairInt,Edge> edgesBySourceElement = new HashMap<PairInt,Edge>();

	public void addEdge(int source, int sink, int element, byte weight) {
		super.add(source);
		super.add(sink);
		
		Edge edge = new Edge(source, sink, element);
		
		this.edges.put(edge, weight);
		this.edgesBySink.add(sink, edge);
		this.edgesBySource.add(source, edge);

		this.edgesBySourceElement.put(new PairInt(source, element), edge);
	}

	public void addEdge(int source, int sink, int element) {
		this.addEdge(source, sink, element, (byte)0);
	}

	public class Edge {
		private int source;
		private int sink;
		private int element;
		
		public Edge(int source, int sink, int element) {
			this.source = source;
			this.sink = sink;
			this.element = element;
		}

		public int getSource() {
			return this.source;
		}

		public int getSink() {
			return this.sink;
		}

		public int getElement() {
			return this.element;
		}

		@Override
		public String toString() {
			return getNormalCfl().elements.getElementById(this.element) + "(" + vertices.getElementById(this.source) + "," + vertices.getElementById(this.sink) + ")"; 
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + element;
			result = prime * result + sink;
			result = prime * result + source;
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
			Edge other = (Edge) obj;
			if (element != other.element)
				return false;
			if (sink != other.sink)
				return false;
			if (source != other.source)
				return false;
			return true;
		}
	}
	
	/*
	public class EdgeData {
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
	*/

	// run Knuth's algorithms
	public TObjectByteHashMap<Edge> getClosure() {
		NormalCfl cfl = this.getNormalCfl();

		// maps from the minimum graph elements to their paths
		MultivalueMap<PairInt,Edge> minEdgesBySourceElement = new MultivalueMap<PairInt,Edge>();
		MultivalueMap<PairInt,Edge> minEdgesBySinkElement = new MultivalueMap<PairInt,Edge>();

		Heap<Edge> curMinEdgeQueue = new Heap<Edge>(); // stores the current minimum graph elements left to be processed
		TObjectByteHashMap<Edge> curMinEdgeData = new TObjectByteHashMap<Edge>(); // stores the current weight of a given edge
		
		// some important elements
		int flowsTo = getNormalCfl().elements.getIdByElement("flowsTo");
		int flowsToBar = getNormalCfl().elements.getIdByElement("flowsToBar");

		// step 1: fill in minGraphElementPaths and minGraphElementQueue with the initial Elements
		for(int source : this) {
			for(Edge edge : this.edgesBySource.get(source)) {
				curMinEdgeQueue.update(edge, this.edges.get(edge));
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
			minEdgesBySourceElement.add(new PairInt(minEdge.getSource(), minEdge.getElement()), minEdge);
			minEdgesBySinkElement.add(new PairInt(minEdge.getSink(), minEdge.getElement()), minEdge);

			// TODO: fix temporary hack
			if(minEdge.getElement() == flowsTo) {
				Edge barEdge = new Edge(minEdge.getSink(), minEdge.getSource(), flowsToBar);
				curMinEdgeQueue.update(barEdge, curMinEdgeData.get(minEdge));
				curMinEdgeData.put(barEdge, curMinEdgeData.get(minEdge));
			}

			// step 2c: update the minimum path for all single productions using that element
			for(UnaryProduction unaryProduction : cfl.getUnaryProductionsByInput(minEdge.getElement())) {
				Edge newEdge = new Edge(minEdge.getSource(), minEdge.getSink(), unaryProduction.getOutput());
				byte newData = curMinEdgeData.get(minEdge);
				/*
				if(curData == null) {
					this.unaryProductionCounts.incrementCount(unaryProduction);
				}
				*/
				if(!curMinEdgeData.containsKey(newEdge) || newData < curMinEdgeData.get(newEdge)) {
					curMinEdgeQueue.update(newEdge, newData);
					curMinEdgeData.put(newEdge, newData);
				}
			}

			// step 2d: update the minimum path for all PairInt productions using that element as the first input
			for(BinaryProduction binaryProduction : cfl.getBinaryProductionsByFirstInput(minEdge.getElement())) {
				for(Edge secondEdge : minEdgesBySourceElement.get(new PairInt(minEdge.getSink(), binaryProduction.getSecondInput()))) {
					Edge newEdge = new Edge(minEdge.getSource(), secondEdge.getSink(), binaryProduction.getOutput());
					byte newData = (byte) (curMinEdgeData.get(minEdge) + curMinEdgeData.get(secondEdge));

					if(!curMinEdgeData.containsKey(newEdge)) {
						String inputName = cfl.elements.getElementById(binaryProduction.getFirstInput());
						if(inputName.startsWith("flowsToField_") && !inputName.contains("^")) {
							String field = inputName.substring(13);
							if(this.edgesBySourceElement.get(new PairInt(newEdge.getSink(), cfl.elements.getIdByElement("load_" + field))) == null) {
								continue;
							}
							//this.binaryProductionCounts.incrementCount(binaryProduction);
						}
					}

					if(!curMinEdgeData.containsKey(newEdge) || newData < curMinEdgeData.get(newEdge)) {
						curMinEdgeQueue.update(newEdge, newData);
						curMinEdgeData.put(newEdge, newData);
					}
				}
			}

			// step 2e: update the minimum path for all PairInt productions using that element as the second input
			for(BinaryProduction binaryProduction : cfl.getBinaryProductionsBySecondInput(minEdge.getElement())) {
				for(Edge firstEdge : minEdgesBySinkElement.get(new PairInt(minEdge.getSource(), binaryProduction.getFirstInput()))) {
					Edge newEdge = new Edge(firstEdge.getSource(), minEdge.getSink(), binaryProduction.getOutput());
					byte newData = (byte) (curMinEdgeData.get(firstEdge) + curMinEdgeData.get(minEdge));
					
					if(!curMinEdgeData.containsKey(newEdge)) {
						String inputName = cfl.elements.getElementById(binaryProduction.getFirstInput());
						if(inputName.startsWith("flowsToField_") && !inputName.contains("^")) {
							String field = inputName.substring(13);
							if(this.edgesBySourceElement.get(new PairInt(newEdge.getSink(), cfl.elements.getIdByElement("load_" + field))) == null) {
								continue;
							}
						}
						//this.binaryProductionCounts.incrementCount(binaryProduction);
					}

					if(!curMinEdgeData.containsKey(newEdge) || newData < curMinEdgeData.get(newEdge)) {
						curMinEdgeQueue.update(newEdge, newData);
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
			result.append(this.getNormalCfl().elements.getElementById(edge.getElement()) + " " + this.vertices.getElementById(edge.getSource()) + " " + this.vertices.getElementById(edge.getSink()) + "\n");
		}
		return result.toString();
	}
}

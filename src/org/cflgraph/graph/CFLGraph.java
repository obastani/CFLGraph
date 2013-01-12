package org.cflgraph.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.Element;
import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.Element.Variable;
import org.cflgraph.cfl.NormalCFL;
import org.cflgraph.cfl.NormalCFL.PairProduction;
import org.cflgraph.cfl.NormalCFL.SingleProduction;
import org.cflgraph.graph.CFLGraph.Vertex;
import org.cflpath.utility.Utility.Heap;
import org.cflpath.utility.Utility.MultivalueMap;
import org.cflpath.utility.Utility.Pair;
import org.cflpath.utility.Utility.Triple;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = 1L;
	
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

	private MultivalueMap<Pair<Vertex,Vertex>,Terminal> edges = new MultivalueMap<Pair<Vertex,Vertex>,Terminal>();
	private MultivalueMap<Vertex,Pair<Vertex,Terminal>> incomingEdges = new MultivalueMap<Vertex,Pair<Vertex,Terminal>>();
	private MultivalueMap<Vertex,Pair<Vertex,Terminal>> outgoingEdges = new MultivalueMap<Vertex,Pair<Vertex,Terminal>>();
	
	private Map<Triple<Vertex,Vertex,Terminal>,Integer> weights = new HashMap<Triple<Vertex,Vertex,Terminal>,Integer>();
	
	public void addEdge(Vertex source, Vertex sink, Terminal terminal, int weight) {
		super.add(source);
		super.add(sink);
		
		this.weights.put(new Triple<Vertex,Vertex,Terminal>(source, sink, terminal), weight);

		this.edges.add(new Pair<Vertex,Vertex>(source,sink), terminal);
		this.incomingEdges.add(sink, new Pair<Vertex,Terminal>(source,terminal));
		this.outgoingEdges.add(source, new Pair<Vertex,Terminal>(sink,terminal));		
	}

	public void addEdge(Vertex source, Vertex sink, Terminal terminal) {
		this.addEdge(source, sink, terminal, 0);
	}

	public class Path {
		private LinkedList<Pair<Vertex,Terminal>> pairs = new LinkedList<Pair<Vertex,Terminal>>();
		private Vertex start;
		private int weight;
		
		public Path() {
			this.weight = 0;
		}
		
		@SafeVarargs
		public Path(Vertex start, Pair<Vertex,Terminal> ... pairs) {
			this.weight = 0;
			this.start = start;
			for(Pair<Vertex,Terminal> pair : pairs) {
				this.add(pair);
			}
		}
		
		public Path(Path ... paths) {
			this.weight = 0;
			this.start = paths.length > 0 ? paths[0].getStart() : null;
			for(Path path : paths) {
				this.add(path);
			}
		}
		
		public void add(Pair<Vertex,Terminal> pair) {
			Vertex source = this.pairs.size() == 0 ? this.start : this.pairs.get(this.pairs.size()-1).getX();
			Integer weight = weights.get(new Triple<Vertex,Vertex,Terminal>(source, pair.getX(), pair.getY()));
			if(weight != null) {
				this.weight += weight;
			}
			this.pairs.add(pair);
		}
		
		public void add(Path path) {
			this.pairs.addAll(path.getPairs());
			this.weight += path.getWeight();
		}
		
		public List<Pair<Vertex,Terminal>> getPairs() {
			return this.pairs;
		}
		
		public Vertex getStart() {
			return this.start;
		}
		
		public int getWeight() {
			return this.weight;
		}
		
		public Path reverse() {
			
			Path reversePath = null;
			Terminal prevTerminal = null;
			
			boolean last = true;
			for(Iterator<Pair<Vertex,Terminal>> iter = this.pairs.descendingIterator(); iter.hasNext();) {
				Pair<Vertex,Terminal> pair = iter.next();
				
				if(last) {
					reversePath = new Path(pair.getX());
					last = false;
				} else {
					reversePath.add(new Pair<Vertex,Terminal>(pair.getX(), prevTerminal));
				}
				prevTerminal = pair.getY();
			}
			
			reversePath.add(new Pair<Vertex,Terminal>(this.start, prevTerminal));
			
			return reversePath;
		}
	}
	
	public static class GraphElement {
		private Vertex source;
		private Vertex sink;
		private Element element;
		
		public GraphElement(Vertex source, Vertex sink, Element element) {
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
				GraphElement graphElement = (GraphElement)object;
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
	
	// run Knuth's algorithms
	public Map<GraphElement,Path> getShortestPaths(NormalCFL cfl) {

		// maps from the minimum graph elements to their paths
		Map<GraphElement,Path> minGraphElementPaths = new HashMap<GraphElement,Path>();
		MultivalueMap<Pair<Vertex,Element>,Pair<Vertex,Path>> minGraphElementPathsBySource = new MultivalueMap<Pair<Vertex,Element>,Pair<Vertex,Path>>();
		MultivalueMap<Pair<Vertex,Element>,Pair<Vertex,Path>> minGraphElementPathsBySink = new MultivalueMap<Pair<Vertex,Element>,Pair<Vertex,Path>>();

		Map<GraphElement,Path> curMinGraphElementPaths = new HashMap<GraphElement,Path>(); // stores the current minimum element paths
		Heap<GraphElement> curMinGraphElementQueue = new Heap<GraphElement>(); // stores the current minimum graph elements left to be processed		

		// step 1: fill in minGraphElementPaths and minGraphElementQueue with the initial terminals
		for(Vertex source : this) {
			for(Pair<Vertex,Terminal> pair : this.outgoingEdges.get(source)) {
				GraphElement element = new GraphElement(source, pair.getX(), pair.getY());
				curMinGraphElementPaths.put(element, new Path(source, pair));
				curMinGraphElementQueue.push(element, this.weights.get(new Triple<Vertex,Vertex,Element>(source, pair.getX(), pair.getY())));
			}
		}

		// step 2: for each element in minGraphElementQueue, iterate through productions and check if they are smaller
		int i=0;
		while(!curMinGraphElementQueue.isEmpty()) {

		    // step 2a: get the minimum element
		    GraphElement minElement = curMinGraphElementQueue.pop();
		    
		    // TODO: remove printing
		    if(i%10000 == 0) {
		    	System.out.println("Current iteration: " + i);
		    	System.out.println("Current heap size: " + curMinGraphElementQueue.size());
		    }
		    i++;

		    // step 2b: add the minimum element to the map
		    Path minPath = curMinGraphElementPaths.get(minElement);
		    minGraphElementPaths.put(minElement, minPath);
		    minGraphElementPathsBySource.add(new Pair<Vertex,Element>(minElement.getSource(),minElement.getElement()), new Pair<Vertex,Path>(minElement.getSink(),minPath));
		    minGraphElementPathsBySink.add(new Pair<Vertex,Element>(minElement.getSink(),minElement.getElement()), new Pair<Vertex,Path>(minElement.getSource(),minPath));
		    
		    // TODO: fix temporary hack
		    if(minElement.getElement().equals(new Variable("flowsTo")) || minElement.getElement().equals(new Variable("label"))) {
		    	GraphElement barElement = new GraphElement(minElement.getSink(), minElement.getSource(), new Variable(minElement.getElement().getName() + "Bar"));
		    	Path barPath = minPath.reverse();
			    minGraphElementPaths.put(barElement, barPath);
			    minGraphElementPathsBySource.add(new Pair<Vertex,Element>(barElement.getSource(),barElement.getElement()), new Pair<Vertex,Path>(barElement.getSink(),barPath));
			    minGraphElementPathsBySink.add(new Pair<Vertex,Element>(barElement.getSink(),barElement.getElement()), new Pair<Vertex,Path>(barElement.getSource(),barPath));		    	
		    }
		    
		    // step 2c: update the minimum path for all single productions using that element
		    for(SingleProduction singleProduction : cfl.getSingleProductionsByInput(minElement.getElement())) {
		    	GraphElement curElement = new GraphElement(minElement.getSource(), minElement.getSink(), singleProduction.getTarget());
		    	
		        Path curPath = curMinGraphElementPaths.get(curElement);
		        Path newPath = minGraphElementPaths.get(minElement);

		        if(curPath == null || newPath.getWeight() < curPath.getWeight()) {
		            curMinGraphElementQueue.update(curElement, newPath.getWeight());
		            curMinGraphElementPaths.put(curElement, newPath);
		        }
		    }

		    // step 2d: update the minimum path for all pair productions using that element as the first input
		    for(PairProduction pairProduction : cfl.getPairProductionsByFirstInput(minElement.getElement())) {
		    	for(Pair<Vertex,Path> pair : minGraphElementPathsBySource.get(new Pair<Vertex,Element>(minElement.getSink(), pairProduction.getSecondInput()))) {
		    		Path secondPath = minGraphElementPaths.get(new GraphElement(minElement.getSink(), pair.getX(), pairProduction.getSecondInput()));
		    		if(secondPath != null) {
		    			GraphElement curElement = new GraphElement(minElement.getSource(), pair.getX(), pairProduction.getTarget());
			    		
		    			Path curPath = curMinGraphElementPaths.get(curElement);
		    			Path newPath = new Path(minGraphElementPaths.get(minElement), secondPath);
		    			
		    			if(curPath == null || newPath.getWeight() < curPath.getWeight()) {
		    				curMinGraphElementQueue.update(curElement, newPath.getWeight());
		    				curMinGraphElementPaths.put(curElement, newPath);
		    			}
		    		}
		    	}
		    }

		    // step 2e: update the minimum path for all pair productions using that element as the second input
		    for(PairProduction pairProduction : cfl.getPairProductionsBySecondInput(minElement.getElement())) {
		    	for(Pair<Vertex,Path> pair : minGraphElementPathsBySink.get(new Pair<Vertex,Element>(minElement.getSource(), pairProduction.getFirstInput()))) {
		    		Path firstPath = minGraphElementPaths.get(new GraphElement(pair.getX(), minElement.getSource(), pairProduction.getFirstInput()));
		    		if(firstPath != null) {
		    			GraphElement curElement = new GraphElement(pair.getX(), minElement.getSink(), pairProduction.getTarget());
			    		
		    			Path curPath = curMinGraphElementPaths.get(curElement);
		    			Path newPath = new Path(firstPath, minGraphElementPaths.get(minElement));
		    			
		    			if(curPath == null || newPath.getWeight() < curPath.getWeight()) {
		    				curMinGraphElementQueue.update(curElement, newPath.getWeight());
		    				curMinGraphElementPaths.put(curElement, newPath);
		    			}
		    		}
		    	}
		    }

		}
		
		return curMinGraphElementPaths;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Map.Entry<Pair<Vertex,Vertex>,Set<Terminal>> entry : this.edges.entrySet()) {
			for(Terminal terminal : entry.getValue()) {
				result.append("(" + entry.getKey().getX().toString() + ","
						+ entry.getKey().getY().toString() + ","
						+ terminal.toString() + ")" + "\n");
			}
		}
		return result.toString();
	}
}

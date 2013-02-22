package org.cflgraph.cfl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.CFLGraph.Vertex;
import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.Element.Variable;
import org.cflgraph.cfl.NormalCFL.PairProduction;
import org.cflgraph.cfl.NormalCFL.SingleProduction;
import org.cflgraph.utility.Utility.Counter;
import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;
import org.cflgraph.utility.Utility.Pair;
import org.cflgraph.utility.Utility.Triple;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = 1L;

	public CFLGraph() {};

	public CFLGraph(BufferedReader input) throws IOException {
	    String line;
	    while((line = input.readLine()) != null) {
		String[] params = line.split(" ");
		if(params.length == 3) {
		    this.addEdge(new Vertex(params[0]), new Vertex(params[1]), new Terminal(params[2]));
		} else if(params.length == 4) {
		    int weight = Integer.parseInt(params[3]);
		    this.addEdge(new Vertex(params[0]), new Vertex(params[1]), new Terminal(params[2]), weight);
		}		    
	    }
	}
	
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

	protected MultivalueMap<Pair<Vertex,Vertex>,Terminal> edges = new MultivalueMap<Pair<Vertex,Vertex>,Terminal>();
	protected MultivalueMap<Vertex,Pair<Vertex,Terminal>> incomingEdges = new MultivalueMap<Vertex,Pair<Vertex,Terminal>>();
	protected MultivalueMap<Vertex,Pair<Vertex,Terminal>> outgoingEdges = new MultivalueMap<Vertex,Pair<Vertex,Terminal>>();
	
	protected Map<Triple<Vertex,Vertex,Terminal>,Integer> weights = new HashMap<Triple<Vertex,Vertex,Terminal>,Integer>();
	
	private Counter<SingleProduction> singleProductionCounts = new Counter<SingleProduction>();
	private Counter<PairProduction> pairProductionCounts = new Counter<PairProduction>();
	
	public Counter<PairProduction> getPairProductionCounts() {
		return this.pairProductionCounts;
	}
	
	public Counter<SingleProduction> getSingleProductionCounts() {
		return this.singleProductionCounts;
	}
	
	private Map<Pair<Vertex,Terminal>,Vertex> outgoingEdgesByTerminal = new HashMap<Pair<Vertex,Terminal>,Vertex>();
	
	public void addEdge(Vertex source, Vertex sink, Terminal terminal, int weight) {
		super.add(source);
		super.add(sink);
		
		this.weights.put(new Triple<Vertex,Vertex,Terminal>(source, sink, terminal), weight);

		this.edges.add(new Pair<Vertex,Vertex>(source,sink), terminal);
		this.incomingEdges.add(sink, new Pair<Vertex,Terminal>(source,terminal));
		this.outgoingEdges.add(source, new Pair<Vertex,Terminal>(sink,terminal));
		
		this.outgoingEdgesByTerminal.put(new Pair<Vertex,Terminal>(source,terminal), sink);
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
				prevTerminal = new Terminal(pair.getY().getName() + "Bar");
			}
			reversePath.add(new Pair<Vertex,Terminal>(this.start, prevTerminal));
			return reversePath;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.start);
			for(Pair<Vertex,Terminal> pair : this.pairs) {
				sb.append(" " + pair.getY() + " " + pair.getX());
			}
			return sb.toString();
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
		    if(minElement.getElement().equals(new Variable("flowsTo"))) {
		    	GraphElement barElement = new GraphElement(minElement.getSink(), minElement.getSource(), new Variable(minElement.getElement().getName() + "Bar"));
		    	Path barPath = minPath.reverse();
		    	curMinGraphElementQueue.update(barElement, barPath.getWeight());
		    	curMinGraphElementPaths.put(barElement, barPath);
		    }
		    
		    // step 2c: update the minimum path for all single productions using that element
		    for(SingleProduction singleProduction : cfl.getSingleProductionsByInput(minElement.getElement())) {
		    	GraphElement curElement = new GraphElement(minElement.getSource(), minElement.getSink(), singleProduction.getTarget());
		    	
		        Path curPath = curMinGraphElementPaths.get(curElement);
		        Path newPath = minGraphElementPaths.get(minElement);
	            
	            if(curPath == null) {
	            	this.singleProductionCounts.incrementCount(singleProduction);
	            }

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

			            if(curPath == null) {
			            	if(pairProduction.getFirstInput().getName().startsWith("flowsToField_") && !pairProduction.getFirstInput().getName().contains("^")) {
			            		String field = pairProduction.getFirstInput().getName().substring(13);
			            		if(this.outgoingEdgesByTerminal.get(new Pair<Vertex,Terminal>(curElement.getSink(), new Terminal("load_" + field))) == null) {
			            			continue;
			            		}
			            	}
			            	this.pairProductionCounts.incrementCount(pairProduction);
			            }
			            
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
		    			
			            if(curPath == null) {
			            	if(pairProduction.getFirstInput().getName().startsWith("flowsToField_") && !pairProduction.getFirstInput().getName().contains("^")) {
			            		String field = pairProduction.getFirstInput().getName().substring(13);
			            		if(this.outgoingEdgesByTerminal.get(new Pair<Vertex,Terminal>(curElement.getSink(), new Terminal("load_" + field))) == null) {
			            			continue;
			            		}
			            	}
			            	this.pairProductionCounts.incrementCount(pairProduction);
			            }
		    			
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
	
	public Set<GraphElement> getProductions(NormalCFL normalCfl) {
		LinkedList<GraphElement> workflow = new LinkedList<GraphElement>();
		
		Set<GraphElement> newElements = new HashSet<GraphElement>();
		
		Set<GraphElement> elements = new HashSet<GraphElement>();
		MultivalueMap<Pair<Vertex,Vertex>, Element> elementsByVertices = new MultivalueMap<Pair<Vertex,Vertex>, Element>();
		MultivalueMap<Pair<Vertex,Element>, Vertex> elementsBySource = new MultivalueMap<Pair<Vertex,Element>, Vertex>();
		MultivalueMap<Pair<Vertex,Element>, Vertex> elementsBySink = new MultivalueMap<Pair<Vertex,Element>, Vertex>();
		
		for(Pair<Vertex,Vertex> pair : this.edges.keySet()) {
			for(Terminal terminal : this.edges.get(pair)) {
				newElements.add(new GraphElement(pair.getX(), pair.getY(), terminal));
			}
		}
		
		// add elements in set of elements to be added
		for(GraphElement graphElement : newElements) {
			Vertex source = graphElement.getSource();
			Vertex sink = graphElement.getSink();
			Element element = graphElement.getElement();
			
			if(!elementsByVertices.get(new Pair<Vertex,Vertex>(source, sink)).contains(element)) {
				workflow.add(graphElement);
				elements.add(graphElement);
				elementsByVertices.add(new Pair<Vertex,Vertex>(source, sink), element);
				elementsBySource.add(new Pair<Vertex,Element>(source, element), sink);
				elementsBySink.add(new Pair<Vertex,Element>(sink,element), source);
			}
		}
		newElements.clear();

		int i=0;
		while(!workflow.isEmpty()) {
			if(i%10000 == 0) {
				System.out.println(i);
				System.out.println(workflow.size());
				System.out.println();
			}
			i++;
			
			GraphElement currentElement = workflow.remove();
			
			Vertex source = currentElement.getSource();
			Vertex sink = currentElement.getSink();
			Element element = currentElement.getElement();
			
			// TODO: fix temporary hack
			if(currentElement.getElement().getName().equals("flowsTo")) {
				newElements.add(new GraphElement(sink, source, new Variable("flowsToBar")));
			}

			for(SingleProduction singleProduction : normalCfl.getSingleProductionsByInput(element)) {
				newElements.add(new GraphElement(source, sink, singleProduction.getTarget()));
			}

			for(PairProduction pairProduction : normalCfl.getPairProductionsByFirstInput(element)) {
				for(Vertex newSink : elementsBySource.get(new Pair<Vertex,Element>(sink, pairProduction.getSecondInput()))) {
					newElements.add(new GraphElement(source, newSink, pairProduction.getTarget()));
				}
			}
			
			for(PairProduction pairProduction : normalCfl.getPairProductionsBySecondInput(element)) {
				for(Vertex newSource : elementsBySink.get(new Pair<Vertex,Element>(source, pairProduction.getFirstInput()))) {
					newElements.add(new GraphElement(newSource, sink, pairProduction.getTarget()));
				}
			}
			
			// add elements in set of elements to be added
			for(GraphElement graphElement : newElements) {
				Vertex newSource = graphElement.getSource();
				Vertex newSink = graphElement.getSink();
				Element newElement = graphElement.getElement();
				
				if(!elementsByVertices.get(new Pair<Vertex,Vertex>(newSource, newSink)).contains(newElement)) {
					workflow.add(graphElement);
					elements.add(graphElement);
					elementsByVertices.add(new Pair<Vertex,Vertex>(newSource, newSink), newElement);
					elementsBySource.add(new Pair<Vertex,Element>(newSource, newElement), newSink);
					elementsBySink.add(new Pair<Vertex,Element>(newSink,newElement), newSource);
				}
			}
			newElements.clear();
		}
		
		return elements;
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

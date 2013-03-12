package org.cflgraph.groupcfl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cflgraph.utility.Utility.Heap;
import org.cflgraph.utility.Utility.MultivalueMap;

public class GroupCFL {
	
	// groups in which to place new elements
	protected MultivalueMap<String,String> elementsToGroups = new MultivalueMap<String,String>();

	// unary productions
	protected MultivalueMap<String,String> unaryProductions = new MultivalueMap<String,String>();
	
	// co-input groups for binary productions
	protected MultivalueMap<String,String> elementsToLeftCoInputGroups = new MultivalueMap<String,String>();
	protected MultivalueMap<String,String> elementsToRightCoInputGroups = new MultivalueMap<String,String>();
	
	// inverted elements
	protected Set<String> invertedElements = new HashSet<String>();
	
	// reductions to apply (both unary and binary)
	protected Map<String,String> reductions = new HashMap<String,String>();
	
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
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((element == null) ? 0 : element.hashCode());
			result = prime * result + ((sink == null) ? 0 : sink.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.hashCode());
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
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element))
				return false;
			if (sink == null) {
				if (other.sink != null)
					return false;
			} else if (!sink.equals(other.sink))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return this.source + " " + this.sink + " " + this.element;
		}
	}
	
	public class GroupGraph {
		private MultivalueMap<String,Edge> sourcesToEdges = new MultivalueMap<String,Edge>();
		private MultivalueMap<String,Edge> sinksToEdges = new MultivalueMap<String,Edge>();		
		
		public void add(Edge edge) {
			this.sourcesToEdges.add(edge.source, edge);
			this.sinksToEdges.add(edge.sink, edge);
		}
		
		public Set<Edge> getEdgesBySource(String source) {
			return this.sourcesToEdges.get(source);
		}
		
		public Set<Edge> getEdgesBySink(String sink) {
			return this.sinksToEdges.get(sink);
		}
	}
	
	public String getProduction(String firstElement, String secondElement) {
		String concatenatedElement = firstElement + "^" + secondElement;
		String reducedElement = this.reductions.get(concatenatedElement );
		return reducedElement == null ? concatenatedElement : reducedElement; 
	}
	
	public class Graph {
		private Map<String,GroupGraph> groupsToGroupGraphs = new HashMap<String,GroupGraph>();
		private Set<Edge> edges = new HashSet<Edge>();
		
		public Graph() {
			for(Set<String> groups : elementsToGroups.values()) {
				for(String group : groups) {
					this.groupsToGroupGraphs.put(group, new GroupGraph());
				}
			}
		}
		
		public void add(Edge edge) {
			this.edges.add(edge);
			for(String group : elementsToGroups.get(edge.element)) {
				this.groupsToGroupGraphs.get(group).add(edge);
			}
		}
		
		public GroupGraph getGroupGraphByGroup(String group) {
			return this.groupsToGroupGraphs.get(group);
		}
		
		public boolean contains(Edge edge) {
			return this.edges.contains(edge);
		}
	}
	
	// TODO: fix edge weights in worklist
	public void getClosure(Map<Edge,Integer> edges) {
		int i = 0;
		// Step 1: setup the graph, its group graphs, and the heap of elements
		Graph graph = new Graph();
		Heap<Edge> worklist = new Heap<Edge>();
		for(Map.Entry<Edge,Integer> entry : edges.entrySet()) {
			worklist.update(entry.getKey(), entry.getValue());
		}
		
		// Step 2: process edges in the heap
		while(!worklist.isEmpty()) {
			// printing
			if(++i%10000 == 0) System.out.println(i);
			
			// Step 2a: remove the lowest weight edge
			Edge minEdge = worklist.pop();
			
			// Step 2b: add the edge to the graph
			graph.add(minEdge);
			edges.put(minEdge, 0); // TODO: remove this
			
			// Step 2c: if the edge is inverted, update the inverse edge in the heap
			if(this.invertedElements.contains(minEdge.element)) {
				Edge newEdge = new Edge(minEdge.sink, minEdge.source, "!" + minEdge.element);
				if(!graph.contains(newEdge)) {
					worklist.update(newEdge, 0);
				}
			}
			
			// Step 2d: for each of the unary productions, update the produced edge in the heap
			for(String newElement : this.unaryProductions.get(minEdge.element)) {
				Edge newEdge = new Edge(minEdge.source, minEdge.sink, newElement);
				if(!graph.contains(newEdge)) {
					worklist.update(newEdge, 0);
				}
			}
			
			// Step 2e: for each of the left and right co-inputs, update the edge in the heaps
			for(String group : this.elementsToLeftCoInputGroups.get(minEdge.element)) {
				for(Edge leftEdge : graph.getGroupGraphByGroup(group).getEdgesBySink(minEdge.source)) {
					Edge newEdge = new Edge(leftEdge.source, minEdge.sink, this.getProduction(leftEdge.element, minEdge.element));
					if(!graph.contains(newEdge)) {
						worklist.update(newEdge, 0);
					}
				}
			}
			for(String group : this.elementsToRightCoInputGroups.get(minEdge.element)) {
				for(Edge rightEdge : graph.getGroupGraphByGroup(group).getEdgesBySource(minEdge.sink)) {
					Edge newEdge = new Edge(minEdge.source, rightEdge.sink, this.getProduction(minEdge.element, rightEdge.element));
					if(!graph.contains(newEdge)) {
						worklist.update(newEdge, 0);
					}
				}
			}
		}
	}
}

package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Set;

public class FlowsToGraph extends CFLGraph {
	private static final long serialVersionUID = 3857460445048824005L;

	private Set<String> fields = new HashSet<String>();
	public void addField(String field) { this.fields.add(field); }
	
	private NormalCfl normalCfl = new NormalCfl();
	
	public void addStubMethod(Set<Integer> args, Integer ret, String methodSignature) {
		int passThrough = this.normalCfl.elements.getIdByElement("passThrough");
		for(int firstArg : args) {
			for(int secondArg : args) {
				if(firstArg != secondArg) {
					super.addEdge(firstArg, secondArg, passThrough, (byte)1);
				}
			}
		}
		if(ret != null) {
			for(int arg : args) {
				super.addEdge(arg, ret, passThrough, (byte)1);
			}
		}
	}
	
	public NormalCfl getNormalCfl() {
		int flowsTo = normalCfl.elements.getIdByElement("flowsTo");
		int flowsToBar = normalCfl.elements.getIdByElement("flowsToBar");
		int new_element = normalCfl.elements.getIdByElement("new");
		int assign = normalCfl.elements.getIdByElement("assign");		

		// flowsTo(o,a_c) <- new(o,a_c)
		this.normalCfl.add(flowsTo, new_element);

		// flowsTo(o,b_c) <- flowsTo(o,a_c) assign(a_c,b_c)
		this.normalCfl.add(flowsTo, flowsTo, assign);

		for(String field : this.fields) {
			int store_field = normalCfl.elements.getIdByElement("store_" + field);
			int load_field = normalCfl.elements.getIdByElement("load_" + field);
			
			//int flowsToField = normalCfl.elements.getIdByElement("flowsToField_" + field);
			
			//this.normalCfl.add(flowsToField, flowsTo, store_field, flowsToBar);
			//this.normalCfl.add(flowsTo, flowsToField, flowsTo, load_field);
			
			// flowsTo(o1,b_c) <- flowsTo(o1,a_c) store_f(a_c,p_c) flowsToBar(p_c,o2) flowsTo(o2,q_c) load_f(q_c,b_c)
			this.normalCfl.add(flowsTo, flowsTo, store_field, flowsToBar, flowsTo, load_field);
		}
		
		return this.normalCfl;
	}
	
	public TaintFlowGraph getTaintFlowGraph() {
		Set<Integer> taintFlowElements = new HashSet<Integer>();
		taintFlowElements.add(this.normalCfl.elements.getIdByElement("source"));
		taintFlowElements.add(this.normalCfl.elements.getIdByElement("sink"));
		taintFlowElements.add(this.normalCfl.elements.getIdByElement("passThrough"));
		
		taintFlowElements.add(this.normalCfl.elements.getIdByElement("flowsTo"));
		taintFlowElements.add(this.normalCfl.elements.getIdByElement("flowsToBar"));
		
		TaintFlowGraph taintFlowGraph = new TaintFlowGraph();
		
		for(Edge edge : this.edges.keySet()) {
			if(taintFlowElements.contains(edge.getElement())) {
				String elementString = this.normalCfl.elements.getElementById(edge.getElement()); 
				int element = taintFlowGraph.getNormalCfl().elements.getIdByElement(elementString);
				
				String sourceString = this.vertices.getElementById(edge.getSource());
				int source = taintFlowGraph.vertices.getIdByElement(sourceString);
				
				String sinkString = this.vertices.getElementById(edge.getSink());
				int sink = taintFlowGraph.vertices.getIdByElement(sinkString);
				taintFlowGraph.addEdge(source, sink, element, this.edges.get(edge));
			}
		}

		for(Edge edge : this.getClosure().keySet()) {
			if(taintFlowElements.contains(edge.getElement())) {
				String elementString = this.normalCfl.elements.getElementById(edge.getElement()); 
				int element = taintFlowGraph.getNormalCfl().elements.getIdByElement(elementString);
				
				String sourceString = this.vertices.getElementById(edge.getSource());
				int source = taintFlowGraph.vertices.getIdByElement(sourceString);
				
				String sinkString = this.vertices.getElementById(edge.getSink());
				int sink = taintFlowGraph.vertices.getIdByElement(sinkString);
				taintFlowGraph.addEdge(source, sink, element, this.edges.get(edge));
			}
		}
		
		return taintFlowGraph;
	}
}

package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowsToGraph extends CFLGraph {
	private static final long serialVersionUID = 3857460445048824005L;

	private Set<String> fields = new HashSet<String>();
	public void addField(String field) { this.fields.add(field); }
	
	// elements
	/*
	private Element new_element = new Element("new");
	private Element assign = new Element("assign");
	private Element store_(String field) { return new Element("store_" + field); }
	private Element load_(String field) { return new Element("load_" + field); }
	
	private Element flowsTo = new Element("flowsTo");
	private Element flowsToBar = new Element("flowsToBar");
	private Element flowsToField_(String field) { return new Element("flowsToField_" + field); }
	
	// elements for annotated flows
	private Element passThrough = new Element("passThrough");
	private Element source = new Element("source");
	private Element sink = new Element("sink");	
	
	// various functions
	public Element getAssign() { return this.assign; }
	public Element getNew() { return this.new_element; }	
	public void addField(String field) { this.fields.add(field); }	
	public Element getLoad(String field) { return this.load_(field); }	
	public Element getStore(String field) { return this.store_(field); }
	public Element getSource() { return this.source; }
	public Element getSink() { return this.sink; }
	public Element getPassThrough() { return this.passThrough; }
	*/
	
	private NormalCfl normalCfl = new NormalCfl();
	
	public void addStubMethod(Set<Integer> args, Integer ret, String methodSignature) {
		int passThrough = this.normalCfl.elements.getIdByElement("passThrough");
		for(int firstArg : args) {
			for(int secondArg : args) {
				if(firstArg != secondArg) {
					super.addEdge(firstArg, secondArg, passThrough, 1);
				}
			}
		}
		if(ret != null) {
			for(int arg : args) {
				super.addEdge(arg, ret, passThrough, 1);
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
			//int flowsToField = normalCfl.elements.getIdByElement("flowsToField_" + field);
			
			// flowsToField_f(o2,o1) <- flowsTo(o2,a_c) store_f(a_c,p_c) flowsToBar(p_c,o1)
			//normalCfl.add(this.flowsToField_(field), flowsTo, this.store_(field), flowsToBar);

			// flowsTo(o2,a_c) <- flowsToField_f(o2,o1) flowsTo(o1,p_c) load_f(p_c,a_c)
			//normalCfl.add(flowsTo, this.flowsToField_(field), flowsTo, this.load_(field));
			
			int store_field = normalCfl.elements.getIdByElement("store_" + field);
			int load_field = normalCfl.elements.getIdByElement("load_" + field);
			
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
		
		for(Map.Entry<Edge,Integer> entry : this.edges.entrySet()) {
			if(taintFlowElements.contains(entry.getKey().getElement())) {
				String elementString = this.normalCfl.elements.getElementById(entry.getKey().getElement()); 
				int element = taintFlowGraph.getNormalCfl().elements.getIdByElement(elementString);
				
				String sourceString = this.vertices.getElementById(entry.getKey().getSource());
				int source = taintFlowGraph.vertices.getIdByElement(sourceString);
				
				String sinkString = this.vertices.getElementById(entry.getKey().getSink());
				int sink = taintFlowGraph.vertices.getIdByElement(sinkString);
				taintFlowGraph.addEdge(source, sink, element, entry.getValue());
			}
		}

		for(Map.Entry<Edge,Integer> entry : this.getClosure().entrySet()) {
			if(taintFlowElements.contains(entry.getKey().getElement())) {
				String elementString = this.normalCfl.elements.getElementById(entry.getKey().getElement()); 
				int element = taintFlowGraph.getNormalCfl().elements.getIdByElement(elementString);
				
				String sourceString = this.vertices.getElementById(entry.getKey().getSource());
				int source = taintFlowGraph.vertices.getIdByElement(sourceString);
				
				String sinkString = this.vertices.getElementById(entry.getKey().getSink());
				int sink = taintFlowGraph.vertices.getIdByElement(sinkString);
				taintFlowGraph.addEdge(source, sink, element, entry.getValue());
			}
		}
		
		return taintFlowGraph;
	}
}

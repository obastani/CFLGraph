package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.NormalCFL.Element;

public class FlowsToGraph extends CFLGraph {
	private static final long serialVersionUID = 3857460445048824005L;

	private Set<String> fields = new HashSet<String>();
	
	// elements
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
	
	public void addStubMethod(Set<Vertex> args, Vertex ret, String methodSignature) {		
		for(Vertex firstArg : args) {
			for(Vertex secondArg : args) {
				if(!firstArg.equals(secondArg)) {
					super.addEdge(firstArg, secondArg, this.passThrough, 1);
				}
			}
		}
		if(ret != null) {
			for(Vertex arg : args) {
				super.addEdge(arg, ret, this.passThrough, 1);
			}
		}
	}
	
	public NormalCFL getFlowsToCfl() {
		NormalCFL normalCfl = new NormalCFL();

		// flowsTo(o,a_c) <- new(o,a_c)
		normalCfl.add(this.flowsTo, this.new_element);

		// flowsTo(o,b_c) <- flowsTo(o,a_c) assign(a_c,b_c)
		normalCfl.add(this.flowsTo, this.flowsTo, this.assign);

		for(String field : this.fields) {
			// flowsToField_f(o2,o1) <- flowsTo(o2,a_c) store_f(a_c,p_c) flowsToBar(p_c,o1)
			normalCfl.add(this.flowsToField_(field), this.flowsTo, this.store_(field), this.flowsToBar);

			// flowsTo(o2,a_c) <- flowsToField_f(o2,o1) flowsTo(o1,p_c) load_f(p_c,a_c)
			normalCfl.add(this.flowsTo, this.flowsToField_(field), this.flowsTo, this.load_(field));
		}
		
		return normalCfl;
	}
	
	public TaintFlowGraph getTaintFlowGraph() {
		Set<Element> taintFlowElements = new HashSet<Element>();
		taintFlowElements.add(this.source);
		taintFlowElements.add(this.sink);
		taintFlowElements.add(this.passThrough);
		
		taintFlowElements.add(this.flowsTo);
		taintFlowElements.add(this.flowsToBar);
				
		TaintFlowGraph taintFlowGraph = new TaintFlowGraph();
		
		for(Map.Entry<Edge,EdgeData> entry : this.edges.entrySet()) {
			if(taintFlowElements.contains(entry.getKey().getElement())) {
				taintFlowGraph.addEdge(entry.getKey(), entry.getValue().getWeight());
			}
		}

		for(Map.Entry<Edge, EdgeData> entry : this.getClosure().entrySet()) {
			if(taintFlowElements.contains(entry.getKey().getElement())) {
				taintFlowGraph.addEdge(entry.getKey(), entry.getValue().getWeight());
			}
		}
		
		return taintFlowGraph;
	}

	public Map<Edge,EdgeData> getClosure() {
		return super.getClosure(this.getFlowsToCfl());
	}
}

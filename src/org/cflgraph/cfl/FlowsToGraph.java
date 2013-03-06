package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowsToGraph extends CFLGraph {

	private Set<String> fields = new HashSet<String>();
	
	// elements
	private String new_element = new String("new");
	private String assign = new String("assign");
	private String store_(String field) { return new String("store_" + field); }
	private String load_(String field) { return new String("load_" + field); }
	
	private String flowsTo = new String("flowsTo");
	private String flowsToBar = new String("flowsToBar");
	private String flowsToField_(String field) { return new String("flowsToField_" + field); }
	
	// elements for annotated flows
	private String passThrough = new String("passThrough");
	private String source = new String("source");
	private String sink = new String("sink");	
	
	// various functions
	public String getAssign() { return this.assign; }
	public String getNew() { return this.new_element; }	
	public void addField(String field) { this.fields.add(field); }	
	public String getLoad(String field) { return this.load_(field); }	
	public String getStore(String field) { return this.store_(field); }
	public String getSource() { return this.source; }
	public String getSink() { return this.sink; }
	public String getPassThrough() { return this.passThrough; }
	
	public void addStubMethod(Set<String> args, String ret, String methodSignature) {		
		for(String firstArg : args) {
			for(String secondArg : args) {
				if(!firstArg.equals(secondArg)) {
					super.addEdge(firstArg, secondArg, this.passThrough, 1);
				}
			}
		}
		if(ret != null) {
			for(String arg : args) {
				super.addEdge(arg, ret, this.passThrough, 1);
			}
		}
	}
	
	public NormalCfl getFlowsToCfl() {
		NormalCfl normalCfl = new NormalCfl();

		// flowsTo(o,a_c) <- new(o,a_c)
		normalCfl.add(this.flowsTo, this.new_element);

		// flowsTo(o,b_c) <- flowsTo(o,a_c) assign(a_c,b_c)
		normalCfl.add(this.flowsTo, this.flowsTo, this.assign);

		for(String field : this.fields) {
			// flowsToField_f(o2,o1) <- flowsTo(o2,a_c) store_f(a_c,p_c) flowsToBar(p_c,o1)
			normalCfl.add(false, this.flowsToField_(field), this.flowsTo, this.store_(field), this.flowsToBar);

			// flowsTo(o2,a_c) <- flowsToField_f(o2,o1) flowsTo(o1,p_c) load_f(p_c,a_c)
			normalCfl.add(false, this.flowsTo, this.flowsToField_(field), this.flowsTo, this.load_(field));
		}
		
		return normalCfl;
	}
	
	public TaintFlowGraph getTaintFlowGraph() {
		Set<String> taintFlowStrings = new HashSet<String>();
		taintFlowStrings.add(this.source);
		taintFlowStrings.add(this.sink);
		taintFlowStrings.add(this.passThrough);
		
		taintFlowStrings.add(this.flowsTo);
		taintFlowStrings.add(this.flowsToBar);
				
		TaintFlowGraph taintFlowGraph = new TaintFlowGraph();
		
		for(Map.Entry<Edge,EdgeData> entry : this.edges.entrySet()) {
			if(taintFlowStrings.contains(entry.getKey().getElement())) {
				taintFlowGraph.addEdge(entry.getKey(), entry.getValue().getWeight());
			}
		}

		for(Map.Entry<Edge, EdgeData> entry : this.getClosure().entrySet()) {
			if(taintFlowStrings.contains(entry.getKey().getElement())) {
				taintFlowGraph.addEdge(entry.getKey(), entry.getValue().getWeight());
			}
		}
		
		return taintFlowGraph;
	}

	public Map<Edge,EdgeData> getClosure() {
		return super.getClosure(this.getFlowsToCfl());
	}
}

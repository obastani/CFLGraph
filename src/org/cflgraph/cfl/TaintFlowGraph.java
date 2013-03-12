package org.cflgraph.cfl;

import java.util.Map;

public class TaintFlowGraph extends CFLGraph {
	
	// Strings for annotated flows
	private String passThrough = new String("passThrough");
	private String source = new String("source");
	private String sink = new String("sink");

	private String flowsTo = new String("flowsTo");
	private String flowsToBar = new String("flowsToBar");

	// Strings for annotated flows
	private String sourceSinkFlow = new String("sourceSinkFlow");
	private String taints = new String("taints");

	// various functions

	public String getSource() {
		return this.source;
	}

	public String getSink() {
		return this.sink;
	}

	public String getPassThrough() {
		return this.passThrough;
	}

	public NormalCFL getTaintFlowCfl() {
		NormalCFL normalCfl = new NormalCFL();
		
		// taints(src,o) -> source(src,v), flowsToBar(v,o)
		normalCfl.add(this.taints, this.source, this.flowsToBar);
		// taints(src,o2) -> taints(src,o1), flowsTo(o1,a), passThrough(a,b), flowsToBar(b,o2)
		normalCfl.add(this.taints, this.taints, this.flowsTo, this.passThrough, this.flowsToBar);
		// sourceSinkFlow(src,sink) -> taints(src,o), flowsTo(o,p), sink(p,sink)
		normalCfl.add(this.sourceSinkFlow, this.taints, this.flowsTo, this.sink);

		return normalCfl;
	}

	public Map<Edge,EdgeData> getClosure() {
		return super.getClosure(this.getTaintFlowCfl());
	}

	public Map<Edge,EdgeData> getClosure2() {
		return super.getClosure2(this.getTaintFlowCfl());
	}
}

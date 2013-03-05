package org.cflgraph.cfl;


public class TaintFlowGraph extends CFLGraph {
	private static final long serialVersionUID = 521959041324489575L;
	
	private NormalCfl normalCfl = new NormalCfl();
	
	public TaintFlowGraph() {
		int taints = this.normalCfl.elements.getIdByElement("taints");
		int source = this.normalCfl.elements.getIdByElement("source");
		int sink = this.normalCfl.elements.getIdByElement("sink");
		
		int passThrough = this.normalCfl.elements.getIdByElement("passThrough");
		
		int flowsTo = this.normalCfl.elements.getIdByElement("flowsTo");
		int flowsToBar = this.normalCfl.elements.getIdByElement("flowsToBar");
		int sourceSinkFlow = this.normalCfl.elements.getIdByElement("sourceSinkFlow");
		
		// taints(src,o) -> source(src,v), flowsToBar(v,o)
		this.normalCfl.add(taints, source, flowsToBar);
		// taints(src,o2) -> taints(src,o1), flowsTo(o1,a), passThrough(a,b), flowsToBar(b,o2)
		this.normalCfl.add(taints, taints, flowsTo, passThrough, flowsToBar);
		// sourceSinkFlow(src,sink) -> taints(src,o), flowsTo(o,p), sink(p,sink)
		this.normalCfl.add(sourceSinkFlow, taints, flowsTo, sink);
	}

	@Override
	public NormalCfl getNormalCfl() {
		return this.normalCfl;
	}
}

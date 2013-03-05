package org.cflgraph.cfl;

import java.util.Map;

import org.cflgraph.cfl.NormalCfl.Element;

public class TaintFlowGraph extends CFLGraph {
	private static final long serialVersionUID = 521959041324489575L;
	
	// Elements for annotated flows
	private Element passThrough = new Element("passThrough");
	private Element source = new Element("source");
	private Element sink = new Element("sink");

	private Element flowsTo = new Element("flowsTo");
	private Element flowsToBar = new Element("flowsToBar");

	// Elements for annotated flows
	private Element sourceSinkFlow = new Element("sourceSinkFlow");
	private Element taints = new Element("taints");

	// various functions

	public Element getSource() {
		return this.source;
	}

	public Element getSink() {
		return this.sink;
	}

	public Element getPassThrough() {
		return this.passThrough;
	}

	public NormalCfl getTaintFlowCfl() {
		NormalCfl normalCfl = new NormalCfl();
		
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
}

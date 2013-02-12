package org.cflgraph.cfl;

import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.Element.Variable;

public class TaintFlowGraph extends CFLGraph {
	private static final long serialVersionUID = 1L;

	// terminals for annotated flows
	private Terminal passThrough = new Terminal("passThrough");
	private Terminal source = new Terminal("source");
	private Terminal sink = new Terminal("sink");

	private Terminal flowsTo = new Terminal("flowsTo");
	private Terminal flowsToBar = new Terminal("flowsToBar");

	// variables for annotated flows
	private Variable sourceSinkFlow = new Variable("sourceSinkFlow");
	private Variable taints = new Variable("taints");

	// various functions

	public Terminal getSource() {
		return this.source;
	}

	public Terminal getSink() {
		return this.sink;
	}

	public Terminal getPassThrough() {
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

	public Map<GraphElement,Path> getShortestPaths() {
		return super.getShortestPaths(this.getTaintFlowCfl());
	}

	public Set<GraphElement> getProductions() {
		return super.getProductions(this.getTaintFlowCfl());
	}
}

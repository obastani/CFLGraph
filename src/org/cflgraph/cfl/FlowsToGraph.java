package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.Element.Variable;

public class FlowsToGraph extends CFLGraph {
	private static final long serialVersionUID = 1L;

	private Set<String> fields = new HashSet<String>();
	
	// terminals
	
	private Terminal new_terminal = new Terminal("new");
	
	private Terminal assign = new Terminal("assign");
	
	//private Terminal store_star = new Terminal("store_*");
	private Terminal store_(String field) { return new Terminal("store_" + field); }
	
	//private Terminal load_star = new Terminal("load_*");
	private Terminal load_(String field) { return new Terminal("load_" + field); }
	
	// terminals for annotated flows
	private Terminal passThrough = new Terminal("passThrough");
	private Terminal source = new Terminal("source");
	private Terminal sink = new Terminal("sink");
	
	// variables
	
	//private Variable label = new Variable("label");
	
	private Variable flowsTo = new Variable("flowsTo");
	private Variable flowsToBar = new Variable("flowsToBar");
	
	private Variable flowsToField_(String field) { return new Variable("flowsToField_" + field); }

	//private Variable flowsToRight = new Variable("flowsToRight");
	
	private Variable alias = new Variable("alias");
	
	// variables for annotated flows
	private Variable sourceSinkFlow = new Variable("sourceSinkFlow");
	private Variable taints = new Variable("taints");
	
	// various functions
	
	public Terminal getAssign() {
		return this.assign;
	}
	
	public Terminal getNew() {
		return this.new_terminal;
	}
	
	public void addField(String field) {
		this.fields.add(field);
	}
	
	/*
	public Terminal getLoadStar() {
		return this.load_star;
	}
	*/
	
	public Terminal getLoad(String field) {
		return this.load_(field);
	}
	
	/*
	public Terminal getStoreStar() {
		return this.store_star;
	}
	*/
	
	public Terminal getStore(String field) {
		return this.store_(field);
	}
	
	public Terminal getSource() {
		return this.source;
	}
	
	public Terminal getSink() {
		return this.sink;
	}
	
	public Terminal getPassThrough() {
		return this.passThrough;
	}
	
	public void addStubMethod(Set<Vertex> args, Vertex ret, String methodSignature) {
		/*
		// temporary variable in the method
		Vertex temp_method = new Vertex("temp_" + methodSignature);
		super.addEdge(temp_method, temp_method, this.load_star);
		super.addEdge(temp_method, temp_method, this.store_star);
		
		// new object in the method
		Vertex obj_method = new Vertex("obj_" + methodSignature);
		super.addEdge(obj_method, temp_method, this.new_terminal);
		
		// add edges to and from args
		for(Vertex arg : args) {
			super.addEdge(arg, temp_method, this.assign);
			super.addEdge(temp_method, arg, this.assign);
		}
		if(ret != null) {
			super.addEdge(temp_method, ret, this.assign);
		}
		*/
		
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
	
	public NormalCFL getFlowsToCfl2() {
		NormalCFL normalCfl = new NormalCFL();

		// flowsTo(o,a_c) <- new(o,a_c)
		normalCfl.add(this.flowsTo, this.new_terminal);

		// flowsTo(o,b_c) <- flowsTo(o,a_c) assign(a_c,b_c)
		normalCfl.add(this.flowsTo, this.flowsTo, this.assign);
 
		// flowsTo(o,b_c2) <- flowsTo(o,a_c1) assign(a_c1,b_c2)
		normalCfl.add(this.flowsTo, this.flowsTo, this.assign);

		for(String field : this.fields) {
			// flowsToField_f(o2,o1) <- flowsTo(o2,a_c) store_f(a_c,p_c) flowsToBar(p_c,o1)
			normalCfl.add(this.flowsToField_(field), this.flowsTo, this.store_(field), this.flowsToBar);

			// flowsTo(o2,a_c) <- flowsToField_f(o2,o1) flowsTo(o1,p_c) load_f(p_c,a_c)
			normalCfl.add(this.flowsTo, this.flowsToField_(field), this.flowsTo, this.load_(field));
		}
		
		// taints(src,o) -> source(src,v), flowsToBar(v,o)
		normalCfl.add(this.taints, this.source, this.flowsToBar);
		// taints(src,o2) -> taints(src,o1), flowsTo(o1,a), passThrough(a,b), flowsToBar(b,o2)
		normalCfl.add(this.taints, this.taints, this.flowsTo, this.passThrough, this.flowsToBar);
		// sourceSinkFlow(src,sink) -> taints(src,o), flowsTo(o,p), sink(p,sink)
		normalCfl.add(this.sourceSinkFlow, this.taints, this.flowsTo, this.sink);

		return normalCfl;
	}
	
	public NormalCFL getFlowsToCfl() {
		NormalCFL normalCfl = new NormalCFL();

		// add production flowsToRight -> assign
		//normalCfl.add(new SingleProduction(this.flowsToRight, this.assign));
		
		normalCfl.add(this.flowsTo, this.flowsTo, this.assign);
		
		// add production flowsToRight -> flowsToRight flowsToRight
		//normalCfl.add(new PairProduction(this.flowsToRight, this.flowsToRight, this.flowsToRight));
		
		// add production flowsToRight -> store_* alias load_*
		//normalCfl.add(this.flowsToRight, this.store_star, this.load_star);
		
		// add production flowsTo ->  new flowsToRight
		//normalCfl.add(this.flowsTo, this.new_terminal, this.flowsToRight);
		
		// add production flowsTo -> new
		normalCfl.add(this.flowsTo, this.new_terminal);

		// add production alias -> flowsToBar flowsTo
		normalCfl.add(this.alias, this.flowsToBar, this.flowsTo);
		
		for(String field : this.fields) {
			// add production flowsToRight -> store_f alias load_f
			//normalCfl.add(this.flowsToRight, this.store_(field), this.alias, this.load_(field));
			normalCfl.add(this.flowsTo, this.flowsTo, this.store_(field), this.alias, this.load_(field));
			
			// add production flowsToRight -> store_* alias load_f
			//normalCfl.add(this.flowsToRight, this.store_star, this.alias, this.store_star);
			
			// add production flowsToRight -> store_f alias load_*
			//normalCfl.add(this.flowsToRight, this.store_(field), this.alias, this.load_star);
		}
		
		// taints(src,o) -> source(src,v), flowsToBar(v,o)
		normalCfl.add(this.taints, this.source, this.flowsToBar);
		// taints(src,o2) -> taints(src,o1), flowsTo(o1,a), passThrough(a,b), flowsToBar(b,o2)
		normalCfl.add(this.taints, this.taints, this.flowsTo, this.passThrough, this.flowsToBar);
		// sourceSinkFlow(src,sink) -> taints(src,o), flowsTo(o,p), sink(p,sink)
		normalCfl.add(this.sourceSinkFlow, this.taints, this.flowsTo, this.sink);
		
		return normalCfl;
	}

	public Map<GraphElement,Path> getShortestPaths() {
		return super.getShortestPaths(this.getFlowsToCfl2());
	}
	
	public Set<GraphElement> getProductions() {
		return super.getProductions(this.getFlowsToCfl2());
	}
}

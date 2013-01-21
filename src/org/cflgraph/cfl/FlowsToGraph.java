package org.cflgraph.cfl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.Element.Variable;
import org.cflgraph.cfl.NormalCFL.PairProduction;
import org.cflgraph.cfl.NormalCFL.SingleProduction;

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
	
	private Variable label = new Variable("label");
	
	private Variable flowsTo = new Variable("flowsTo");
	private Variable flowsToBar = new Variable("flowsToBar");

	private Variable flowsToRight = new Variable("flowsToRight");
	
	private Variable alias = new Variable("alias");
	
	// variables for annotated flows
	private Variable sourceSinkFlow = new Variable("sourceSinkFlow");
	
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
	
	public NormalCFL getFlowsToCfl() {
		NormalCFL normalCfl = new NormalCFL();

		// add production flowsToRight -> assign
		normalCfl.add(new SingleProduction(this.flowsToRight, this.assign));
		
		// add production flowsToRight -> flowsToRight flowsToRight
		normalCfl.add(new PairProduction(this.flowsToRight, this.flowsToRight, this.flowsToRight));
		
		// add production flowsToRight -> store_* alias load_*
		//normalCfl.add(this.flowsToRight, this.store_star, this.load_star);
		
		// add production flowsTo ->  new flowsToRight
		normalCfl.add(this.flowsTo, this.new_terminal, this.flowsToRight);
		
		// add production flowsTo -> new
		normalCfl.add(this.flowsTo, this.new_terminal);

		// add production alias -> flowsToBar flowsTo
		normalCfl.add(this.alias, this.flowsToBar, this.flowsTo);
		
		for(String field : this.fields) {
			// add production flowsToRight -> store_f alias load_f
			normalCfl.add(this.flowsToRight, this.store_(field), this.alias, this.load_(field));
			
			// add production flowsToRight -> store_* alias load_f
			//normalCfl.add(this.flowsToRight, this.store_star, this.alias, this.store_star);
			
			// add production flowsToRight -> store_f alias load_*
			//normalCfl.add(this.flowsToRight, this.store_(field), this.alias, this.load_star);
		}
		
		// annotated flows
		// label(l,o2) -> label(l,o1), flowsTo(o1,p), passThrough(p,q), flowsToBar(q,o2)
		normalCfl.add(this.label, this.label, this.flowsTo, this.passThrough, this.flowsToBar);
		
		// label(l,o) -> source(l,v), flowsToBar(v,o)
		normalCfl.add(this.label, this.source, this.flowsToBar);
		
		// sourceSinkFlow(l1,l2) -> label(l1,o), flowsTo(o,v), sink(v,l2)
		normalCfl.add(this.sourceSinkFlow, this.label, this.flowsTo, this.sink);
		
		return normalCfl;
	}

	public Map<GraphElement,Path> getShortestPaths() {
		return super.getShortestPaths(this.getFlowsToCfl());
	}
}

package org.cflgraph.groupcfl;

public class TaintFlowCFL extends GroupCFL {
	public TaintFlowCFL() {		
		// Production 1: taints(src,o) -> source(src,v), flowsToBar(v,o)
		
		// groups
		super.elementsToGroups.add("flowsTo", "flowsTo");
		super.elementsToGroups.add("!flowsTo", "!flowsTo");
		super.elementsToGroups.add("source", "source");
		super.elementsToGroups.add("taints", "taints");
		
		// productions
		super.elementsToLeftCoInputGroups.add("!flowsTo", "source");
		super.elementsToRightCoInputGroups.add("source", "!flowsTo");
		
		// reductions
		super.reductions.put("source^!flowsTo", "taints");
		
		// Production 2: taints(src,o2) -> taints(src,o1), flowsTo(o1,a), passThrough(a,b), flowsToBar(b,o2)
		
		// groups
		super.elementsToGroups.add("passThrough", "passThrough");
		super.elementsToGroups.add("taints^flowsTo", "taints^flowsTo");
		super.elementsToGroups.add("taints^flowsTo", "taints^flowsTo");
		super.elementsToGroups.add("taints^flowsTo^passThrough", "taints^flowsTo^passThrough");
		
		// productions
		super.elementsToLeftCoInputGroups.add("flowsTo", "taints");
		super.elementsToRightCoInputGroups.add("taints", "flowsTo");
		
		super.elementsToLeftCoInputGroups.add("passThrough", "taints^flowsTo");
		super.elementsToRightCoInputGroups.add("taints^flowsTo", "passThrough");
		
		super.elementsToLeftCoInputGroups.add("!flowsTo", "taints^flowsTo^passThrough");
		super.elementsToRightCoInputGroups.add("taints^flowsTo^passThrough", "!flowsTo");
		
		super.reductions.put("taints^flowsTo^passThrough^!flowsTo", "taints");
		
		// inverses
		super.invertedElements.add("flowsTo");
		
		// Production 3: sourceSinkFlow(src,sink) -> taints(src,o), flowsTo(o,p), sink(p,sink)
		
		// groups
		super.elementsToGroups.add("sourceSinkFlow", "sourceSinkFlow");
		super.elementsToGroups.add("sink", "sink");
		
		// productions
		super.elementsToLeftCoInputGroups.add("flowsTo", "taints");
		super.elementsToRightCoInputGroups.add("taints", "flowsTo");
		
		super.elementsToLeftCoInputGroups.add("sink", "taints^flowsTo");
		super.elementsToRightCoInputGroups.add("taints^flowsTo", "sink");
		
		// reductions
		super.reductions.put("taints^flowsTo^sink", "sourceSinkFlow");
	}
}

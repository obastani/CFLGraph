package org.cflgraph.groupcfl;

import java.util.Set;

public class FlowsToCFL extends GroupCFL {
	public FlowsToCFL(Set<String> fields) {
		// Production 1: flowsTo(o,a_c) <- new(o,a_c)
		
		// productions
		super.unaryProductions.add("new", "flowsTo");
		
		// groups
		super.elementsToGroups.add("new", "new");
		super.elementsToGroups.add("flowsTo", "flowsTo");

		// Production 2: flowsTo(o,b_c) <- flowsTo(o,a_c) assign(a_c,b_c)
		
		// groups
		super.elementsToGroups.add("assign", "assign");
		
		// productions
		super.elementsToLeftCoInputGroups.add("assign", "flowsTo");
		super.elementsToRightCoInputGroups.add("flowsTo", "assign");

		// reductions
		super.reductions.put("flowsTo^assign", "flowsTo");		
		
		// Production 3: flowsTo(o2,a_c) <- flowsTo(o2,a_c) store_f(a_c,p_c) !flowsTo(p_c,o1) flowsTo(o1,p_c) load_f(p_c,a_c)
		
		// productions
		super.elementsToLeftCoInputGroups.add("!flowsTo", "store");
		super.elementsToRightCoInputGroups.add("flowsTo", "load");

		super.elementsToRightCoInputGroups.add("flowsTo", "store^!flowsTo^flowsTo^load");
		
		// groups
		super.elementsToGroups.add("!flowsTo", "!flowsTo");
		
		for(String field : fields) {
			// groups
			super.elementsToGroups.add("store_" + field, "store");
			super.elementsToGroups.add("store_" + field, "store_" + field);
			
			super.elementsToGroups.add("load_" + field, "load");
			super.elementsToGroups.add("load_" + field, "load_" + field);
			
			super.elementsToGroups.add("flowsTo^load_" + field, "flowsTo^load_" + field);
			super.elementsToGroups.add("store_" + field + "^!flowsTo", "store_" + field + "^!flowsTo");
			super.elementsToGroups.add("store_" + field + "^!flowsTo^flowsTo^load_" + field, "store_" + field + "^!flowsTo^flowsTo^load_" + field);
			super.elementsToGroups.add("store_" + field + "^!flowsTo^flowsTo^load_" + field, "store^!flowsTo^flowsTo^load");
			
			// productions
			super.elementsToRightCoInputGroups.add("store_" + field, "!flowsTo");
			super.elementsToLeftCoInputGroups.add("load_" + field, "flowsTo");
	
			super.elementsToLeftCoInputGroups.add("flowsTo^load_" + field, "store_" + field + "^!flowsTo");
			super.elementsToRightCoInputGroups.add("store_" + field + "^!flowsTo", "flowsTo^load_" + field);
	
			super.elementsToLeftCoInputGroups.add("store_" + field + "^!flowsTo^flowsTo^load_" + field, "flowsTo");
			
			// reductions
			super.reductions.put("flowsTo^store_" + field + "^!flowsTo^flowsTo^load_" + field, "flowsTo");
		}
		
		// inverted elements
		super.invertedElements.add("flowsTo");
	}
}

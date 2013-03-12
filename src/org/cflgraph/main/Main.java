package org.cflgraph.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cflgraph.groupcfl.FlowsToCFL;
import org.cflgraph.groupcfl.GroupCFL.Edge;
import org.cflgraph.groupcfl.TaintFlowCFL;
import org.cflgraph.utility.Utility.MultivalueMap;

public class Main {	
	public static Map<Edge,Integer> getInput(BufferedReader input) throws IOException {
		Map<Edge,Integer> edges = new HashMap<Edge,Integer>();
		Set<String> fields = new HashSet<String>();
		
		// stub method arguments
		MultivalueMap<String,String> methodArgs = new MultivalueMap<String,String>();
		Map<String,String> methodRet = new HashMap<String,String>();
		
		String line;
		while((line = input.readLine()) != null) {
			String[] tokens = line.split(" ");
			if(tokens.length == 3) {
				String source = tokens[0];
				String sink = tokens[1];

				String label = null;
				if(tokens[2].startsWith("new")) {
					label = "new";
				} else if(tokens[2].startsWith("load_")) {
					String field = tokens[2].substring(5);
					fields.add(field);
					label = "load_" + field;
				} else if(tokens[2].startsWith("store_")) {
					String field = tokens[2].substring(6);
					fields.add(field);
					label = "store_" + field;
				} else if(tokens[2].startsWith("assign")) {
					label = "assign";
				} else if(tokens[2].startsWith("srcFlow")) {
					label = "source";
				} else if(tokens[2].startsWith("sinkFlow")) {
					label = "sink";
				} else if(tokens[2].startsWith("passThrough")) {
					label = "passThrough";
				} else if(tokens[2].startsWith("stubArg")) {
					//graph.addMethod(tokens[1]);
					methodArgs.add(tokens[1], tokens[0]);
					label = null;
				} else if(tokens[2].startsWith("stubRet")) {
					methodRet.put(tokens[0], tokens[1]);
					label = null;
				}
				if(label != null) {
					edges.put(new Edge(source, sink, label), 0);
				}
			}
		}
		/*
		for(String methodName : methodArgs.keySet()) {
			graph.addStubMethod(methodArgs.get(methodName), methodRet.get(methodName), methodName);
		}
		*/
		
		FlowsToCFL flowsToCFL = new FlowsToCFL(fields);
		flowsToCFL.getClosure(edges);
		TaintFlowCFL taintFlowCFL = new TaintFlowCFL();
		taintFlowCFL.getClosure(edges);
		return edges;
	}

	public static void main(String[] args) {
		try {
			String input = "opengps";
			
			long time = System.currentTimeMillis();
			Map<Edge,Integer> edges = getInput(new BufferedReader(new FileReader("input/" + input + ".dat")));
			System.out.println("time: " + (System.currentTimeMillis() - time));
			
			PrintWriter pw = new PrintWriter("output/" + input + ".knuth");
			for(Edge edge : edges.keySet()) {
				if(edge.getElement().equals("sourceSinkFlow")) {
					pw.println(edge.toString());
				}
			}
			pw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

package org.cflgraph.main;

import gnu.trove.map.hash.TObjectByteHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.cflgraph.cfl.CFLGraph.Edge;
import org.cflgraph.cfl.FlowsToGraph;
import org.cflgraph.cfl.NormalCfl;
import org.cflgraph.cfl.TaintFlowGraph;
import org.cflgraph.utility.Utility.MultivalueMap;

public class Main {
	
	public static FlowsToGraph getInput(BufferedReader input) throws IOException {
		// graph
		FlowsToGraph graph = new FlowsToGraph();
		NormalCfl normalCfl = graph.getNormalCfl();
		
		// stub method arguments
		MultivalueMap<String,Integer> methodArgs = new MultivalueMap<String,Integer>();
		Map<String,Integer> methodRet = new HashMap<String,Integer>();
		
		String line;
		while((line = input.readLine()) != null) {
			String[] tokens = line.split(" ");
			if(tokens.length == 3) {
				int source = graph.vertices.getIdByElement(tokens[0]);
				int sink = graph.vertices.getIdByElement(tokens[1]);

				Integer label = null;
				if(tokens[2].startsWith("new")) {
					label = normalCfl.elements.getIdByElement("new");
				} else if(tokens[2].startsWith("load_")) {
					String field = tokens[2].substring(5);
					graph.addField(field);
					label = normalCfl.elements.getIdByElement("load_" + field);
				} else if(tokens[2].startsWith("store_")) {
					String field = tokens[2].substring(6);
					graph.addField(field);
					label = normalCfl.elements.getIdByElement("store_" + field);
				} else if(tokens[2].startsWith("assign")) {
					label = normalCfl.elements.getIdByElement("assign");
				} else if(tokens[2].startsWith("srcFlow")) {
					label = normalCfl.elements.getIdByElement("source");
				} else if(tokens[2].startsWith("sinkFlow")) {
					label = normalCfl.elements.getIdByElement("sink");
				} else if(tokens[2].startsWith("passThrough")) {
					label = normalCfl.elements.getIdByElement("passThrough");
				} else if(tokens[2].startsWith("stubArg")) {
					methodArgs.add(tokens[1], graph.vertices.getIdByElement(tokens[0]));
					label = null;
				} else if(tokens[2].startsWith("stubRet")) {
					methodRet.put(tokens[0], graph.vertices.getIdByElement(tokens[1]));
					label = null;
				}
				if(label != null) {
					graph.addEdge(source, sink, label);
				}
			}
		}
		for(String methodName : methodArgs.keySet()) {
			graph.addStubMethod(methodArgs.get(methodName), methodRet.get(methodName), methodName);
		}
		
		//System.out.println(graph);
		//System.out.println(graph.getNormalCfl());
		
		return graph;
	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		try {
			String input = "butane_cs";
			FlowsToGraph flowsToGraph = getInput(new BufferedReader(new FileReader("input/" + input + ".dat")));
			PrintWriter pw = new PrintWriter("output/" + input + ".knuth");

			/*
			TObjectByteHashMap<Edge> flowsTo = flowsToGraph.getClosure();
			for(Edge edge : flowsTo.keySet()) {
				if(flowsToGraph.getNormalCfl().elements.getElementById(edge.getElement()).equals("flowsTo")) {
					pw.println(edge + ", weight: " + flowsTo.get(edge));
					//System.out.println(edge.getPath(true));
					//System.out.println();
				}
			}
			pw.println();
			*/
			
			TaintFlowGraph taintFlowGraph = flowsToGraph.getTaintFlowGraph();
			TObjectByteHashMap<Edge> taintFlow = taintFlowGraph.getClosure();
			for(Edge edge : taintFlow.keySet()) {
				if(taintFlowGraph.getNormalCfl().elements.getElementById(edge.getElement()).equals("sourceSinkFlow")) {
					pw.println(edge + ", weight: " + taintFlow.get(edge));
					//System.out.println(edge.getPath(true));
					//System.out.println();
				}
			}
			pw.println();
			/*
			pw.println("Rule counts for pair production rules:");

			for(Map.Entry<PairProduction,Integer> entry : cflGraph.getPairProductionCounts().sortedKeySet()) {
				pw.println(entry.getKey() + " : " + entry.getValue());
			}
			pw.println();
			pw.println("Rule counts for single production rules:");
			for(Map.Entry<SingleProduction,Integer> entry : cflGraph.getSingleProductionCounts().sortedKeySet()) {
				pw.println(entry.getKey() + " : " + entry.getValue());
			}
			*/
			
			pw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}
}

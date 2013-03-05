package org.cflgraph.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.cflgraph.cfl.CFLGraph.Edge;
import org.cflgraph.cfl.CFLGraph.EdgeData;
import org.cflgraph.cfl.CFLGraph.Vertex;
import org.cflgraph.cfl.FlowsToGraph;
import org.cflgraph.cfl.NormalCfl.Element;
import org.cflgraph.cfl.TaintFlowGraph;
import org.cflgraph.utility.Utility.MultivalueMap;

public class Main {
	
	public static FlowsToGraph getInput(BufferedReader input) throws IOException {
		// graph
		FlowsToGraph graph = new FlowsToGraph();
		
		// stub method arguments
		MultivalueMap<String,Vertex> methodArgs = new MultivalueMap<String,Vertex>();
		Map<String,Vertex> methodRet = new HashMap<String,Vertex>();
		
		String line;
		while((line = input.readLine()) != null) {
			String[] tokens = line.split(" ");
			if(tokens.length == 3) {
				Vertex source = new Vertex(tokens[0]);
				Vertex sink = new Vertex(tokens[1]);

				Element label = null;
				if(tokens[2].startsWith("new")) {
					label = graph.getNew();
				} else if(tokens[2].startsWith("load_")) {
					String field = tokens[2].substring(5);
					graph.addField(field);
					label = graph.getLoad(field);
				} else if(tokens[2].startsWith("store_")) {
					String field = tokens[2].substring(6);
					graph.addField(field);
					label = graph.getStore(field);
				} else if(tokens[2].startsWith("assign")) {
					label = graph.getAssign();
				} else if(tokens[2].startsWith("srcFlow")) {
					label = graph.getSource();
				} else if(tokens[2].startsWith("sinkFlow")) {
					label = graph.getSink();
				} else if(tokens[2].startsWith("passThrough")) {
					label = graph.getPassThrough();
				} else if(tokens[2].startsWith("stubArg")) {
					//graph.addMethod(tokens[1]);
					methodArgs.add(tokens[1], new Vertex(tokens[0]));
					label = null;
				} else if(tokens[2].startsWith("stubRet")) {
					methodRet.put(tokens[0], new Vertex(tokens[1]));
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
		
		return graph;
	}
	
	public static void main(String[] args) {
		try {
			String input = "butane_cs";
			FlowsToGraph flowsToGraph = getInput(new BufferedReader(new FileReader("input/" + input + ".dat")));

			long time = System.currentTimeMillis();
			TaintFlowGraph taintFlowGraph = flowsToGraph.getTaintFlowGraph();
			Map<Edge,EdgeData> taintFlow = taintFlowGraph.getClosure();
			System.out.println("time: " + (System.currentTimeMillis() - time));
			
			PrintWriter pw = new PrintWriter("output/" + input + ".knuth");
			for(Map.Entry<Edge,EdgeData> entry : taintFlow.entrySet()) {
				if(entry.getKey().getElement().getName().equals("sourceSinkFlow")) {
					pw.println(entry.getKey() + ", weight: " + entry.getValue().getWeight());
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
	}
}

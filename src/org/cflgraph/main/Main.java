package org.cflgraph.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.cflgraph.cfl.CFLGraph.GraphElement;
import org.cflgraph.cfl.CFLGraph.Path;
import org.cflgraph.cfl.CFLGraph.Vertex;
import org.cflgraph.cfl.Element.Terminal;
import org.cflgraph.cfl.FlowsToGraph;
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

				Terminal label = null;
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
					methodRet.put(tokens[1], new Vertex(tokens[0]));
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
		
		PrintWriter pw = new PrintWriter("graph.txt");
		pw.println(graph);
		pw.close();
		
		return graph;
	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		try {
			//FlowsToGraph cflGraph = getInput(new BufferedReader(new FileReader("test.reps")));
			FlowsToGraph cflGraph = getInput(new BufferedReader(new FileReader("lesson1_flow.reps")));
			
			Map<GraphElement,Path> shortestPaths = cflGraph.getShortestPaths();
			for(Map.Entry<GraphElement,Path> entry : shortestPaths.entrySet()) {
				if(entry.getKey().getElement().getName().equals("sourceSinkFlow")) {
					System.out.println(entry.getKey() + ", weight: " + entry.getValue().getWeight());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("time: " + (System.currentTimeMillis() - time));
	}
}

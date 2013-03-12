package org.cflgraph.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;

import org.cflgraph.cfl.CFLGraph;
import org.cflgraph.cfl.NormalCFL;

public class IO {
	
	public static NormalCFL readCFL(BufferedReader br) throws IOException {
		NormalCFL cfl = new NormalCFL();
		
		String line;
		while((line = br.readLine()) != null) {
			String[] params = line.split("\\s");
			String[] inputs = Arrays.copyOfRange(params, 1, params.length);
			if(params.length > 1) {
				cfl.add(params[0], inputs);
			}
		}
		
		return cfl;
	}
	
	public static void writeCFL(NormalCFL cfl, PrintWriter pw) {
		pw.println(cfl.toString());
	}
	
	public static CFLGraph readCFLGraph(BufferedReader br, Set<String> inputs) throws IOException {
		CFLGraph cflGraph = new CFLGraph();
		
		String line;
		while((line = br.readLine()) != null) {
			String[] params = line.split("\\s");
			switch(params.length) {
			case 3:
				if(inputs == null || inputs.contains(params[2])) {
					cflGraph.addEdge(params[0], params[1], params[2]);
				}
				break;
			case 4:
				if(inputs == null || inputs.contains(params[2])) {
					cflGraph.addEdge(params[0], params[1], params[2], Integer.parseInt(params[3]));
				}
				break;
			}
		}
		
		return cflGraph;
	}
	
	public static CFLGraph readCFLGraph(BufferedReader br) throws IOException {
		return readCFLGraph(br, null);
	}
	
	public static void writeCFLGraph(CFLGraph cflGraph, PrintWriter pw, Set<String> outputs) {
		pw.println(cflGraph.toString(outputs));
	}
	
	public static void writeCFLGraph(CFLGraph cflGraph, PrintWriter pw) {
		pw.println(cflGraph.toString());
	}
}

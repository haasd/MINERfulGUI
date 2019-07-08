package minerful.gui.graph.util;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class GraphUtil {
	
	public static Graph drawGraph(ProcessModel processModel) {
		Graph graph = new MultiGraph("Tutorial 1");
		graph.setAttribute("ui.stylesheet", "url("+GraphUtil.class.getClassLoader().getResource("css/graph.css").toExternalForm()+")");
		
		for(TaskChar task : processModel.getTasks()) {
			Node node = graph.addNode(task.identifier.toString());
			node.setAttribute("ui.label", task.getName());
		}
		
		int id = 0;
		for(Constraint constraint : processModel.getAllConstraints()) {
			if(constraint.getImplied() == null) {
				Node node = graph.getNode(constraint.getBase().getJoinedStringOfIdentifiers());
				node.setAttribute("ui.class", constraint.type);
			} else {
				id++;
				Edge edge = graph.addEdge(String.valueOf(id), constraint.getBase().getJoinedStringOfIdentifiers(), constraint.getImplied().getJoinedStringOfIdentifiers());
				edge.setAttribute("ui.class", constraint.type);
			}
			
		}
		
		return graph;
	}

}

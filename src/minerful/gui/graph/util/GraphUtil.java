package minerful.gui.graph.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class GraphUtil {
	
	public static Graph drawGraph(ProcessModel processModel) {
		
		Map<String,Constraint> edges = new HashMap<>();
		
		Graph graph = new MultiGraph("MINERful");
		//SpriteManager sm = new SpriteManager(graph);
		graph.setAttribute("ui.stylesheet", "url("+GraphUtil.class.getClassLoader().getResource("css/graph.css").toExternalForm()+")");
		
		for(TaskChar task : processModel.getTasks()) {
			Node node = graph.addNode(task.identifier.toString());
			node.setAttribute("ui.label", task.getName());
		}
		
		for(Constraint constraint : processModel.getAllConstraints()) {
			if(constraint.getImplied() == null) {
				Node node = graph.getNode(constraint.getBase().getJoinedStringOfIdentifiers());
				node.setAttribute("ui.class", constraint.type);
			} else {

				String forwardEdge = constraint.getBase().getJoinedStringOfIdentifiers()+constraint.getImplied().getJoinedStringOfIdentifiers();
				String backwardEdge = constraint.getImplied().getJoinedStringOfIdentifiers() +constraint.getBase().getJoinedStringOfIdentifiers();
				
				if(!edges.containsKey(forwardEdge) && !edges.containsKey(backwardEdge)) {
					
					// if no edge between nodes exists add one and also add constraint as attribute
					Edge edge = graph.addEdge(forwardEdge, constraint.getBase().getJoinedStringOfIdentifiers(), constraint.getImplied().getJoinedStringOfIdentifiers());
					edge.setAttribute("ui.class", constraint.type);
					List<Constraint> constraints = new ArrayList<>();
					constraints.add(constraint);
					edge.setAttribute("constraints", constraints);
				} else {
					
					// if edge already exists only add constraint as attribute
					if(graph.getEdge(forwardEdge) != null) {
						Edge edge = graph.getEdge(forwardEdge);
						edge.setAttribute(constraint.toString(), constraint);
					} else if (graph.getEdge(backwardEdge) != null) {
						Edge edge = graph.getEdge(backwardEdge);
						edge.setAttribute(constraint.toString(), constraint);
						System.out.println(edge.getAttribute(constraint.toString()).toString());
					}
				}
				
				edges.put(constraint.getBase().getJoinedStringOfIdentifiers()+constraint.getImplied().getJoinedStringOfIdentifiers(),constraint);
			}
		}
		
		return graph;
	}

	public static String showInfo(Constraint constraint) {
		return String.format("%s \nSupport: %4.3f \nConfidence: %4.3f \nInterestFactor: %4.3f",constraint.type, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor());
	}
	
}

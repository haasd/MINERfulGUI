package minerful.gui.graph.util;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class GraphUtil {
	
	public static Graph drawGraph(ProcessModel processModel) {
		Graph graph = new MultiGraph("MINERful");
		SpriteManager sm = new SpriteManager(graph);
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
//				Sprite a = sm.addSprite(String.valueOf(id));
//				a.setPosition(0.5);
				Edge edge = graph.addEdge(String.valueOf(id), constraint.getBase().getJoinedStringOfIdentifiers(), constraint.getImplied().getJoinedStringOfIdentifiers());
				edge.setAttribute("ui.class", constraint.type);
//				a.attachToEdge(String.valueOf(id));
//				a.setAttribute("ui.label",showInfo(constraint));
			}
		}
		
		return graph;
	}

	public static String showInfo(Constraint constraint) {
		return String.format("%s \nSupport: %4.3f \nConfidence: %4.3f \nInterestFactor: %4.3f",constraint.type, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor());
	}
	
}

package minerful.gui.graph.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.ValidationEngine;

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
		
		for(Constraint constraint : processModel.getAllUnmarkedConstraints()) {
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
	
	public static TableView<Constraint> getConstraintsForActivity(ProcessModel processModel, String activity, Boolean outgoing){
		TableView<Constraint> constraints = new TableView<>();
		constraints.setStyle("-fx-border-width: 1 0 1 0; -fx-border-color: #A5A39C;"); 
		constraints.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn<Constraint, String> constraintColumn = new TableColumn<>("Constraint");
		TableColumn<Constraint, Double> supportColumn= new TableColumn<>("Support");
		TableColumn<Constraint, Double> confidenceColumn= new TableColumn<>("Confidence");
		TableColumn<Constraint, Double> interestColumn= new TableColumn<>("Interest");
		
		constraints.getColumns().add(constraintColumn);
		constraints.getColumns().add(supportColumn);
		constraints.getColumns().add(confidenceColumn);
		constraints.getColumns().add(interestColumn);
		
		constraintColumn.getStyleClass().add("column-left");
		supportColumn.setMaxWidth(75);
		supportColumn.setMinWidth(75);
		confidenceColumn.setMaxWidth(75);
		confidenceColumn.setMinWidth(75);
		interestColumn.setMaxWidth(75);
		interestColumn.setMinWidth(75);
		constraintColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().toString()));
		
		supportColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("support"));
		supportColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
		
		confidenceColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("confidence"));
		confidenceColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
		
		interestColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("interestFactor"));
		interestColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
		
		ObservableList<Constraint> discoveredConstraints = FXCollections.observableArrayList();
		List<Constraint> conList = new ArrayList<>();
		
		if(outgoing) {
			constraints.setPlaceholder(new Label("No outgoing constraints!"));
			conList = processModel.getAllUnmarkedConstraints().stream().filter(con -> (con.getBase().getJoinedStringOfIdentifiers().equals(activity))).collect(Collectors.toList());
		} else {
			constraints.setPlaceholder(new Label("No incoming constraints!"));
			conList = processModel.getAllUnmarkedConstraints().stream().filter(con -> ((con.getImplied() != null) && con.getImplied().getJoinedStringOfIdentifiers().equals(activity))).collect(Collectors.toList());
		}
		
		for(Constraint con : conList) {
			discoveredConstraints.add(con);
		}
		
		constraints.setItems(discoveredConstraints);
		
		return constraints;
	}
	
}

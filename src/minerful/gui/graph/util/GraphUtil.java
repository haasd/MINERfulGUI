package minerful.gui.graph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

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
import minerful.gui.common.RelationConstraintInfo;
import minerful.gui.common.ValidationEngine;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.Card;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.StructuringElement;

public class GraphUtil {
	
	public static Graph drawGraph(ProcessModel processModel) {
		
		Graph graph = new MultiGraph("MINERful");
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
				String forwardEdge = constraint.getBase().getJoinedStringOfIdentifiers()+constraint.getImplied().getJoinedStringOfIdentifiers()+"_"+constraint.toString();
				Edge edge = graph.addEdge(forwardEdge, constraint.getBase().getJoinedStringOfIdentifiers(), constraint.getImplied().getJoinedStringOfIdentifiers());
				edge.setAttribute("ui.class", constraint.type);
				
				Integer width = (int) Math.round(constraint.getSupport()*10);
				Integer opacity = (int) Math.round(constraint.getConfidence()*255); 
				Integer shadow = (int) Math.round(constraint.getInterestFactor()*10);
				
				edge.setAttribute("layout.weight", 5.0);
				
				edge.setAttribute("ui.style", String.format("size: %s; fill-color: rgba(131,173,213, %d); shadow-mode: gradient-radial; shadow-color: #EEF, #000; shadow-offset: 0px; shadow-width: %s;", width, opacity, shadow));
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
	
	public static List<RelationConstraintInfo> determineConstraints(List<ActivityElement> activityElements) {
		List<RelationConstraintInfo> constraintElements = new ArrayList<>();
		
		for(ActivityElement sourceElement : activityElements) {
			
			if(sourceElement.getExistenceConstraint() != null) {
				StructuringElement struct = sourceElement.getExistenceConstraint().getStruct();
				if(struct != null) {
					if(struct == StructuringElement.END) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "End"));
					} else if (struct == StructuringElement.INIT) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Init"));
					} else if (struct == StructuringElement.INITEND) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Init"));
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "End"));
					}
				}
			
				Card card = sourceElement.getExistenceConstraint().getCard();
				
				if(card != null) {
					if("0".equals(card.getMin()) && "1".equals(card.getMax())) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "AtMostOne"));
					} else {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Participation"));
					}
				}
			}
			for(RelationConstraintElement constraint : sourceElement.getConstraintList()) {
				for(ActivityElement targetElement : constraint.getParameter2Elements()) {
					
					if(targetElement != sourceElement) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), targetElement.getIdentifier(), constraint.getTemplate().getName()));
					}
				}
			}
		}
		
		return constraintElements;
	}
	
}

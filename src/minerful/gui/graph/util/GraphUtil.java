package minerful.gui.graph.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.springframework.util.FastByteArrayOutputStream;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.RelationConstraintInfo;
import minerful.gui.common.ValidationEngine;
import minerful.gui.controller.ModelGeneratorTabController;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.Card;
import minerful.gui.model.EventHandlerManager;
import minerful.gui.model.ExistenceConstraintEnum;
import minerful.gui.model.LineNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintEnum;
import minerful.gui.model.RelationConstraintNode;
import minerful.gui.model.StructuringElement;
import minerful.gui.model.Template;
import minerful.gui.model.xml.XMLExistenceConstraint;
import minerful.gui.util.Config;

public class GraphUtil {
	
	private static Config config = new Config("config");
	
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
	
	public static void drawProcessModel(ProcessElement processElement, EventHandlerManager eventHandler, AnchorPane anchorPane) {
		for(ActivityElement aElement : processElement.getActivityEList()) {
			anchorPane.getChildren().add(aElement.getNode());
			eventHandler.setEventHandler(aElement.getNode());
		}
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			anchorPane.getChildren().add(rcElement.getConstraintNode());
			eventHandler.setEventHandler(rcElement.getConstraintNode());
		}
	}
	
	
	/*
	 * Create ProcessElement based on a ProcessModel 
	 */
	public static ProcessElement transformProcessModelIntoProcessElement(ProcessModel processModel, AnchorPane pane, EventHandlerManager eventHandler) {
		ProcessElement processElement = new ProcessElement();
		
		determineActivityElements(processElement, processModel.getTasks(), pane, eventHandler);
		determineConstraintElements(processElement, processModel.getAllUnmarkedConstraints(), pane, eventHandler);
		
		return processElement;
	}
	
	//generate a copy of a ProcessElement
	public static ProcessElement cloneProcessElement(ProcessElement processElement) {
		ProcessElement newProcessElement = new ProcessElement();
		
		 try {
	            // Write the object out to a byte array
	            FastByteArrayOutputStream fbos = 
	                    new FastByteArrayOutputStream();
	            ObjectOutputStream out = new ObjectOutputStream(fbos);
	            out.writeObject(processElement);
	            out.flush();
	            out.close();

	            // Retrieve an input stream from the byte array and read
	            // a copy of the object back in. 
	            ObjectInputStream in = 
	                new ObjectInputStream(fbos.getInputStream());
	            newProcessElement = (ProcessElement) in.readObject();
	        }
	        catch(IOException e) {
	            e.printStackTrace();
	        }
	        catch(ClassNotFoundException cnfe) {
	            cnfe.printStackTrace();
	        }
		
		return newProcessElement;
	}
	
	private static void determineActivityElements(ProcessElement processElement, Set<TaskChar> activities, AnchorPane pane, EventHandlerManager eventHandler) {
		
		Integer id = 0;
		double x = 100d;
		double y = 100d;
		for(TaskChar taskChar : activities) {
			id++;
			ActivityElement activityElement = new ActivityElement(id, taskChar.getName(), taskChar.identifier.toString());
			activityElement.setPosition(x, y);
			processElement.addActivity(activityElement);

			Random random = new Random(); 
			
			x += (random.nextDouble() * 800d ) - 400d;
			y += (random.nextDouble() * 800d ) - 400d;
			
			if(x < 0) {
				x+= 400d;
			}
			
			if(y < 0) {
				y+= 400d;
			}
			
			//create Node and add it to Pane
			ActivityNode aNode = new ActivityNode(activityElement, null);
			pane.getChildren().add(aNode);
			
			eventHandler.setEventHandler(aNode);
			
		}
	}
	
	/*
	 * Transform ProcessModel constraints into ExistenceConstraints and RelationConstraints
	 */
	private static void determineConstraintElements(ProcessElement processElement, SortedSet<Constraint> constraints, AnchorPane pane, EventHandlerManager eventHandler) {
	
		for(Constraint constraint : constraints) {
			
			// Determine Constraints Label
			String conTemplateLabel = constraint.getTemplateName();
			
			if(constraint.getImplied() == null) {
				// handle ExistenceConstraints
				String taskId = constraint.getBase().getJoinedStringOfIdentifiers();
				
				ActivityElement activityElement = processElement.getActivityEList().stream().filter(a -> taskId.equals(a.getTaskCharIdentifier())).findFirst().orElse(null);
				
				if(activityElement != null) {

					if(conTemplateLabel.equals(ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel()) || conTemplateLabel.equals(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel())) {
						
						Card card;
						if(conTemplateLabel.equals(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel())) {
							 card = new Card("0", "1");
						} else {
							 card = new Card("0", "*");
						}
						
						if(activityElement.getExistenceConstraint() != null) {
							activityElement.getExistenceConstraint().setCard(card);
						} else {
							activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), card, null));
						}
						
					} else if (conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel()) || conTemplateLabel.equals(ExistenceConstraintEnum.END.getTemplateLabel())) {
						
						// If INIT or END was already set replace it with INITEND
						if(activityElement.getExistenceConstraint() != null) {
							if(activityElement.getExistenceConstraint().getStruct() == StructuringElement.END || activityElement.getExistenceConstraint().getStruct() == StructuringElement.INIT) {
								activityElement.getExistenceConstraint().setStruct(StructuringElement.INITEND);
							} else {
								activityElement.getExistenceConstraint().setStruct(conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel()) ? StructuringElement.INIT : StructuringElement.END);
							}
						} else {
							activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), null,conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel()) ? StructuringElement.INIT : StructuringElement.END));
						}
					} 
					
				}
			} else {
				// handle RelationConstraints
				Template template = RelationConstraintEnum.findTemplateByTemplateLabel(conTemplateLabel);
				Integer maxConstraintID = processElement.getMaxConstraintId();

				RelationConstraintElement cElement = new RelationConstraintElement(maxConstraintID, template);
				
				String taskId1 = constraint.getBase().getJoinedStringOfIdentifiers();
				String taskId2 = constraint.getImplied().getJoinedStringOfIdentifiers();
				
				ActivityElement aElement1 = processElement.getActivityEList().stream().filter(a -> taskId1.equals(a.getTaskCharIdentifier())).findFirst().orElse(null);
				ActivityElement aElement2 = processElement.getActivityEList().stream().filter(a -> taskId2.equals(a.getTaskCharIdentifier())).findFirst().orElse(null);
				
			
				//Create new Position Element
				double contraintRadius = config.getDouble("constraint.radius");
				double activityRadius = config.getDouble("activity.radius");
				double posX = (aElement1.getPosX() + aElement2.getPosX()) / 2 + activityRadius - contraintRadius; 
				double posY = (aElement1.getPosY() + aElement2.getPosY()) / 2 + activityRadius - contraintRadius;
				cElement.setPosition(posX, posY);
				
				// add Constraint to Process
				processElement.addRelationConstraint(cElement);
				
				RelationConstraintNode cNode = createConstraintNode(cElement);
				addAdditionalActivity(aElement1, cNode, 1, pane);
				addAdditionalActivity(aElement2, cNode, 2, pane);

				int amountOfLinesOnPane = 1; // start with 1 because of backgroundPane
				for(ActivityElement aElem : processElement.getActivityEList()){
					amountOfLinesOnPane += aElem.getConstraintList().size();
				}
				
				pane.getChildren().add(amountOfLinesOnPane,cNode);	
				eventHandler.setEventHandler(cNode);
				
			}
		}
	}
	
	/**
	 * creates a Node of an already existing Constraint in the currentProcessElement
	 * @param c is a constraintElement that encapsulates information and positions of the constraint
	 * @return
	 * @throws PersistenceException
	 */
	private static RelationConstraintNode createConstraintNode(RelationConstraintElement cElement){
		RelationConstraintNode cNode = new RelationConstraintNode(cElement,null);
		ArrayList<ActivityElement> parameter1List = cElement.getParameter1Elements();
		ArrayList<ActivityElement> parameter2List = cElement.getParameter2Elements();
		
		if (parameter1List == null || parameter2List == null){
			//TODO Dialog WARNING
			System.out.println("Inconsistent Files: Constraint connects not existing Activity.");
		}
		
		ArrayList<ActivityElement> tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter1List);
		for (ActivityElement aElem : tempList){
			cElement.createAndSetLineNode(aElem,1);
		}
		
		tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter2List);
		for (ActivityElement aElem : tempList){
			cElement.createAndSetLineNode(aElem,2);
		}
		return cNode;
	}
	
	/**
	 * adds an Activity to the Activation Side of a constraint
	 * @param aNode defines the ActivityNode
	 * @param cNode defines the ConstraintNode, if NULL the selectedElement has to be a ConstraintNode and will be connected
	 */
	public static void addAdditionalActivity(ActivityElement aNode, RelationConstraintNode cNode, int parameterNumber, AnchorPane pane){
		RelationConstraintElement cElement;
		cElement = cNode.getConstraintElement();
		
		LineNode newLine = cElement.addActivityElement(aNode, parameterNumber);
		//int position = currentProcessElement.getActivityEList().size() + 1;		// line has to be added after Activities
		pane.getChildren().add(1,newLine);		// position 0 is BackgroundPane, but has to be behind other Nodes
	}
	
}

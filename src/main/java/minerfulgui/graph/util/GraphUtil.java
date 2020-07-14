package minerfulgui.graph.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.util.FastByteArrayOutputStream;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.io.encdec.DeclareConstraintTransferObject;
import minerful.io.encdec.TransferObjectToConstraintTranslator;
import minerful.io.encdec.pojo.ConstraintPojo;
import minerfulgui.common.RelationConstraintInfo;
import minerfulgui.common.ValidationEngine;
import minerfulgui.controller.ModelGeneratorTabController;
import minerfulgui.model.ActivityElement;
import minerfulgui.model.ActivityNode;
import minerfulgui.model.Card;
import minerfulgui.model.CardinalityElement;
import minerfulgui.model.ConstraintElement;
import minerfulgui.model.EventHandlerManager;
import minerfulgui.model.ExistenceConstraintEnum;
import minerfulgui.model.LineNode;
import minerfulgui.model.ProcessElement;
import minerfulgui.model.RelationConstraintElement;
import minerfulgui.model.RelationConstraintEnum;
import minerfulgui.model.RelationConstraintNode;
import minerfulgui.model.StructureElement;
import minerfulgui.model.Template;
import minerfulgui.model.ActivityNode.Cursor;
import minerfulgui.model.xml.XMLExistenceConstraint;
import minerfulgui.service.FruchtermanReingoldAlgorithm;
import minerfulgui.service.LayoutAlgorithm;
import minerfulgui.service.ProcessElementInterface;
import minerfulgui.util.Config;

public class GraphUtil {
	
	private static Config config = new Config("config");
	private static Logger logger = Logger.getLogger(GraphUtil.class);
	
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
	
	public static List<RelationConstraintInfo> determineConstraints(ProcessElement processElement) {
		List<RelationConstraintInfo> constraintElements = new ArrayList<>();
		
		for(ActivityElement sourceElement : processElement.getActivityEList()) {
			
			if(sourceElement.getExistenceConstraint() != null) {
				
				boolean init = sourceElement.getExistenceConstraint().getInitConstraint() != null ? sourceElement.getExistenceConstraint().getInitConstraint().isActive() : false;
				boolean end = sourceElement.getExistenceConstraint().getEndConstraint() != null ? sourceElement.getExistenceConstraint().getEndConstraint().isActive() : false;
				
				if(end && !init) {
					constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "End"));
				} else if (!end && init) {
					constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Init"));
				} else if (end && init) {
					constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Init"));
					constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "End"));
				}
			
				Card card = sourceElement.getExistenceConstraint().getCard();
				
				if(card != null) {
					if("1".equals(card.getMax())) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "AtMostOne"));
					} 
					
					if("1".equals(card.getMin())) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Participation"));
					}
				}
			}
		}
		
		for(RelationConstraintElement constraint : processElement.getConstraintEList()) {
			
			for(ActivityElement aElement1: constraint.getParameter1Elements()) {
				for(ActivityElement aElement2 : constraint.getParameter2Elements()) {
					constraintElements.add(new RelationConstraintInfo(aElement1.getIdentifier(), aElement2.getIdentifier(), constraint.getTemplate().getName()));
				}
			}
		}
		
		return constraintElements;
	}
	
	public static void drawProcessModel(ProcessElement processElement, EventHandlerManager eventHandler, AnchorPane anchorPane, ModelGeneratorTabController controller) {
		for(ActivityElement aElement : processElement.getActivityEList()) {
			
			ActivityNode aNode = new ActivityNode(aElement, controller);
			
			controller.getActivityNodes().add(aNode);
			
			anchorPane.getChildren().add(aNode);
			eventHandler.setEventHandler(aNode);
		}
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			
			RelationConstraintNode rcNode = createConstraintNode(rcElement,controller);
			controller.getConstraintNodes().add(rcNode);
			
			anchorPane.getChildren().add(rcNode);
			
			
			eventHandler.setEventHandler(rcNode);
		}
	}
	
	/*
	 * Create ProcessModel based on a ProcessElement
	 */
	public static ProcessModel transformProcessElementIntoProcessModel(ProcessElement processElement) {
		TaskCharFactory tChFactory = new TaskCharFactory();
		List<TaskChar> taskChars = new ArrayList<>();
		
		Map<ActivityElement, TaskChar> aElementMap = new HashMap<>();
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			TaskChar taskChar = tChFactory.makeTaskChar(aElement.getIdentifier());
			taskChars.add(taskChar);
			aElementMap.put(aElement, taskChar);
		}
		
		// Create the tasks archive to store the "process alphabet"
		TaskCharArchive taChaAr = new TaskCharArchive(taskChars);
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			
			if(aElement.getExistenceConstraint() != null) {
				// transform position constraints
				if(aElement.getExistenceConstraint().getInitConstraint() != null && aElement.getExistenceConstraint().getInitConstraint().isActive()) {
					Init constraint = new Init(aElementMap.get(aElement), aElement.getExistenceConstraint().getInitConstraint().getSupport());
					constraint.setConfidence(aElement.getExistenceConstraint().getInitConstraint().getConfidence());
					constraint.setInterestFactor(aElement.getExistenceConstraint().getInitConstraint().getInterest());
					bag.add(constraint);
				}
				
				if(aElement.getExistenceConstraint().getEndConstraint() != null && aElement.getExistenceConstraint().getEndConstraint().isActive()) {
					End constraint = new End(aElementMap.get(aElement), aElement.getExistenceConstraint().getEndConstraint().getSupport());
					constraint.setConfidence(aElement.getExistenceConstraint().getEndConstraint().getConfidence());
					constraint.setInterestFactor(aElement.getExistenceConstraint().getEndConstraint().getInterest());
					bag.add(constraint);
				}
				
				// Transform card. constraints 
				if(aElement.getExistenceConstraint().getCard() != null) {
					Card card = aElement.getExistenceConstraint().getCard();
					
					if("1".equals(card.getMax().getBorder())) {
						AtMostOne constraint = new AtMostOne(aElementMap.get(aElement), card.getMax().getSupport());
						constraint.setConfidence(card.getMax().getConfidence());
						constraint.setInterestFactor(card.getMax().getInterest());
						bag.add(constraint);
					} 
					
					if("1".equals(card.getMin().getBorder())) {
						Participation constraint = new Participation(aElementMap.get(aElement), card.getMin().getSupport());
						constraint.setConfidence(card.getMin().getConfidence());
						constraint.setInterestFactor(card.getMin().getInterest());
						bag.add(constraint);
					}
					
				}
			}
			
			// transform relation constraints
			for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
				bag.add(mapTemplateToRelationConstraint(rcElement, taChaAr));
			}
		}
		
		return new ProcessModel(bag);
	}
	
	private static Constraint mapTemplateToRelationConstraint(RelationConstraintElement rcElement, TaskCharArchive taChaAr) {
		
		List<Set<String>> params = new ArrayList<>();
		Set<String> base = new HashSet<>();
		Set<String> implied = new HashSet<>();
		
		for(ActivityElement aElement1: rcElement.getParameter1Elements()) {
			base.add(aElement1.getIdentifier());
		}
		
		for(ActivityElement aElement2 : rcElement.getParameter2Elements()) {
			implied.add(aElement2.getIdentifier());
		}
		
		params.add(base);
		params.add(implied);
		
		ConstraintPojo pojo = new ConstraintPojo();
		pojo.template = rcElement.getTemplate().getName();
		pojo.support = rcElement.getSupport();
		pojo.confidence = rcElement.getConfidence();
		pojo.interestFactor = rcElement.getInterest();
		pojo.parameters = params;
		
		TransferObjectToConstraintTranslator totct = new TransferObjectToConstraintTranslator(taChaAr);
		
		Constraint constraint = totct.createConstraint(new DeclareConstraintTransferObject(pojo));
		
		return constraint;

	}

	/*
	 * Create ProcessElement based on a ProcessModel 
	 */
	public static ProcessElement transformProcessModelIntoProcessElement(ProcessModel processModel, AnchorPane pane, EventHandlerManager eventHandler, ProcessElementInterface controller, boolean reminingRequired, boolean relocatingRequired) {
		ProcessElement processElement = new ProcessElement();
		
		determineActivityElements(processElement, processModel.getTasks(), pane, eventHandler, controller, reminingRequired);
		determineConstraintElements(processElement, processModel.getAllUnmarkedConstraints(), pane, eventHandler, controller);
		
		if(reminingRequired || relocatingRequired) {
			LayoutAlgorithm testAlgorithm = new FruchtermanReingoldAlgorithm(1500,1500, processElement, controller, 1000);
			testAlgorithm.optimizeLayout();
		}
		
		determineLocation(controller,processElement);
		
		displayWholeProcessMap(controller, processElement);
		
		return processElement;
	}
	
	private static void displayWholeProcessMap(ProcessElementInterface controller, ProcessElement processElement) {
		
		double maxX = 0.0;
		double maxY = 0.0;
		
		for(ActivityElement node : processElement.getActivityEList()) {
			if(Math.abs(node.getPosY()) > maxY) {
				maxY = node.getPosY();
			}
			
			if(Math.abs(node.getPosX()) > maxX) {
				maxX = node.getPosX();
			}
			
		}
		
		double height = Math.max(controller.getScrollPane().getHeight(), 400d);
		Double maxYabs = Math.abs(maxY);
		
		if(maxYabs > height) {
			controller.getScrollPane().setScaleValue(height/(maxYabs+200d));
			controller.getScrollPane().updateScale();
		}
		
	}

	public static void determineLocation(ProcessElementInterface controller, ProcessElement processElement) {
		
		int constraintCounter = 0;
		List<RelationConstraintElement> rcElements = new ArrayList<>();
		double x = 800d;
		double y = 50d;
		int counter = 0;
		
		for(ActivityElement node : processElement.getActivityEList()) {
			ActivityNode aNode = controller.determineActivityNode(node);
			
			for(RelationConstraintElement rcElement : aNode.getActivityElement().getConstraintList()) {
				
				constraintCounter = determineConstraintCounter(rcElement, rcElements);
	        	controller.determineRelationConstraintNode(rcElement).moveConstraintBetweenActivities(constraintCounter);
	        	rcElements.add(rcElement);
				
			}

			// relocated all activities without relation constraints
			if(aNode.getActivityElement().getConstraintList().size() == 0) {
				aNode.getActivityElement().setPosition(x, y);
				counter++;
				x += 150d;
				
				if(counter == 6) {
					counter=0;
					y += 150d;
					x = 800d;
				}
				
				aNode.updateNode();
			}
			aNode.updateAllLineNodePositions();
			rcElements = new ArrayList<>();
		}
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
	
	private static void determineActivityElements(ProcessElement processElement, Set<TaskChar> activities, AnchorPane pane, EventHandlerManager eventHandler, ProcessElementInterface controller, boolean reminingRequired) {
		
		Integer id = 0;
		double x = 50d;
		double y = 50d;
		for(TaskChar taskChar : activities) {
			id++;
			ActivityElement activityElement = new ActivityElement(id, taskChar.getName(), taskChar.identifier.toString());
			activityElement.setPosition(x, y);
			processElement.addActivity(activityElement);
			

			Random random = new Random();
			
			if(reminingRequired) {
				x = (random.nextDouble() * 800d ) - 400d;
				y = (random.nextDouble() * 800d ) - 400d;
				
				if(x < 0) {
					x+= 400d;
				}
				
				if(y < 0) {
					y+= 400d;
				}
			} else {
				for(ActivityElement aElement : controller.getCurrentProcessElement().getActivityEList()) {
					if(taskChar.getName().equals(aElement.getIdentifier())) {
						activityElement.setPosition(aElement.getPosX(), aElement.getPosY());
					}
				}
			}
			
			//create Node and add it to Pane
			ActivityNode aNode = new ActivityNode(activityElement, controller);
			pane.getChildren().add(aNode);
			controller.getActivityNodes().add(aNode);
			
			eventHandler.setEventHandler(aNode);
			
		}
	}
	
	/*
	 * Transform ProcessModel constraints into ExistenceConstraints and RelationConstraints
	 */
	private static void determineConstraintElements(ProcessElement processElement, SortedSet<Constraint> constraints, AnchorPane pane, EventHandlerManager eventHandler,ProcessElementInterface controller) {
	
		for(Constraint constraint : constraints) {
			
			// Determine Constraints Label
			String conTemplateLabel = constraint.getTemplateName();
			
			if(constraint.getImplied() == null) {
				// handle ExistenceConstraints
				String taskId = constraint.getBase().getJoinedStringOfIdentifiers();
				
				ActivityElement activityElement = processElement.getActivityEList().stream().filter(a -> taskId.equals(a.getTaskCharIdentifier())).findFirst().orElse(null);
				
				if(activityElement != null) {

					if(conTemplateLabel.equals(ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel()) || conTemplateLabel.equals(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel())) {
						
						if(activityElement.getExistenceConstraint() == null ) {
							activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), new Card(), new StructureElement(false), new StructureElement(false)));
						}
						
						CardinalityElement cardElement = new CardinalityElement("1", constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor());
						if(conTemplateLabel.equals(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel())) {
							activityElement.getExistenceConstraint().getCard().setMax(cardElement);
						} else {
							activityElement.getExistenceConstraint().getCard().setMin(cardElement);
						}
						
					} else if (conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel()) || conTemplateLabel.equals(ExistenceConstraintEnum.END.getTemplateLabel())) {
						
						// If INIT or END was already set replace it with INITEND
						if(activityElement.getExistenceConstraint() != null) {
							if(conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel())) {
								activityElement.getExistenceConstraint().setInitConstraint(new StructureElement(true, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor()));
							} else {
								activityElement.getExistenceConstraint().setEndConstraint(new StructureElement(true, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor()));
							}
						} else {
							if(conTemplateLabel.equals(ExistenceConstraintEnum.INIT.getTemplateLabel())) {			
								activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), null,new StructureElement(true, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor()),new StructureElement(false)));
							} else {
								activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), null,new StructureElement(false),new StructureElement(true, constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor())));
							}
						}
					} 
					controller.determineActivityNode(activityElement).updateNode();
					
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
				cElement.setSupport(constraint.getSupport());
				cElement.setConfidence(constraint.getConfidence());
				cElement.setInterest(constraint.getInterestFactor());
				cElement.setPositionFixed(true);
				
				// add Constraint to Process
				processElement.addRelationConstraint(cElement);
				
				controller.determineActivityNode(aElement1);
				
				RelationConstraintNode cNode = createConstraintNode(cElement , controller);
				addAdditionalActivity(controller.determineActivityNode(aElement1), cNode, 1, pane, processElement , constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor());
				addAdditionalActivity(controller.determineActivityNode(aElement2), cNode, 2, pane, processElement , constraint.getSupport(), constraint.getConfidence(), constraint.getInterestFactor());

				int amountOfLinesOnPane = 1; // start with 1 because of backgroundPane
				for(ActivityElement aElem : processElement.getActivityEList()){
					amountOfLinesOnPane += aElem.getConstraintList().size();
				}
				
				pane.getChildren().add(amountOfLinesOnPane,cNode);
				
				cNode.changeConstraintType();
				
				eventHandler.setEventHandler(cNode);
				
			}
		}
	}
	
	private static int determineConstraintCounter(RelationConstraintElement rcElement, List<RelationConstraintElement> rcElements) {
		int counter = 0;
		
		ActivityElement source = rcElement.getParameter1Elements().get(0);
		ActivityElement target = rcElement.getParameter2Elements().get(0);
		
		for(RelationConstraintElement rcElem : rcElements) {
			if((rcElem.getParameter1Elements().get(0) == source && rcElem.getParameter2Elements().get(0) == target) 
					|| (rcElem.getParameter1Elements().get(0) == target && rcElem.getParameter2Elements().get(0) == source)) {
				counter++;
			}
		}
		
		return counter; 
	}
	
	/**
	 * creates a Node of an already existing Constraint in the currentProcessElement
	 * @param c is a constraintElement that encapsulates information and positions of the constraint
	 * @return
	 * @throws PersistenceException
	 */
	private static RelationConstraintNode createConstraintNode(RelationConstraintElement cElement, ProcessElementInterface controller){
		RelationConstraintNode cNode = new RelationConstraintNode(cElement,controller);
		controller.getConstraintNodes().add(cNode);
		ArrayList<ActivityElement> parameter1List = cElement.getParameter1Elements();
		ArrayList<ActivityElement> parameter2List = cElement.getParameter2Elements();
		
		if (parameter1List == null || parameter2List == null){
			logger.error("Inconsistent Files: Constraint connects not existing Activity.");
		}
		
		ArrayList<ActivityElement> tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter1List);
		for (ActivityElement aElem : tempList){
			controller.determineRelationConstraintNode(cElement).createAndSetLineNode(controller.determineActivityNode(aElem),1);
		}
		
		tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter2List);
		for (ActivityElement aElem : tempList){
			controller.determineRelationConstraintNode(cElement).createAndSetLineNode(controller.determineActivityNode(aElem),2);
		}
		return cNode;
	}
	
	/**
	 * adds an Activity to the Activation Side of a constraint
	 * @param aNode defines the ActivityNode
	 * @param cNode defines the ConstraintNode, if NULL the selectedElement has to be a ConstraintNode and will be connected
	 */
	public static void addAdditionalActivity(ActivityNode activityNode, RelationConstraintNode cNode, int parameterNumber,AnchorPane pane, ProcessElement processElement, double support, double confidence, double interest){
		RelationConstraintElement cElement;
		cElement = cNode.getConstraintElement();
		
		cElement.addActivityElement(activityNode.getActivityElement(), parameterNumber, support, confidence, interest);
		LineNode newLine = cNode.createAndSetLineNode(activityNode, parameterNumber);
		
		pane.getChildren().add(1,newLine);		// position 0 is BackgroundPane, but has to be behind other Nodes
	}
	
	public static void hideConstraints(List<ActivityNode> aNodes, List<RelationConstraintNode> rcNodes, Boolean positive) {
		for(RelationConstraintNode rcNode : rcNodes) {
			if(RelationConstraintEnum.isPositiveConstraint(rcNode.getConstraintElement().getTemplate().getName()) == positive) {
				rcNode.getStyleClass().add("hide");
				
				for(ActivityNode aNode : aNodes) {
					Cursor cursor = aNode.getCursorByConstraint(rcNode);
					if(cursor !=null) {
						if(cursor.getInsideCursor() != null) {
							cursor.getInsideCursor().getStyleClass().add("hide");
						}
						
						if(cursor.getOutsideCursor() != null) {
							cursor.getOutsideCursor().getStyleClass().add("hide");
						}
					}
				}
				
				for(LineNode lNode : rcNode.getParameter1Lines()) {
					lNode.getStyleClass().add("hide");
				}
				
				for(LineNode lNode : rcNode.getParameter2Lines()) {
					lNode.getStyleClass().add("hide");
				}
			}
		}
	}
	
	public static void displayConstraints(List<ActivityNode> aNodes, List<RelationConstraintNode> rcNodes, Boolean positive) {
		for(RelationConstraintNode rcNode : rcNodes) {
			if(RelationConstraintEnum.isPositiveConstraint(rcNode.getConstraintElement().getTemplate().getName()) == positive) {
				rcNode.getStyleClass().remove("hide");
				
				for(ActivityNode aNode : aNodes) {
					Cursor cursor = aNode.getCursorByConstraint(rcNode);
					if(cursor !=null) {
						if(cursor.getInsideCursor() != null) {
							cursor.getInsideCursor().getStyleClass().remove("hide");
						}
						
						if(cursor.getOutsideCursor() != null) {
							cursor.getOutsideCursor().getStyleClass().remove("hide");
						}
					}
				}
				
				for(LineNode lNode : rcNode.getParameter1Lines()) {
					lNode.getStyleClass().remove("hide");
					
				}
				
				for(LineNode lNode : rcNode.getParameter2Lines()) {
					lNode.getStyleClass().remove("hide");
				}
			}
		}
	}
	
	public static double determineOpacity(double support, double confidence, double interest) {
		
		return support * 0.85 + confidence * 0.10 + interest * 0.05;
		
	}
	
	/**
	 * determines the stroke width of relationConstraintElement based on parameters
	 * 
	 * @param support
	 * @param confidence
	 * @param interest
	 * @return strokeWidth as String
	 */
	public static String determineStrokeWidth(double support, double confidence, double interest) {
		
		double width = Math.max((3 * (support * 0.85 + confidence * 0.10 + interest * 0.05)), 0.2d);
		
		return String.valueOf(width).replace(",", ".");
	}
	
	/**
	 * determines shapeOpacity based on rgb-values and parameters
	 * 
	 * @param attribute
	 * @param node
	 * @param red
	 * @param green
	 * @param blue
	 * @param constraintElement
	 */
	public static void setShapeOpacity(String attribute, Node node, int red, int green, int blue, ConstraintElement constraintElement) {
		
		double opacity = determineOpacity(constraintElement.getSupport(), constraintElement.getConfidence(),constraintElement.getInterest());
		String opacityString = String.valueOf(opacity).replace(",", ".");
		node.setStyle(String.format("%s: rgba(%d, %d, %d, %s);",attribute, red, green, blue, opacityString));
		if("-fx-stroke".equals(attribute) ) {
			String width = determineStrokeWidth(constraintElement.getSupport(), constraintElement.getConfidence(),constraintElement.getInterest());
			
			node.setStyle(node.getStyle().concat(String.format(" -fx-stroke-width: %s;", width)));
		}
	}
	
	/**
	 * determines shapeOpacity and also allows fade (required if opacity is influenced by two constraints)
	 * 
	 * @param node
	 * @param red
	 * @param green
	 * @param blue
	 * @param constraintElement1
	 * @param constraintElement2
	 */
	public static void setShapeWithFadeOpacity(Node node, int red, int green, int blue, ConstraintElement constraintElement1, ConstraintElement constraintElement2) {
		
		double opacity = determineOpacity(constraintElement1.getSupport(), constraintElement1.getConfidence(),constraintElement1.getInterest());
		String opacityString = String.valueOf(opacity).replace(",", ".");
		String color1 = String.format("rgba(%d, %d, %d, %s)", red, green, blue, opacityString);
		opacity = determineOpacity(constraintElement2.getSupport(), constraintElement2.getConfidence(),constraintElement2.getInterest());
		opacityString = String.valueOf(opacity).replace(",", ".");
		String color2 = String.format("rgba(%d, %d, %d, %s)", red, green, blue, opacityString);
		node.setStyle(String.format("-fx-fill: linear-gradient(from 25%% 25%% to 100%% 100%% ,%s, %s);", color1, color2, opacity));
	}
	
	/**
	 * used to enable and disable styling of graph based on parameters
	 * 
	 * @param aNodes
	 * @param rcNodes
	 * @param active
	 */
	public static void setParameterStyling(List<ActivityNode> aNodes, List<RelationConstraintNode> rcNodes, Boolean active) {
		if(active) {
			for(RelationConstraintNode rcNode : rcNodes) {
				for(LineNode lNode : rcNode.getParameter1Lines()) {
					setShapeOpacity("-fx-stroke", lNode.getLine(), 61, 136, 195, rcNode.getConstraintElement());
				}
				
				for(LineNode lNode : rcNode.getParameter2Lines()) {
					setShapeOpacity("-fx-stroke", lNode.getLine(), 61, 136, 195, rcNode.getConstraintElement());
				}
			}
			
			for(ActivityNode aNode : aNodes) {
				if(aNode.getActivityElement().getExistenceConstraint() != null) {
					if(aNode.getActivityElement().getExistenceConstraint().getCard() != null) {
						setShapeWithFadeOpacity(aNode.getCardinalityShape(), 255, 255, 255, aNode.getActivityElement().getExistenceConstraint().getCard().getMin(), aNode.getActivityElement().getExistenceConstraint().getCard().getMax());
					}
					setShapeOpacity("-fx-fill", aNode.getInitShape(), 255, 255, 255, aNode.getActivityElement().getExistenceConstraint().getInitConstraint());
					setShapeOpacity("-fx-fill", aNode.getEndShape(), 255, 255, 255, aNode.getActivityElement().getExistenceConstraint().getEndConstraint());
				}
			}
		} else {
			for(RelationConstraintNode rcNode : rcNodes) {
				for(LineNode lNode : rcNode.getParameter1Lines()) {
					lNode.getLine().setStyle("");
				}
				
				for(LineNode lNode : rcNode.getParameter2Lines()) {
					lNode.getLine().setStyle("");
				}
			}
			
			for(ActivityNode aNode : aNodes) {
				if(aNode.getActivityElement().getExistenceConstraint() != null) {
					aNode.getCardinalityShape().setStyle("");
					aNode.getInitShape().setStyle("");
					aNode.getEndShape().setStyle("");
				}
			}
		}
	}
}

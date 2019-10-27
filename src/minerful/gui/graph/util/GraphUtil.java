package minerful.gui.graph.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.gui.common.RelationConstraintInfo;
import minerful.gui.common.ValidationEngine;
import minerful.gui.controller.ModelGeneratorTabController;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.ActivityNode.Cursor;
import minerful.gui.model.Card;
import minerful.gui.model.EventHandlerManager;
import minerful.gui.model.ExistenceConstraintEnum;
import minerful.gui.model.LineElement;
import minerful.gui.model.LineNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintEnum;
import minerful.gui.model.RelationConstraintNode;
import minerful.gui.model.StructureElement;
import minerful.gui.model.Template;
import minerful.gui.model.xml.XMLExistenceConstraint;
import minerful.gui.service.FruchtermanReingoldAlgorithm;
import minerful.gui.service.LayoutAlgorithm;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.util.Config;

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
	
	public static List<RelationConstraintInfo> determineConstraints(List<ActivityElement> activityElements) {
		List<RelationConstraintInfo> constraintElements = new ArrayList<>();
		List<LineElement> alreadyAddedLineElements = new ArrayList<>();
		
		for(ActivityElement sourceElement : activityElements) {
			
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
					if("0".equals(card.getMin()) && "1".equals(card.getMax())) {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "AtMostOne"));
					} else {
						constraintElements.add(new RelationConstraintInfo(sourceElement.getIdentifier(), null, "Participation"));
					}
				}
			}
			
			for(RelationConstraintElement constraint : sourceElement.getConstraintList()) {
				
				for(LineElement lineElement: constraint.getLineElements()) {
					
					if(!alreadyAddedLineElements.contains(lineElement)) {
						alreadyAddedLineElements.add(lineElement);
						constraintElements.add(new RelationConstraintInfo(lineElement.getSourceElement().getIdentifier(), lineElement.getTargetElement().getIdentifier(), constraint.getTemplate().getName()));
					}
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
		
		List<LineElement> alreadyAddedLineElements = new ArrayList<>();
		
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
					
					if("0".equals(card.getMin()) && "1".equals(card.getMin())) {
						AtMostOne constraint = new AtMostOne(aElementMap.get(aElement), card.getSupport());
						constraint.setConfidence(card.getConfidence());
						constraint.setInterestFactor(card.getInterest());
						bag.add(constraint);
					} else {
						Participation constraint = new Participation(aElementMap.get(aElement), card.getSupport());
						constraint.setConfidence(card.getConfidence());
						constraint.setInterestFactor(card.getInterest());
						bag.add(constraint);
					}
				}
			}
			
			// transform relation constraints
			for(RelationConstraintElement rcElement : aElement.getConstraintList()) {
				for(LineElement lineElement : rcElement.getLineElements()) {
					if(!alreadyAddedLineElements.contains(lineElement)) {
						alreadyAddedLineElements.add(lineElement);
						bag.add(mapTemplateToRelationConstraint(aElementMap, rcElement.getTemplate().getName(), lineElement));
					}
				}
			}
			
			
		}
		
		return new ProcessModel(bag);
	}
	
	private static Constraint mapTemplateToRelationConstraint(Map<ActivityElement, TaskChar> aElementMap, String templateName, LineElement lineElement) {
		
		TaskChar source = aElementMap.get(lineElement.getSourceElement()); 
		TaskChar target = aElementMap.get(lineElement.getTargetElement());

		
		switch(templateName) {
			// RelationConstraints
			case "respondedExistence" :
				RespondedExistence rExist = new RespondedExistence(source, target, lineElement.getSupport());
				rExist.setConfidence(lineElement.getConfidence());
				rExist.setInterestFactor(lineElement.getInterest());
				return rExist;
			case "response":
				Response response = new Response(source, target, lineElement.getSupport());
				response.setConfidence(lineElement.getConfidence());
				response.setInterestFactor(lineElement.getInterest());
				return response;
			case "alternateResponse":
				AlternateResponse alternateResponse = new AlternateResponse(source, target, lineElement.getSupport());
				alternateResponse.setConfidence(lineElement.getConfidence());
				alternateResponse.setInterestFactor(lineElement.getInterest());
				return alternateResponse;
			case "chainResponse":
				ChainResponse chainResponse = new ChainResponse(source, target, lineElement.getSupport());
				chainResponse.setConfidence(lineElement.getConfidence());
				chainResponse.setInterestFactor(lineElement.getInterest());
				return chainResponse;
			case "precedence":
				Precedence precendence = new Precedence(source, target, lineElement.getSupport());
				precendence.setConfidence(lineElement.getConfidence());
				precendence.setInterestFactor(lineElement.getInterest());
				return precendence;
			case "alternatePrecedence":
				AlternatePrecedence altPrec = new AlternatePrecedence(source, target, lineElement.getSupport());
				altPrec.setConfidence(lineElement.getConfidence());
				altPrec.setInterestFactor(lineElement.getInterest());
				return altPrec;
			case "chainPrecedence":
				ChainPrecedence chainPre = new ChainPrecedence(source, target, lineElement.getSupport());
				chainPre.setConfidence(lineElement.getConfidence());
				chainPre.setInterestFactor(lineElement.getInterest());
				return chainPre;
			case "coExistence":
				CoExistence coExist = new CoExistence(source, target, lineElement.getSupport());
				coExist.setConfidence(lineElement.getConfidence());
				coExist.setInterestFactor(lineElement.getInterest());
				return coExist;
			case "succession":
				Succession succession = new Succession(source, target, lineElement.getSupport());
				succession.setConfidence(lineElement.getConfidence());
				succession.setInterestFactor(lineElement.getInterest());
				return succession;
			case "alternateSuccession":
				AlternateSuccession alternateSuccession = new AlternateSuccession(source, target, lineElement.getSupport());
				alternateSuccession.setConfidence(lineElement.getConfidence());
				alternateSuccession.setInterestFactor(lineElement.getInterest());
				return alternateSuccession;
			case "chainSuccession":
				ChainSuccession chainSuccession = new ChainSuccession(source, target, lineElement.getSupport());
				chainSuccession.setConfidence(lineElement.getConfidence());
				chainSuccession.setInterestFactor(lineElement.getInterest());
				return chainSuccession;
			case "notChainSuccession":
				NotChainSuccession notChainSuccession = new NotChainSuccession(source, target, lineElement.getSupport());
				notChainSuccession.setConfidence(lineElement.getConfidence());
				notChainSuccession.setInterestFactor(lineElement.getInterest());
				return notChainSuccession;
			case "notSuccession":
				NotSuccession notSuccession = new NotSuccession(source, target, lineElement.getSupport());
				notSuccession.setConfidence(lineElement.getConfidence());
				notSuccession.setInterestFactor(lineElement.getInterest());
				return notSuccession;
			case "notCoExistence":
				NotCoExistence notCoExistence = new NotCoExistence(source, target, lineElement.getSupport());
				notCoExistence.setConfidence(lineElement.getConfidence());
				notCoExistence.setInterestFactor(lineElement.getInterest());
				return notCoExistence;
		}
		
		return null;
	}

	/*
	 * Create ProcessElement based on a ProcessModel 
	 */
	public static ProcessElement transformProcessModelIntoProcessElement(ProcessModel processModel, AnchorPane pane, EventHandlerManager eventHandler, ProcessElementInterface controller) {
		ProcessElement processElement = new ProcessElement();
		
		determineActivityElements(processElement, processModel.getTasks(), pane, eventHandler, controller);
		determineConstraintElements(processElement, processModel.getAllUnmarkedConstraints(), pane, eventHandler, controller);
		
		LayoutAlgorithm testAlgorithm = new FruchtermanReingoldAlgorithm(1000, 500, processElement, controller, 1000);
		
		testAlgorithm.optimizeLayout();
		
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
	
	private static void determineActivityElements(ProcessElement processElement, Set<TaskChar> activities, AnchorPane pane, EventHandlerManager eventHandler, ProcessElementInterface controller) {
		
		Integer id = 0;
		double x = 50d;
		double y = 50d;
		for(TaskChar taskChar : activities) {
			id++;
			ActivityElement activityElement = new ActivityElement(id, taskChar.getName(), taskChar.identifier.toString());
			activityElement.setPosition(x, y);
			processElement.addActivity(activityElement);

			Random random = new Random(); 
			
			x = (random.nextDouble() * 800d ) - 400d;
			y = (random.nextDouble() * 800d ) - 400d;
			
			if(x < 0) {
				x+= 400d;
			}
			
			if(y < 0) {
				y+= 400d;
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
						
						Card card;
						if(conTemplateLabel.equals(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel())) {
							 card = new Card("0", "1");
						} else {
							 card = new Card("0", "*");
						}
						
						card.setSupport(constraint.getSupport());
						card.setInterest(constraint.getInterestFactor());
						card.setConfidence(constraint.getConfidence());
						
						if(activityElement.getExistenceConstraint() != null) {
							activityElement.getExistenceConstraint().setCard(card);
						} else {
							activityElement.setExistenceConstraint(new XMLExistenceConstraint(activityElement.getId(), card, new StructureElement(false), new StructureElement(false)));
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
}

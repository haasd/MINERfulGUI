package minerful.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.ConstraintsBag;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.RelationConstraintInfo;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.AddConstraintMode;
import minerful.gui.model.EditActivityPane;
import minerful.gui.model.EditConstraintPane;
import minerful.gui.model.EventHandlerManager;
import minerful.gui.model.LineNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintNode;
import minerful.gui.model.Selectable;
import minerful.gui.model.Template;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.util.Config;

public class ModelGeneratorTabController extends AbstractController implements Initializable, ProcessElementInterface {
	
	Logger logger = Logger.getLogger(ModelGeneratorTabController.class);
	
	@FXML
	ScrollPane scrollPane;
	
	@FXML
	AnchorPane anchorPane;
	
	@FXML
	BorderPane backgroundPane;
	
	@FXML
	VBox sidePane;
	
	@FXML
	ListView<ActivityElement> activitiesList;
	
	@FXML
	TableView<RelationConstraintInfo> constraintsTable;
	
	@FXML
	TableColumn<RelationConstraintInfo, String> sourceColumn;
	
	@FXML
	TableColumn<RelationConstraintInfo, String> targetColumn;
	
	@FXML
	TableColumn<RelationConstraintInfo, String> templateColumn;
	
	private ModelInfo modelInfo;
	
	private ConstraintsBag bag = new ConstraintsBag();
	
	private ProcessModel processModel = new ProcessModel(bag);
	
	private TaskCharFactory tChFactory = new TaskCharFactory();
	
	private ObservableList<ActivityElement> activityElements = FXCollections.observableArrayList();
	
	private ObservableList<RelationConstraintInfo> constraintElements = FXCollections.observableArrayList();
	
	private Config config = new Config("config");
	
	private ProcessElement currentProcessElement = new ProcessElement();	// holds all information of currently active process
	private Selectable selectedElement;				// 

	private Accordion editTabPane = new Accordion();
	
	private EditActivityPane editActivityPane = new EditActivityPane(this);
	private EditConstraintPane editConstraintPane = new EditConstraintPane(this);
	private TitledPane activityPane = new TitledPane("Activity", editActivityPane );
	private TitledPane constraintPane = new TitledPane("Constraint", editConstraintPane);
	

	//private RelationSide side;
	private AddConstraintMode addConstraintMode;
	private double scrollPanePadding = 250d;
	private DoubleProperty maxTranslateX = new SimpleDoubleProperty(scrollPanePadding);
	private DoubleProperty maxTranslateY = new SimpleDoubleProperty(scrollPanePadding);
	
	private ActivityNode oldNode = null; // saves the Node that is about to be removed from Constraint

	private EventHandlerManager eventManager = new EventHandlerManager(this);

	private Button addActivityB = new Button("Add Activity");
	
	private String highlightedClass = "highlightedActivity";
	private String activityClass = "activity";
	
	// ProcessNode workaround
	private List<ActivityNode> activityNodes = new ArrayList<>();
	private List<RelationConstraintNode> constraintNodes = new ArrayList<>();
	
	private Boolean onLoad = false;
	
	private TaskCharFactory taChar = new TaskCharFactory();
	private TaskCharArchive taCharAr = new TaskCharArchive();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		FontIcon fontIcon = new FontIcon("fa-plus");
		fontIcon.setIconSize(25);
		fontIcon.setIconColor(Paint.valueOf("white"));
		
		addActivityB.setGraphic(fontIcon);
		addActivityB.setGraphicTextGap(20);
		addActivityB.setOnAction((event) -> {
			addActivityClicked();
		});
		
		editActivityPane.setDisable(false);
		editConstraintPane.setDisable(false);
		activityPane.setDisable(true);
		constraintPane.setDisable(true);
		
		
		addActivityB.getStyleClass().add("btn-default");
		addActivityB.getStyleClass().add("btn-important-with-graphic");
		VBox.setMargin(addActivityB, new Insets(20));
		
		sourceColumn.setCellValueFactory(
                new PropertyValueFactory<RelationConstraintInfo, String>("activitySource"));
		
		targetColumn.setCellValueFactory(
                new PropertyValueFactory<RelationConstraintInfo, String>("activityTarget"));
		
		templateColumn.setCellValueFactory(
                new PropertyValueFactory<RelationConstraintInfo, String>("constraintTemplate"));
		
		constraintsTable.setItems(constraintElements);
		
		activityElements = FXCollections.observableArrayList(
		    new Callback<ActivityElement, Observable[]>() {
		        @Override
		        public Observable[] call(ActivityElement activityElement) {
		            return new Observable[] { activityElement.getIdentifierProperty()} ;
		        }
		});
		
		activitiesList.setCellFactory(new Callback<ListView<ActivityElement>, ListCell<ActivityElement>>() {

		    @Override
		    public ListCell<ActivityElement> call(ListView<ActivityElement> list) {
		        ListCell<ActivityElement> cell = new ListCell<ActivityElement>() {
		            @Override
		            public void updateItem(ActivityElement item, boolean empty) {
		                super.updateItem(item, empty);
		                // also sets to graphic to null when the cell becomes empty
		                if (item == null) {
		                    setText(null);
		                } else {
		                    // assume MyDataType.getSomeProperty() returns a string
		                    setText(item.getIdentifier());
		                }
		            }
		        };

		        return cell;
		    }
		});
		
		activitiesList.setItems(activityElements);

		eventManager.setEventHandler(backgroundPane);

		// add Listeners for Background
		maxTranslateX.addListener((obs,oldValue,newValue) -> {
			backgroundPane.setPrefWidth(newValue.doubleValue());
		});
		maxTranslateY.addListener((obs,oldValue,newValue) -> {
			backgroundPane.setPrefHeight(newValue.doubleValue());
		});
		
		//Draw process
		createAndAddNodesOfProcessElement();
		sendMouseReleaseEvent(backgroundPane);

		
		editTabPane.getPanes().addAll(activityPane,constraintPane);
		editTabPane.getStyleClass().add("editTabPane");

		sidePane.getChildren().addAll(addActivityB,editTabPane);
		
	}
	
	@FXML
	private void exportFile() {
		MinerfulGuiUtil.exportFile(this, getMainController().getSavedProcessModels());
	}
	
	@FXML
	public void saveModel(ActionEvent event) {
		logger.info("Save Model");
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Save Model");
		dialog.setHeaderText("Save Model as");
		dialog.setContentText("Modelname:");
		dialog.getDialogPane().setMinWidth(500.0);
		
		ModelInfo modelInfo = new ModelInfo();
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			modelInfo.setSaveName(result.get());
		} else {
			MinerfulGuiUtil.displayAlert("Error Saving", "Error Saving", "No Name was provided!", AlertType.ERROR);
			return;
		}
		
		
		// TODO: Write Transform of ProcessElement into ProcessModel
		 
		modelInfo.setProcessModel(GraphUtil.transformProcessElementIntoProcessModel(currentProcessElement));
		modelInfo.setSaveDate(new Date());
		modelInfo.setProcessElement(GraphUtil.cloneProcessElement(currentProcessElement));
		
		getMainController().addSavedProcessModels(modelInfo);

	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	
	public TaskCharFactory gettChFactory() {
		return tChFactory;
	}

	public void settChFactory(TaskCharFactory tChFactory) {
		this.tChFactory = tChFactory;
	}
	
	public void loadGraph() {
		onLoad = true;
		currentProcessElement = modelInfo.getProcessElement();
		
		createAndAddNodesOfProcessElement();
	}
	
	/**
	 * creates a Node of an already existing Constraint in the currentProcessElement
	 * @param c is a constraintElement that encapsulates information and positions of the constraint
	 * @return
	 * @throws PersistenceException
	 */
	private RelationConstraintNode createConstraintNode(RelationConstraintElement cElement){
		RelationConstraintNode cNode = new RelationConstraintNode(cElement,this);
		ArrayList<ActivityElement> parameter1List = cElement.getParameter1Elements();
		ArrayList<ActivityElement> parameter2List = cElement.getParameter2Elements();
		
		if (parameter1List == null || parameter2List == null){
			//TODO Dialog WARNING
			System.out.println("Inconsistent Files: Constraint connects not existing Activity.");
		}
		
		ArrayList<ActivityElement> tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter1List);
		for (ActivityElement aElem : tempList){
			cNode.createAndSetLineNode(determineActivityNode(aElem),1);
		}
		
		tempList = new ArrayList<ActivityElement>();
		tempList.addAll(parameter2List);
		for (ActivityElement aElem : tempList){
			cNode.createAndSetLineNode(determineActivityNode(aElem),2);
		}
		
		constraintNodes.add(cNode);
		return cNode;
	}
	
	
	/**
	 * Draws the process saved in currentProcess
	 * Elements of the process are the backgroundPane, ActivityNodes and ConstraintNodes
	 * ActivityNodes and ConstraintNodes are created based on the current information of
	 * the currentProcessElement
	 */
	private void createAndAddNodesOfProcessElement(){
		
		for (ActivityElement a: currentProcessElement.getActivityEList()){
			ActivityNode aNode = new ActivityNode(a, this);
			activityNodes.add(aNode);
			anchorPane.getChildren().add(aNode);
			eventManager.setEventHandler(aNode);
			this.determineActivityNode(a).updateNode();
		}
		
		this.activityElements.clear();		
		this.activityElements.addAll(currentProcessElement.getActivityEList());
		
		onLoad=false;
		
		//create RelationConstraintNodes
		ArrayList<RelationConstraintNode> relationConstraintNodes = new ArrayList<RelationConstraintNode>();
		ArrayList<LineNode> lineNodes = new ArrayList<LineNode>();
		for (RelationConstraintElement c : currentProcessElement.getConstraintEList()){
			RelationConstraintNode rcNode = new RelationConstraintNode(c, this);
			constraintNodes.add(rcNode);
			relationConstraintNodes.add(rcNode);
			for (ActivityElement a : c.getParameter1Elements()){
				lineNodes.add(rcNode.createAndSetLineNode(determineActivityNode(a),1));
			}
			for (ActivityElement a : c.getParameter2Elements()){
				lineNodes.add(rcNode.createAndSetLineNode(determineActivityNode(a),2));
			}
			eventManager.setEventHandler(rcNode);
			rcNode.changeConstraintType();
		}
		
		anchorPane.getChildren().addAll(1, relationConstraintNodes);
		anchorPane.getChildren().addAll(1, lineNodes);
	
		
		// initiate IDCounter on Process
		currentProcessElement.setMaxActivityID();
		currentProcessElement.setMaxConstraintID();
		
		// Adjust BackgroundPane
		setMaxTranslate();
		
	}
	
	
	/**
	 * creates a new Activity, ActivityElement and ActivityNode
	 * Activity and ActivityElement are added to the process and ProcessElement
	 * ActivityPosition will be created and saved.
	 * ActivityNode will be positioned in top left corner or 200px right next to the currently selected Node
	 */
	@FXML
	public ActivityElement addActivityClicked(){
		double SpacingToSelectedElement = 200d;
		// create Activity and ActivityElement
		Integer id = currentProcessElement.getMaxActivityID();
		String taskName = "new Activity " + id;
		
		for(ActivityElement aElement : currentProcessElement.getActivityEList()) {
			TaskChar taskChar = taChar.makeTaskChar(aElement.getIdentifier());
			aElement.setTaskCharIdentifier(taskChar.identifier.toString());
		}
		
		TaskChar taskChar = taChar.makeTaskChar(taskName);
		
		ActivityElement aElement = new ActivityElement(id, taskName, taskChar.identifier.toString());
		
		// add Activity to process model
		currentProcessElement.addActivity(aElement);
		
		activityElements.add(aElement);
		
		//create Node and add it to Pane
		ActivityNode aNode = new ActivityNode(aElement, this);
		anchorPane.getChildren().add(aNode);
		activityNodes.add(aNode);
		
		//move new Activity next to selected Activity if one is selected
		
		if(selectedElement != null){
			aNode.setTranslateX(selectedElement.getPosX() + SpacingToSelectedElement);
			aNode.setTranslateY(selectedElement.getPosY());
			aNode.updateActivityElement();
		}
		eventManager.setEventHandler(aNode);
		
		//Adjust BackgroundPane if newly added Activity is most right element
		setMaxTranslate();
		
		//select newly created ActivityNode by clicking on it
		sendMouseReleaseEvent(aNode);
		
		return aElement;
		
	}

	
	/**
	 * deletes the currently selectedElemnt from currentProcessElement and from processTab
	 * if activity has constraints, they are also deleted after confirmation
	 * otherwise activity is deleted without dialog confirmation.
	 * should only be called if selectedElement is guaranteed to be an ActivityNode
	 * @param aNode
	 */
	public void deleteActivity(ActivityNode aNode){
		Optional<ButtonType> result = MinerfulGuiUtil.displayAlert("Activity Deletion Confirmation", "Deleting all adjacent constraints", "By deleting this Activity, all adjacent constraints are also removed. Are you ok with this?", AlertType.CONFIRMATION);

		if(!aNode.getActivityElement().getConstraintList().isEmpty()){
			if (result.get() == ButtonType.OK){
				//delete all connected constraints
				ArrayList<RelationConstraintElement> constraintL =  new ArrayList<RelationConstraintElement>();		//local copy to avoid ConcurrentdModificationException
				for (RelationConstraintElement c: aNode.getActivityElement().getConstraintList()){
					constraintL.add(c);
				}
				for(RelationConstraintElement c : constraintL){
					deleteRelationConstraint(determineRelationConstraintNode(c));
				}
				anchorPane.getChildren().remove(aNode);
				currentProcessElement.deleteActivity(aNode.getActivityElement());
				activityElements.remove(aNode.getActivityElement());
			} else {
			    // do nothing
			}
		} else {
			anchorPane.getChildren().remove(aNode);
			currentProcessElement.deleteActivity(aNode.getActivityElement());
			activityElements.remove(aNode.getActivityElement());
		}
		sendMouseReleaseEvent(backgroundPane);
	}
	
	/**
	 * Creates a new relationConstraint based on 2 ActivityNodes
	 * Modifies the following lists: 
	 * 			activityElement.getRelationConstraintList (both Nodes)
	 * 			RelationConstraintElement.getLineNodes()
	 * 			RelationConstraintElement.getActivities
	 * @param aElem1
	 * @param aElem2
	 */
	public RelationConstraintElement addNewRelationConstraint(ActivityElement aElem1, ActivityElement aElem2){
		Template template = currentProcessElement.getTemplateList().get(0);
		//XMLRelationConstraint c = new XMLRelationConstraint();
		Integer maxConstraintID = currentProcessElement.getMaxConstraintId();
		//c.setId(++maxConstraintID);

		RelationConstraintElement cElement = new RelationConstraintElement(maxConstraintID, template);
	
		//Create new Position Element
		double contraintRadius = config.getDouble("constraint.radius");
		double activityRadius = config.getDouble("activity.radius");
		double posX = (aElem1.getPosX() + aElem2.getPosX()) / 2 + activityRadius - contraintRadius; 
		double posY = (aElem1.getPosY() + aElem2.getPosY()) / 2 + activityRadius - contraintRadius;
		cElement.setPosition(posX, posY);
		
		// add Constraint to Process
		//currentProcessElement.getProcessPosition().getConstraint().add(new XMLRelationConstraintPosition(maxConstraintID, posX,posY));
		currentProcessElement.addRelationConstraint(cElement);
		

		RelationConstraintNode cNode = createConstraintNode(cElement);
		addAdditionalActivity(aElem1, cNode, 1);
		addAdditionalActivity(aElem2, cNode, 2);

		int amountOfLinesOnPane = 1; // start with 1 because of backgroundPane
		for(ActivityElement aElem : currentProcessElement.getActivityEList()){
			amountOfLinesOnPane += aElem.getConstraintList().size();
		}
		
		anchorPane.getChildren().add(amountOfLinesOnPane,cNode);	
		eventManager.setEventHandler(cNode);
		
		determineConstraints();
		return cElement;
	}
	
	/**
	 * adjusts the Constraint to be in relation with the new Activity Node. Makes sure that the old ActivityNode is disconnected.
	 * @param newNode
	 */
	public void adjustRelationConstraint(ActivityNode newNode, int parameterNumber){
		RelationConstraintElement cElement = ((RelationConstraintNode) selectedElement).getConstraintElement();
		//side is set by OnClick Action of the Button
		//save unconnected Activity
		removeActivityFromRelationConstraint(oldNode, parameterNumber);
		addAdditionalActivity(newNode.getActivityElement(), null, parameterNumber);
	}
	
	/**
	 * adds an Activity to the Activation Side of a constraint
	 * @param aNode defines the ActivityNode
	 * @param cNode defines the ConstraintNode, if NULL the selectedElement has to be a ConstraintNode and will be connected
	 */
	public void addAdditionalActivity(ActivityElement aNode, RelationConstraintNode cNode, int parameterNumber){
		RelationConstraintElement cElement;
		RelationConstraintNode rcNode;
		if(cNode == null) {
			cElement = ((RelationConstraintNode) selectedElement).getConstraintElement();
			rcNode = (RelationConstraintNode) selectedElement;
		}
		else { 
			cElement = cNode.getConstraintElement();
			rcNode = cNode;
		}
		cElement.addActivityElement(aNode, parameterNumber, 0d, 0d, 0d);
		LineNode newLine = rcNode.createAndSetLineNode(determineActivityNode(aNode), parameterNumber);
		
		//int position = currentProcessElement.getActivityEList().size() + 1;		// line has to be added after Activities
		anchorPane.getChildren().add(1,newLine);		// position 0 is BackgroundPane, but has to be behind other Nodes
	
		sendMouseReleaseEvent(cNode);
	}
	
	
	/**
	 * Removes the connection between the selected RelationConstraintNode and the given ActivityNode.
	 * This includes the removal from contentPane and the removal of the logic in all elements
	 * @param aNode
	 * @param parameterNumber
	 */
	public void removeActivityFromRelationConstraint(ActivityNode aNode, int parameterNumber){
		RelationConstraintElement cElement = ((RelationConstraintNode) selectedElement).getConstraintElement();
		cElement.removeActivity(aNode.getActivityElement(),parameterNumber);
		LineNode removedLine = aNode.getLineNodeByConstraint(cElement);
		aNode.removeLineNode(removedLine);
		
		((RelationConstraintNode) selectedElement).removeActivity(removedLine, parameterNumber);
		
		anchorPane.getChildren().remove(removedLine);
		determineConstraints();
		
		sendMouseReleaseEvent(((RelationConstraintNode) selectedElement));
	}
	
	/**
	 * updates the given activityNode at the editPane
	 * @param activityNode
	 */
	public void editActivity(ActivityNode activityNode) {
		editActivityPane.setActivity(activityNode);
	}
	/**
	 * updates the given constraintNode at the editPane
	 * @param constraintNode
	 */
	public void editConstraint(RelationConstraintNode constraintNode){
		editConstraintPane.setConstraint(constraintNode);
	}

	/**
	 * removes constraint from currentProcessElement 
	 * removes constraintNode and lineNodes of the constraintNode
	 * @param constraintNode
	 */
	public void deleteRelationConstraint(RelationConstraintNode constraintNode) {
		//TODO disconnect RelationConstraint from all activities
		currentProcessElement.deleteRelationConstraint(constraintNode.getConstraintElement());
		constraintElements.remove(constraintNode.getConstraintElement());
		
		for (ActivityElement aElement : constraintNode.getConstraintElement().getParameter1Elements()) {
			determineActivityNode(aElement).removeLineNode(determineActivityNode(aElement).getLineNodeByConstraint(constraintNode.getConstraintElement()));
		}
		for (ActivityElement aElement : constraintNode.getConstraintElement().getParameter2Elements()) {
			determineActivityNode(aElement).removeLineNode(determineActivityNode(aElement).getLineNodeByConstraint(constraintNode.getConstraintElement()));
		}
		
		anchorPane.getChildren().remove(constraintNode);
		anchorPane.getChildren().removeAll(constraintNode.getParameter1Lines());
		anchorPane.getChildren().removeAll(constraintNode.getParameter2Lines());
		
		determineConstraints();
		
		sendMouseReleaseEvent(backgroundPane);
	}
	
	/**
	 * activates the selection mode to make only activities selectable which are suitable for the new RelationConstraint
	 * The currently selected Activity is the only Node which is not plausible to select
	 */
	public void setSelectionModeToAddConstraint(){
		if(currentProcessElement.getActivityEList().size() <= 1){
			MinerfulGuiUtil.displayAlert("Error", "Not possible!", "Please add an ACTIVITY before you add a CONSTRAINT with two parameters. Otherwise you can not set the second parameter.", AlertType.INFORMATION);
		} else {
			ActivityElement selectedActivity = ((ActivityNode) selectedElement).getActivityElement();
			ActivityNode aNode = (ActivityNode) selectedElement;
			this.addConstraintMode = AddConstraintMode.NEW_CONSTRAINT;
			//Disable everything
			disableAllElements(true);
			//Enable all Activities and set EventHandlers on all but this activity
			for (ActivityElement aElem : currentProcessElement.getActivityEList()){
				//only choose if it is not part of the activation side
				if (aElem != selectedActivity){
					determineActivityNode(aElem).setDisable(false);
					eventManager.setActivityToSelectionMode(determineActivityNode(aElem), 2);
					//Highlight
					determineActivityNode(aElem).getStyleClass().add(highlightedClass);
				}
			}
		}
	}
	
	/**
	 * activates the selection mode to make only activities selectable which are suitable for the RelationConstraint  
	 * @param oldElement - gives the ActivityElement which will be exchanged (may be null)
	 * @param mode - defines if there will be an exchange of Activies, or an additional Activity
	 * @param parameterNumber - relevant for Exchange_Activity only to decide the position of the parameter
	 */
	public void setSelectionModeToChangeConstraint(ActivityNode oldElement, AddConstraintMode mode, int parameterNumber){
		RelationConstraintElement selectedConstraint = ((RelationConstraintNode) selectedElement).getConstraintElement();
		int amountOfConnectedActivities = selectedConstraint.getParameter1Elements().size() + selectedConstraint.getParameter2Elements().size();
		if (mode == AddConstraintMode.EXCHANGE_ACTIVITY){
			amountOfConnectedActivities -= 1;	// in case of Activity Exchange there is one more selectable Element
		}
		if(currentProcessElement.getActivityEList().size() <= amountOfConnectedActivities){
			MinerfulGuiUtil.displayAlert("Error", "Not possible!", "Please add an additional ACTIVITY. All available ACTIVITIES are already part of this CONSTRAINT.", AlertType.INFORMATION);
		} else {
			addConstraintMode = mode;
			//Disable everything
			disableAllElements(true);
			
			//Enable all Activities and set EventHandlers on all but the activities on of this RelationConstraint
			for (ActivityElement aElem : currentProcessElement.getActivityEList()){
				//only enable if it is not part of the constraint already
				if ((!selectedConstraint.getParameter1Elements().contains(aElem)) && (!selectedConstraint.getParameter2Elements().contains(aElem)) ){
					determineActivityNode(aElem).setDisable(false);
					// set eventhandler to allow Activity to be selected
					eventManager.setActivityToSelectionMode(determineActivityNode(aElem), parameterNumber);
					determineActivityNode(aElem).getStyleClass().add(highlightedClass);
				}
			}
			//Make old Element also available for selection if there is one
			if (oldElement != null){
				oldElement.setDisable(false);
				eventManager.setActivityToSelectionMode(oldElement, parameterNumber);
				oldElement.getStyleClass().add(highlightedClass);
			}
		}
	}
	
	public ActivityNode determineActivityNode(ActivityElement activityElement) {
		for(ActivityNode activityNode : activityNodes) {
			if(activityNode.getActivityElement() == activityElement) {
				return activityNode;
			}
		}
		
		return null;
	}
	
	public RelationConstraintNode determineRelationConstraintNode(RelationConstraintElement relationElement) {
		for(RelationConstraintNode rcNode : constraintNodes) {
			if(rcNode.getConstraintElement() == relationElement) {
				return rcNode;
			}
		}
		
		return null;
	}
	
	
	/**
	 * sets all nodes of the process back to normal state after an Activity has been selected. 
	 * This includes styling, disabling and ClickEvents
	 */
	public void resetToNormalState() {
		//remove selectionEventHandler from all Activities and add normal ones
		for (ActivityElement a : currentProcessElement.getActivityEList()){
			determineActivityNode(a).setOnMouseReleased(null);
			eventManager.setEventHandler(determineActivityNode(a));
			determineActivityNode(a).getStyleClass().clear();
			determineActivityNode(a).getStyleClass().add(activityClass);
		}
		//Enable all Elements
		disableAllElements(false);
		
		addConstraintMode = null;
	}

	/**
	 * disables or enables all Elements of the ContentPane + the Controll Buttons to prevent Clicking on them during SelectionMode
	 * @param disable
	 */
	private void disableAllElements(boolean disable){
		for(Node n : anchorPane.getChildren()){
			n.setDisable(disable);
		}
		addActivityB.setDisable(disable);
		editTabPane.setDisable(disable);
	}
	

	

	
	/**
	 * determines the Node that is translated the most to the right, and the one to 
	 * the bottom and saves the coordinates to define the edge of the background.
	 * Field Scrollpadding defines how much padding there is between the outer elements 
	 * and the edge of the background.
	 */
	public void setMaxTranslate(){
		double maxX = scrollPanePadding;
		double maxY = scrollPanePadding;
		for(Node n : anchorPane.getChildren()){
			if (n.getTranslateX() + scrollPanePadding > maxX) {
				maxX = n.getTranslateX() + scrollPanePadding;
			}
			if (n.getTranslateY() + scrollPanePadding > maxY){
				maxY = n.getTranslateY() + scrollPanePadding;
			}
		}
		maxTranslateX.set(maxX);
		maxTranslateY.set(maxY);
	}
	
	public ProcessElement getCurrentProcessElement() {
		return currentProcessElement;
	}

	public AddConstraintMode isAddConstraintMode() {
		return addConstraintMode;
	}

	public Selectable getSelectedElement() {
		return selectedElement;
	}

	public void setSelectedElement(Selectable selectedElement) {
		this.selectedElement = selectedElement;
	}

	public Accordion getEditTabPane() {
		return editTabPane;
	}

	public TitledPane getActivityPane() {
		return activityPane;
	}


	public TitledPane getConstraintPane() {
		return constraintPane;
	}

	public BorderPane getBackgroundPane() {
		return backgroundPane;
	}

	/**
	 * oldNode is a needed variable for deleting Activities from Constraints
	 * @param oldNode
	 */
	public void setOldNode(ActivityNode oldNode) {
		this.oldNode = oldNode;
	}

	public AnchorPane getContentPane() {
		return anchorPane;
	}

	/**
	 * Imitates a MouseClick on a specified Node
	 * @param targetNode
	 */
	void sendMouseReleaseEvent(Node targetNode) {
		MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
		Event.fireEvent(targetNode, mouseEvent);
	}
	
	public void determineConstraints() {
		constraintElements.clear(); 
		constraintElements.addAll(GraphUtil.determineConstraints(currentProcessElement));
		
	}
	
	public void determineActivities() {
		activityElements.clear();
		activityElements.addAll(currentProcessElement.getActivityEList());
	}
	
	public List<ActivityNode> getActivityNodes() {
		return activityNodes;
	}

	public void setActivityNodes(List<ActivityNode> activityNodes) {
		this.activityNodes = activityNodes;
	}

	public List<RelationConstraintNode> getConstraintNodes() {
		return constraintNodes;
	}

	public void setConstraintNodes(List<RelationConstraintNode> constraintNodes) {
		this.constraintNodes = constraintNodes;
	}
	
	@Override
	public AnchorPane getAnchorPane() {
		// TODO Auto-generated method stub
		return anchorPane;
	}

	@Override
	public ProcessModel getCurrentProcessModel() {
		// TODO Auto-generated method stub
		return processModel;
	}

	@Override
	public ScrollPane getScrollPane() {
		// TODO Auto-generated method stub
		return scrollPane;
	}

	@Override
	public DoubleProperty getMaxTranslateX() {
		return maxTranslateX;
	}

	@Override
	public DoubleProperty getMaxTranslateY() {
		return maxTranslateY;
	}

	@Override
	public boolean isParamsStylingActive() {
		// TODO Auto-generated method stub
		return false;
	}
	

}

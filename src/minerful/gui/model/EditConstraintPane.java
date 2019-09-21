package minerful.gui.model;

import java.util.HashMap;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import minerful.gui.controller.ModelGeneratorTabController;

public class EditConstraintPane extends ScrollPane {

	private ModelGeneratorTabController controller;
	private VBox activityEditVBox = new VBox();
	private VBox typeEditVBox = new VBox();
	private RelationConstraintNode selectedConstraint;
	/*
	 * private Button a1Button; private Button a2Button;
	 */
	private Label typeLabel;
	private final ToggleGroup templateGroup = new ToggleGroup(); 
	private HashMap<Template, RadioButton> radioButtonOfTemplate = new HashMap<Template, RadioButton>();
	/*
	private RadioButton precedenceRB;
	private RadioButton respondedExistenceRB;
	private RadioButton coExistenceRB;
	private RadioButton responseRB;
	private RadioButton alternateResponseRB;
	private RadioButton alternatePrecedenceRB;
	private RadioButton chainResponseRB;
	private RadioButton chainPrecedenceRB;
	private RadioButton successionRB;
	private RadioButton alternateSuccessionRB;
	private RadioButton chainSuccessionRB;
	private RadioButton notSuccessionRB;
	private RadioButton notChainSuccessionRB;
	private RadioButton notCoExistenceRB;
	*/
	private VBox parameter1Area;
	private VBox parameter2Area;
	private final int buttonImageSize = 12;

	public EditConstraintPane(ModelGeneratorTabController controller) {
		super();
		this.controller = controller;
		setFitToHeight(true);
		setFitToWidth(true);
		setPrefWidth(200.0);
		
	
		
		// STRUCTURE OF PANE TO EDIT ACTIVIY
		VBox contentOfConstraintPane = new VBox(activityEditVBox, typeEditVBox);
		setContent(contentOfConstraintPane);
		contentOfConstraintPane.getStyleClass().add("editPane");

		activityEditVBox.setAlignment(Pos.TOP_CENTER);
		Label sideATitle = new Label("Parameter 1:");
		sideATitle.getStyleClass().add("editpane-header");
		sideATitle.getStyleClass().add("EditPaneH2");
		
		Label sideBTitle = new Label("Parameter 2:");
		sideBTitle.getStyleClass().add("editpane-header");
		sideBTitle.getStyleClass().add("EditPaneH2");
		
		FontIcon arrowLabel = new FontIcon("typ-arrow-down-thick");
		arrowLabel.setIconSize(20);
		parameter1Area = new VBox();
		parameter2Area = new VBox();
		
		parameter1Area.getStyleClass().add("editConstraintSideBox");
		parameter2Area.getStyleClass().add("editConstraintSideBox");
		

		Button addParameter1Button = new Button("");
		addParameter1Button.setOnAction((event) -> {
			controller.setSelectionModeToChangeConstraint(null, AddConstraintMode.ADD_TO_PARAMETER1, 1);
		});

		Pane expandingdPane = new Pane();
		expandingdPane.setPrefWidth(0);
		HBox.setHgrow(expandingdPane, Priority.ALWAYS);
		HBox addParameter1HBox = new HBox(expandingdPane, addParameter1Button);

		Button addParameter2Button = new Button("");
		addParameter2Button.setOnAction((event) -> {
			controller.setSelectionModeToChangeConstraint(null, AddConstraintMode.ADD_TO_OPARAMETER2, 2);
		});

		// Pane expandingdPane = new Pane();
		Pane expandingdPane2 = new Pane();
		HBox addParameter2HBox = new HBox(expandingdPane2, addParameter2Button);
		HBox.setHgrow(expandingdPane2, Priority.ALWAYS);

		Image AddButtonImage = new Image(getClass().getClassLoader().getResource("images/Add-icon.png").toExternalForm(),
				buttonImageSize, buttonImageSize, true, true);
		addParameter1Button.setGraphic(new ImageView(AddButtonImage));
		addParameter2Button.setGraphic(new ImageView(AddButtonImage));
		
		addParameter1HBox.getStyleClass().add("editConstraintAddButtonBox");
		addParameter2HBox.getStyleClass().add("editConstraintAddButtonBox");

		activityEditVBox.getChildren().addAll(sideATitle, parameter1Area, addParameter1HBox, arrowLabel, sideBTitle,
				parameter2Area, addParameter2HBox);

		typeLabel = new Label("Type:");
		typeLabel.getStyleClass().add("editpane-header");
		typeLabel.getStyleClass().add("EditPaneH2");
	
		typeEditVBox.getChildren().add(typeLabel);
		
		for (Template template : controller.getCurrentProcessElement().getTemplateList()){
			typeEditVBox.getChildren().add(createTemplateSelectionBox(template));
		}
		/*
		typeEditVBox.getChildren().addAll(typeLabel, coExistenceBox, respondedExistenceBox, notCoExistenceBox, precedenceBox, successionBox, responseBox, notSuccessionBox, alternatePrecedenceBox, alternateSuccessionBox, alternateResponseBox,
				chainResponseBox, chainPrecedenceBox,chainSuccessionBox, notChainSuccessionBox);
		*/
		for (Node n : typeEditVBox.getChildren()){
			n.getStyleClass().add("editConstraintTypeSelection");
		}
		typeEditVBox.getStyleClass().add("editConstraintTypeBox");

		setDisable(true);

	}
	
	/**
	 * creates an EventHandler for the RadioButtons to change the RelationConstraint to the correct Type of the Template
	 * @param template
	 * @return the eventHandler
	 */
	private EventHandler<ActionEvent> createActionEventHandler(Template template){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
            	selectedConstraint.getConstraintElement().changeConstraintType(template);
            	selectedConstraint.changeConstraintType();
            }
        };
        return handler;
		
	}
	
	private HBox createTemplateSelectionBox(Template template){
		
		FontIcon fontIcon = new FontIcon("fa-question-circle");
		fontIcon.setIconSize(15);
		fontIcon.setIconColor(Paint.valueOf("#0966d7"));
		RadioButton radioButton = new RadioButton(template.getName());
		radioButtonOfTemplate.put(template, radioButton);
		
		//TODO: load Tooltip of template by template.getInformation();
		radioButton.setTooltip(new Tooltip("Give some information about this type of constraint..."));
		HBox templateBox = new HBox(radioButton, fontIcon);
		radioButton.setToggleGroup(templateGroup);
		radioButton.setOnAction(createActionEventHandler(template));
		return templateBox;
	}
	
	/**
	 * adds a Button of an Activity to the EditPane either on one of to sides of the RelationConstraint
	 * 
	 * @param aNode - defines the Node that should be added
	 * @param parameterNumberBox - defines the location of the new Button
	 */
	private void addActivityToConstraintButtons(ActivityNode aNode, VBox parameterNumberBox, int numberOfElements) {

		Label activityIdentifier = new Label(aNode.getActivityElement().getIdentifier());

		Button exchangeActivityButton = new Button();
		Button removeButton = new Button("");
		Pane expandingPane2 = new Pane();
		expandingPane2.setPrefHeight(0);
		VBox.setVgrow(expandingPane2, Priority.ALWAYS);
		VBox editActivityButtons = new VBox(exchangeActivityButton, expandingPane2);
		// only add the remove Button if there is more than one element on this
		// side to prevent half constraints
		if (numberOfElements > 1) {
			editActivityButtons.getChildren().add(removeButton);
		}
		Pane expandingPane = new Pane();
		expandingPane.setPrefWidth(0);
		HBox.setHgrow(expandingPane, Priority.ALWAYS);

		HBox buttonHBox = new HBox(activityIdentifier, expandingPane, editActivityButtons);
		parameterNumberBox.getChildren().add(buttonHBox);

		// Styling
		buttonHBox.getStyleClass().add("constraintPaneActivityBox");
		Image removeButtonImage = new Image(getClass().getClassLoader().getResource("images/Remove-icon.png").toExternalForm(),
				buttonImageSize, buttonImageSize, true, true);
		removeButton.setGraphic(new ImageView(removeButtonImage));
		Image exchangeButtonImage = new Image(getClass().getClassLoader().getResource("images/Exchange-icon.png").toExternalForm(),
				buttonImageSize, buttonImageSize, true, true);
		exchangeActivityButton.setGraphic(new ImageView(exchangeButtonImage));
		activityIdentifier.getStyleClass().add("constraintPaneActivityLabel");
		activityIdentifier.setPrefWidth(150);
		activityIdentifier.setWrapText(true);
		activityIdentifier.setMinHeight(60);
		activityIdentifier.setMaxHeight(100);
		activityIdentifier.setAlignment(Pos.CENTER);
		activityIdentifier.setTextAlignment(TextAlignment.CENTER);

		// Define Behavior OnAction
		exchangeActivityButton.setOnAction((event) -> {
			controller.setOldNode(aNode);
			int parameterNumber;
			if (parameterNumberBox == parameter1Area) {
				parameterNumber = 1;
			} else {
				parameterNumber = 2;
			}			
			controller.setSelectionModeToChangeConstraint(aNode,
					AddConstraintMode.EXCHANGE_ACTIVITY, parameterNumber);
		});

		removeButton.setOnAction((event) -> {
			if (parameterNumberBox == parameter1Area) {
				controller.removeActivityFromRelationConstraint(aNode, 1);
			} else {
				controller.removeActivityFromRelationConstraint(aNode,2);
			}
		});
	}

	/**
	 * should be called when an RelationConstraintElement is clicked
	 * @param cNode
	 */
	public void setConstraint(RelationConstraintNode cNode) {
		selectedConstraint = cNode;
		updatePane();

	}

	/**
	 * updates the EditConstraintPane to display the correct info of the currently selected Constraint
	 */
	private void updatePane() {
		parameter1Area.getChildren().clear();
		parameter2Area.getChildren().clear();
		if (selectedConstraint != null) {
			if (selectedConstraint.getConstraintElement() != null) { // for initial update
				int numberOfActivatonElements = selectedConstraint.getConstraintElement().getParameter1Elements().size();
				for (ActivityElement aElement : selectedConstraint.getConstraintElement().getParameter1Elements()) {
					addActivityToConstraintButtons(controller.determineActivityNode(aElement), parameter1Area, numberOfActivatonElements);
				}

				int numberOfParameter2Elements = selectedConstraint.getConstraintElement().getParameter2Elements().size();
				for (ActivityElement aElement : selectedConstraint.getConstraintElement().getParameter2Elements()) {
					addActivityToConstraintButtons(controller.determineActivityNode(aElement), parameter2Area, numberOfParameter2Elements);
				}
				// TODO: differentiate between all types of Constraints
				setTypeSelectionRadioButton();
				
			}
		} else {
			// initially loading no information of the Pane
		}
	}
	
	/**
	 * part of updatePane method. Sets the selection of the RadioButtons to the currently selected template of the RelationConstraint.
	 */
	private void setTypeSelectionRadioButton(){
		Template template = controller.getCurrentProcessElement().getTemplateByName(selectedConstraint.getConstraintElement().getTemplate().getName());
		
		RadioButton button;
		
		for(Template temp : radioButtonOfTemplate.keySet()) {
			if(temp.getName().equals(selectedConstraint.getConstraintElement().getTemplate().getName())) {
				button = radioButtonOfTemplate.get(temp);
				button.setSelected(true);
			}
		}

		/*switch (template) {
		case PRECEDENCE:
			precedenceRB.setSelected(true);
			break;
			
		case RESPONDEDEXISTENCE:
			respondedExistenceRB.setSelected(true);
			break;
			
		case COEXISTENCE:
			coExistenceRB.setSelected(true);
			break;
			
		case RESPONSE:
			responseRB.setSelected(true);
			break;
			
		case ALTERNATERESPONSE:
			alternateResponseRB.setSelected(true);
			break;
			
		case ALTERNATEPRECEDENCE:
			alternatePrecedenceRB.setSelected(true);
			break;
			
		case CHAINRESPONSE:
			chainResponseRB.setSelected(true);
			break;
			
		case CHAINPRECEDENCE:
			chainPrecedenceRB.setSelected(true);
			break;
			
		case SUCCESSION:
			successionRB.setSelected(true);
			break;
			
		case ALTERNATSUCCESION:
			alternateSuccessionRB.setSelected(true);
			break;
			
		case CHAINSUCCESSION:
			chainSuccessionRB.setSelected(true);
			break;
			
		case NOTSUCCESSION:
			notSuccessionRB.setSelected(true);
			break;
			
		case NOTCHAINSUCCESSION:
			notChainSuccessionRB.setSelected(true);
			break;
			
		case NOTCOEXISTENCE:
			notCoExistenceRB.setSelected(true);
			break;
		default: 
			precedenceRB.setSelected(true);
			break;
		}
		*/
	}

}

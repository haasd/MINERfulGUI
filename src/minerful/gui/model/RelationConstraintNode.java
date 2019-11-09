package minerful.gui.model;


import java.io.Serializable;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import minerful.gui.controller.ModelGeneratorTabController;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.util.Config;

/**
 * ConstraintNode displays the information of an Constraint. The Node is based on an in
 * invisible StackPane which holds the following elements:
 * 		Circle - (optional) to display circle for unchained constraints
 * 		Label - (optional) to show the asterisk label
 * 		Buttons - (optional) to delete this Constraint
 * 		LineNodes - Lines to both activities
 * @author Lukas
 */
public class RelationConstraintNode extends StackPane implements Positionable, Selectable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -210909592012283202L;

	private Config config = new Config("config");
	
	private RelationConstraintElement constraintElement;
	

	private Circle clickable;
	private Image imgDelete;
	private Button deleteButton;
	private ProcessElementInterface controller;
	
	private String selectedClass = "selectedActivity";
	private Label asteriskLabel;
	
	private boolean renderingOptimization = config.getBoolean("optimize.rendering");
	private String newNegativeStyle = "newNegativeCircle";
	private String oldNegativeStyle = "activity";
	private String normalStyle = "activity";
	
	private int buttonMargin = -10;
	private double radius;
	private double activityRadius = config.getDouble("activity.radius");
	private double constraintRadius = config.getDouble("constraint.radius");
	
	private boolean parameterStylingIsActive = false;

	private ArrayList<LineNode> parameter1Lines = new ArrayList<LineNode>();
	private ArrayList<LineNode> parameter2Lines = new ArrayList<LineNode>();
	
	public RelationConstraintNode(RelationConstraintElement constraintElement, ProcessElementInterface controller) {
		this.constraintElement = constraintElement;
		this.controller = controller;
		radius = config.getDouble("constraint.radius");

		//Circle
		this.clickable = new Circle(radius); 
		this.getChildren().add(clickable);

		//Label
		asteriskLabel = new Label("*");
		this.getChildren().add(asteriskLabel);
		
		//Action Buttons
		deleteButton = new Button("");
		deleteButton.setOnAction((event)-> {if(controller instanceof ModelGeneratorTabController) {((ModelGeneratorTabController) this.controller).deleteRelationConstraint(this);}});
		//TODO: DISABLE AGAIN
		// = new Image(getClass().getResource("resources/Delete-icon.png").toExternalForm(),12,12,true,true);
		//deleteButton.setGraphic(new ImageView(imgDeleteButton));
		//this.getChildren().add(deleteButton);
		StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
		StackPane.setMargin(deleteButton, new Insets(buttonMargin, buttonMargin, 0, 0));
		
		
		this.setTranslateX(this.constraintElement.getPosX());
		this.setTranslateY(this.constraintElement.getPosY());
		
		//Set StyleClasses
		this.getStyleClass().add("activityPane");
		asteriskLabel.getStyleClass().add("activityLabel");
		clickable.getStyleClass().add("activity");
		setEditable(false);
	}
	
	/**
	 * changes visibility of buttons of this Constraint to interact with it and changes
	 * appearance of this node 
	 */
	@Override
	public void setEditable(boolean editable) {
		if(editable){
			if (!getChildren().contains(deleteButton)){
				imgDelete =  new Image(getClass().getClassLoader().getResource("images/Delete-icon.png").toExternalForm(),12,12,true,true);
				deleteButton.setGraphic(new ImageView(imgDelete));
				
				this.getChildren().add(deleteButton);
			}
			clickable.getStyleClass().add(selectedClass);
			if(controller instanceof ModelGeneratorTabController) {
				((ModelGeneratorTabController) controller).editConstraint(this);
			}
			
		} else {
			if (getChildren().contains(deleteButton)){
				imgDelete = null;
				this.getChildren().remove(deleteButton);
			}
			clickable.getStyleClass().remove(selectedClass);
		}
	}


	public Circle getCicrle() {
		return clickable;
	}

	public RelationConstraintElement getConstraintElement() {
		return constraintElement;
	}
	
	@Override
	public void updatePosition() {
		constraintElement.setPosition(getTranslateX(), getTranslateY());
	}

	
	public Button getDeleteButton(){
		return this.deleteButton;
	}
	
	public Label getAsteriskLabel() {
		return asteriskLabel;
	}

	@Override
	public double getPosX() {
		// TODO Auto-generated method stub
		return getTranslateX();
	}

	@Override
	public double getPosY() {
		// TODO Auto-generated method stub
		return getTranslateY();
	}

	public void moveConstraintBetweenActivities() {
		ActivityNode activity1 = controller.determineActivityNode(constraintElement.getParameter1Elements().get(0));
		ActivityNode activity2 = controller.determineActivityNode(constraintElement.getParameter2Elements().get(0));
		
		double activity1CenterX = activity1.getTranslateX() + activityRadius;
		double activity2CenterX = activity2.getTranslateX() + activityRadius;
		double activity1CenterY = activity1.getTranslateY() + activityRadius;
		double activity2CenterY = activity2.getTranslateY() + activityRadius;
		
		double centerPositionX = (activity1CenterX + activity2CenterX) / 2 - constraintRadius;
		double centerPositionY = (activity1CenterY + activity2CenterY) / 2 - constraintRadius;
		
		setTranslateX(centerPositionX);
		setTranslateY(centerPositionY);
		
		updatePosition();
		
		//Update Line Positions on Scene     
        
        for(LineNode line : getParameter1Lines()){
      	   line.updateLinePosition();
      	   
        }
        for(LineNode line : getParameter2Lines()){
       	   line.updateLinePosition();
        }
		
	}
	
	public void moveConstraintBetweenActivities(Integer diff) {
		ActivityNode activity1 = controller.determineActivityNode(constraintElement.getParameter1Elements().get(0));
		ActivityNode activity2 = controller.determineActivityNode(constraintElement.getParameter2Elements().get(0));
		
		double activity1CenterX = activity1.getTranslateX() + activityRadius;
		double activity2CenterX = activity2.getTranslateX() + activityRadius;
		double activity1CenterY = activity1.getTranslateY() + activityRadius;
		double activity2CenterY = activity2.getTranslateY() + activityRadius;
		
		double offsetPixels = 5.0;
		
		double L = Math.sqrt((activity1CenterX-activity2CenterX)*(activity1CenterX-activity2CenterX)+(activity1CenterY-activity2CenterY)*(activity1CenterY-activity2CenterY));
		
		double parallel1CenterX = activity1.getTranslateX() + activityRadius + offsetPixels * diff * (activity2CenterY-activity1CenterY) / L;
		double parallel2CenterX = activity2.getTranslateX() + activityRadius + offsetPixels * diff * (activity2CenterY-activity1CenterY) / L;
		double parallel1CenterY = activity1.getTranslateY() + activityRadius + offsetPixels * diff * (activity1CenterX-activity2CenterX) / L;
		double parallel2CenterY = activity2.getTranslateY() + activityRadius + offsetPixels * diff * (activity1CenterX-activity2CenterX) / L;
		
		double centerPositionX = ((parallel1CenterX + parallel2CenterX) / 2 - constraintRadius);
		double centerPositionY = ((parallel1CenterY + parallel2CenterY) / 2 - constraintRadius);
		
		setTranslateX(centerPositionX);
		setTranslateY(centerPositionY);
		
		updatePosition();
		
		//Update Line Positions on Scene     
        
        for(LineNode line : getParameter1Lines()){
      	   line.updateLinePosition();
        }
        for(LineNode line : getParameter2Lines()){
       	   line.updateLinePosition();
        }
		
	}

	public void setRenderingOptimization(Boolean optimizationMode) {
		this.renderingOptimization = optimizationMode;
		
	}

	public void updateAfterChanges() {
		clickable.getStyleClass().clear();
		
		//TemplateEnum enumType = constraintElement.getTemplate();
		Template type = this.constraintElement.getTemplate();//RelationConstraintTemplateConverter.translateEnumToTemplate(enumType);
		if (type!=null){
			if(type.getNegation()){
				if (renderingOptimization){
					clickable.getStyleClass().add(newNegativeStyle);
				}
				else {
					clickable.getStyleClass().add(oldNegativeStyle);
				}
			} else {
				clickable.getStyleClass().add(normalStyle);
			}
			clickable.getStyleClass().add(normalStyle);
		}
		if(this.controller != null) {
			controller.determineConstraints();
		}
	}

	/**
	 * creates a LineNode from this RelationConstraint to the given ActivityElement.
	 * ActivityElement should already be in List of Activities of this relationConstraint. 
	 * Line will be added to the list of paramater1Elements of this relationConstraint.
	 * @param activityElement
	 * @param parameterNumber - defines the relevant parameter of the constraint
	 * @return - the lineNode which has to be added to the contentPane
	 */
	public LineNode createAndSetLineNode(ActivityNode activityNode, int parameterNumber){	
		LineNode newLine = new LineNode(this, activityNode,parameterNumber);
		if(parameterNumber == 1){
			parameter1Lines.add(newLine);
		} else {
			if (parameterNumber == 2){
				parameter2Lines.add(newLine);
			}
		}
		activityNode.addLineNode(newLine);
		newLine.updateLineElementsAfterChanges();
		updateAfterChanges();
		return newLine;
	}
	
	/**
	 * Removes the Connection between a this RelationConstraint and an Activity
	 * @param activityElement
	 * @return - the lineNode which has to be removed from the contentPane
	 */
	public LineNode removeActivity(LineNode removedLine, int parameterNumber){
		
		if (parameterNumber == 1){
			parameter1Lines.remove(removedLine);
		} else {
			if (parameterNumber == 2){
				parameter2Lines.remove(removedLine);
			}
		}
		
		return removedLine;
	}

	public ArrayList<LineNode> getParameter1Lines() {
		return parameter1Lines;
	}

	public void setParameter1Lines(ArrayList<LineNode> parameter1Lines) {
		this.parameter1Lines = parameter1Lines;
	}

	public ArrayList<LineNode> getParameter2Lines() {
		return parameter2Lines;
	}

	public void setParameter2Lines(ArrayList<LineNode> parameter2Lines) {
		this.parameter2Lines = parameter2Lines;
	}

	public void changeConstraintType(){
		updateAfterChanges();
		for (LineNode line : getParameter1Lines()){
			line.updateLineElementsAfterChanges();
		}
		for (LineNode line : getParameter2Lines()){
			line.updateLineElementsAfterChanges();
		}
	}
	
	public boolean isParameterStylingIsActive() {
		return parameterStylingIsActive;
	}

	public void setParameterStylingIsActive(boolean parameterStylingIsActive) {
		this.parameterStylingIsActive = parameterStylingIsActive;
	}
}

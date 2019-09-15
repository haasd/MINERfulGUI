package minerful.gui.model;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import minerful.gui.controller.DiscoverTabController;
import minerful.gui.controller.ModelGeneratorTabController;
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
public class RelationConstraintNode extends StackPane implements Positionable, Selectable {
	private Config config = new Config("config");
	
	private RelationConstraintElement constraintElement;
	

	private Circle clickable;
	private Image imgDelete;
	private Button deleteButton;
	private ModelGeneratorTabController controller;
	
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
	
	public RelationConstraintNode(RelationConstraintElement constraintElement, ModelGeneratorTabController controller) {
		constraintElement.setConstraintNode(this);
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
		deleteButton.setOnAction((event)-> {this.controller.deleteRelationConstraint(this);});
		//TODO: DISABLE AGAIN
		// = new Image(getClass().getResource("resources/Delete-icon.png").toExternalForm(),12,12,true,true);
		//deleteButton.setGraphic(new ImageView(imgDeleteButton));
		//this.getChildren().add(deleteButton);
		StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
		StackPane.setMargin(deleteButton, new Insets(buttonMargin, buttonMargin, 0, 0));
		
		
		this.setTranslateX(this.constraintElement.getPosX());
		this.setTranslateY(this.constraintElement.getPosY());
		
		this.constraintElement.setConstraintNode(this);
		
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
			controller.editConstraint(this);
			
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
		double activity1CenterX = constraintElement.getParameter1Elements().get(0).getNode().getTranslateX() + activityRadius;
		double activity2CenterX = constraintElement.getParameter2Elements().get(0).getNode().getTranslateX() + activityRadius;
		double activity1CenterY = constraintElement.getParameter1Elements().get(0).getNode().getTranslateY() + activityRadius;
		double activity2CenterY = constraintElement.getParameter2Elements().get(0).getNode().getTranslateY() + activityRadius;
		
		double centerPositionX = (activity1CenterX + activity2CenterX) / 2 - constraintRadius;
		double centerPositionY = (activity1CenterY + activity2CenterY) / 2 - constraintRadius;
		
		setTranslateX(centerPositionX);
		setTranslateY(centerPositionY);
		
		//Update Line Positions on Scene     
        
        for(LineNode line : constraintElement.getParameter1Lines()){
      	   line.updateLinePosition();
        }
        for(LineNode line : constraintElement.getParameter2Lines()){
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
		
		controller.determineConstraints();
	}

	
	


}

package minerful.gui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import minerful.gui.controller.ModelGeneratorTabController;
import minerful.gui.model.xml.XMLExistenceConstraint;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.util.Config;

/**
 * ActivityNode displays the information of an Activity. The Node is based on an in
 * invisible StackPane which holds the following elements:
 * 		Circle - to display the Activity
 * 		Label - to show the identifier of the Activity
 * 		Buttons - (optional) to delete this Activity and to add a Constraint starting from this Activity
 * 		structuralShapes - (optional) initShape and EndShape
 * 		cardinalityShape - (optional) with different labels 
 * @author Lukas
 */
public class ActivityNode extends StackPane implements Selectable {

	private Config config = new Config("config");
	
	//information 
	private ActivityElement activityElement;
	private ProcessElementInterface processTab;
	
	private HashMap<RelationConstraintNode, Cursor> cursorMap = new HashMap<RelationConstraintNode, Cursor>();
	
	// Shapes and Buttons
	private Circle clickable;
	private Label identifier;
	private Button deleteButton, addConstraintButton;
	private Image imgDelete;
	private Image imgAddConstraint;
	
	private Shape cardinalityShape;
	private Shape initShape;
	private Shape endShape;
	private Label initLabel = new Label("INIT");
	private Label endLabel = new Label("END");
	private Label cardLabel;
	private ArrayList<LineNode> lineNodes = new ArrayList<LineNode>();
	
	private int existenceConstraintPosition = 1;
	
	//private Polygon insideArrow;
	//private Polygon outsideArrow;
	
	
	private final double arrowHeight = config.getDouble("arrow.height");
	private final double arrowWidth = config.getDouble("arrow.width");
	
	private double radius;
	private int buttonMargin = -5;

	//private Rotate rotateInside;
	//private Rotate rotateOutside;
	

	/**
	 * Creates an acitivityNode based on the given activity and the processTab it was created in.
	 * @param activity - contains information
	 * @param process - processTab where activityNode will be added to
	 */
	public ActivityNode(ActivityElement activity, ProcessElementInterface process) {
		radius= config.getDouble("activity.radius");
		this.activityElement = activity;
		this.processTab = process;
		
		
		//StackPane
		this.setWidth(1);
		this.setHeight(1);
		this.toFront();		
		
		//Circle
		this.clickable = new Circle(radius); 
		this.getChildren().add(clickable);
		
		//Label
		identifier = new Label();
		identifier.setMaxSize(radius * 2, radius);
		identifier.setWrapText(true);
		identifier.setPadding(new Insets(0, 5, 0 , 5));
		identifier.setTextAlignment(TextAlignment.CENTER);
		identifier.setAlignment(Pos.CENTER);
		this.getChildren().add(identifier);
		
		//CardinalityShape
		Circle c = new Circle(radius);
		Rectangle r = new Rectangle(-radius,-radius/2,radius * 2, radius * 1.5);
		cardinalityShape = Shape.subtract(c, r);
		cardinalityShape.setFill(Color.RED);
		//this.getChildren().add(cardinalityShape);
		StackPane.setAlignment(cardinalityShape, Pos.TOP_CENTER);
		cardLabel = new Label("not set");
		//this.getChildren().add(cardLabel);
		StackPane.setAlignment(cardLabel, Pos.TOP_CENTER);
		StackPane.setMargin(cardLabel, new Insets(10,0,0,0));
		
		
		//InitShape	
		Rectangle r2 = new Rectangle(-radius,radius/2,radius,radius/2);
		initShape = Shape.intersect(c, r2);
		//this.getChildren().add(initShape);
		StackPane.setAlignment(initShape, Pos.BOTTOM_LEFT);
		
		
		double angle = Math.asin(-0.5);
		
		double translateX = ((1 - Math.cos(angle)) * radius);// * 0.85;
	
		StackPane.setMargin(initShape, new Insets(0, 0, 0, translateX-1));
		
		//TODO: calculate distance relative

		StackPane.setAlignment(initLabel, Pos.BOTTOM_LEFT);
		StackPane.setMargin(initLabel, new Insets(0, 0, radius*0.2, radius*0.5));
		
		//EndShape
		Rectangle r3 = new Rectangle(0,radius/2,radius,radius/2);
		endShape = Shape.intersect(c, r3);

		StackPane.setAlignment(endShape, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(endShape, new Insets(0, translateX, 0, 0));
		//TODO: calculate distance relative

		StackPane.setAlignment(endLabel, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(endLabel, new Insets(0, radius * 0.5, radius * 0.2, 0));
		
		
		
		
		//Edit Buttons
		deleteButton = new Button("");
		deleteButton.setOnAction((event) -> {if(this.processTab != null) {this.processTab.deleteActivity(this);}});
		//TODO: ENABLE AGAIN
		
		
		
		addConstraintButton = new Button("");
		addConstraintButton.setOnAction((event) -> {
			if(this.processTab != null) {
				this.processTab.setSelectionModeToAddConstraint();
			}
		});
		
		//this.getChildren().addAll(deleteButton, addConstraintButton /*, editButton*/);
		StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
		StackPane.setAlignment(addConstraintButton, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(deleteButton, new Insets(buttonMargin, buttonMargin, 0, 0));
		StackPane.setMargin(addConstraintButton, new Insets(0, buttonMargin, buttonMargin, 0));
		
		//Set Position
		this.setTranslateX(this.activityElement.getPosX());
		this.setTranslateY(this.activityElement.getPosY());
		
		//Set StyleClasses
		this.getStyleClass().add("activityPane");
		identifier.getStyleClass().add("activityLabel");
		cardinalityShape.getStyleClass().add("cardinalityElements");
		initShape.getStyleClass().add("structuringElements");
		endShape.getStyleClass().add("structuringElements");
		initLabel.getStyleClass().add("constraintText");
		endLabel.getStyleClass().add("constraintText");
		cardLabel.getStyleClass().add("constraintText");
		//deleteButton.getStyleClass().add("actionButton");
		
		setEditable(false);
		
		updateNode();
	}
	
	/**
	 * changes visibility of buttons of this Activity to interact with it and changes
	 * appearance of this node 
	 */
	@Override
	public void setEditable(boolean editable){
		deleteButton.setVisible(editable);
		addConstraintButton.setVisible(editable);
		if(editable){
			
			if(!getChildren().contains(deleteButton)){
				imgDelete =  new Image(getClass().getClassLoader().getResource("images/Delete-icon.png").toExternalForm(),12,12,true,true);
				deleteButton.setGraphic(new ImageView(imgDelete));
				
				imgAddConstraint =  new Image(getClass().getClassLoader().getResource("images/Connect-icon.png").toExternalForm(),12,12,true,true);
				addConstraintButton.setGraphic(new ImageView(imgAddConstraint));
				
				getChildren().addAll(deleteButton, addConstraintButton);
			}
			
			clickable.getStyleClass().clear();
			clickable.getStyleClass().add("selectedActivity");
			if(processTab != null) {
				processTab.editActivity(this);
			}
			
		} else {
			if(getChildren().contains(deleteButton)){
				imgDelete = null;
				imgAddConstraint = null;
				getChildren().removeAll(deleteButton, addConstraintButton);
			}
			clickable.getStyleClass().clear();
			clickable.getStyleClass().add("activity");
		}
	}

	/**
	 * updates the visible activityNode to display the saved information of the activity
	 */
	public void updateNode(){
		this.identifier.setText(activityElement.getIdentifier());
		//Set Position
		this.setTranslateX(this.activityElement.getPosX());
		this.setTranslateY(this.activityElement.getPosY());
		updateExistenceConstraint();
		processTab.determineActivities();
		processTab.determineConstraints();
	}
	
	/**
	 * updates the elements of the Existence Constraint on the ActivityNode based on the information of the activityElement
	 */
	private void updateExistenceConstraint() {
		if (activityElement.getExistenceConstraint() != null){
			Card currentCard = activityElement.getExistenceConstraint().getCard();
			if(currentCard != null){
				cardLabel.setText(currentCard.getMin()+ ".." + currentCard.getMax());
				
				if (!getChildren().contains(cardinalityShape)){
					getChildren().add(existenceConstraintPosition, cardinalityShape);
				}
				if (!getChildren().contains(cardLabel)){
					getChildren().add(existenceConstraintPosition + 1, cardLabel);
				}
			} else {
				cardLabel.setText("");
				if (getChildren().contains(cardLabel)){
					getChildren().remove(cardLabel);
				}
				if (getChildren().contains(cardinalityShape)){
					getChildren().remove(cardinalityShape);
				}
			}
			
			if (activityElement.getExistenceConstraint().getStruct() == StructuringElement.INITEND){
				if (!getChildren().contains(initShape)){
					getChildren().add(existenceConstraintPosition, initShape);
				}
				if (!getChildren().contains(initLabel)){
					getChildren().add(existenceConstraintPosition + 1, initLabel);
				}
				if (!getChildren().contains(endShape)){
					getChildren().add(existenceConstraintPosition, endShape);
				}
				if (!getChildren().contains(endLabel)){
					getChildren().add(existenceConstraintPosition + 1, endLabel);
				}
			}
			else {
				if(activityElement.getExistenceConstraint().getStruct() == StructuringElement.INIT){
					if (!getChildren().contains(initShape)){
						getChildren().add(existenceConstraintPosition, initShape);
					}
					if (!getChildren().contains(initLabel)){
						getChildren().add(existenceConstraintPosition + 1, initLabel);
					}
				} else { 
					if (getChildren().contains(initShape)){
						getChildren().remove(initShape);
					}
					if (getChildren().contains(initLabel)){
						getChildren().remove(initLabel);
					}
					}
				
				if(activityElement.getExistenceConstraint().getStruct() == StructuringElement.END){
					if (!getChildren().contains(endShape)){
						getChildren().add(existenceConstraintPosition, endShape);
					}
					if (!getChildren().contains(endLabel)){
						getChildren().add(existenceConstraintPosition + 1, endLabel);
					}
				} else { 
					if (getChildren().contains(endShape)){
						getChildren().remove(endShape);
					}
					if (getChildren().contains(endLabel)){
						getChildren().remove(endLabel);
					}
				}
			}
		} else {
			if (getChildren().contains(cardLabel)){
				getChildren().remove(cardLabel);
			}
			if (getChildren().contains(cardinalityShape)){
				getChildren().remove(cardinalityShape);
			}
			if (getChildren().contains(initShape)){
				getChildren().remove(initShape);
			}
			if (getChildren().contains(initLabel)){
				getChildren().remove(initLabel);
			}
			if (getChildren().contains(endShape)){
				getChildren().remove(endShape);
			}
			if (getChildren().contains(endLabel)){
				getChildren().remove(endLabel);
			}
		}
	}

	public void updateActivityElement() {
		activityElement.setPosition(getTranslateX(), getTranslateY());
	}


	public void setClickable(Circle clickable) {
		this.clickable = clickable;
	}

	public ActivityElement getActivityElement() {
		return activityElement;
	}

	public void setActivity(ActivityElement activity) {
		this.activityElement = activity;
	}
	
	public XMLExistenceConstraint getExistenceConstraint() {
		return activityElement.getExistenceConstraint();
	}

	public void setExistenceConstraint(XMLExistenceConstraint existenceConstraint) {
		activityElement.setExistenceConstraint(existenceConstraint);
	}

	public Button getDeleteButton(){
		return this.deleteButton;
	}

	@Override
	public double getPosX() {
		return getTranslateX();
	}

	@Override
	public double getPosY() {
		return getTranslateY();
	}
	
	/**
	 * Creates and displays cursors of a RelationConstraint. Should be called whenever the template of a constraint is changed.
	 * @param type - defines the styling of the cursors
	 * @param parameterNumber - is needed to know on what side of the constraint this Node is
	 */
	public void updateCursorStyling(Template type, int parameterNumber, RelationConstraintNode cNode) {
		
		// Cursor
		Cursor cursor = cursorMap.get(cNode);
		if (cursor != null){
		Polygon insideArrow = cursor.insideCursor;
		Polygon outsideArrow = cursor.outsideCursor;
			if(parameterNumber == 1){
				if (type.getP1InsideCursor()){
					if (!getChildren().contains(insideArrow)){
						getChildren().add(insideArrow);
					}
				} else {
					if (getChildren().contains(insideArrow)){
						getChildren().remove(insideArrow);
					}
				}
				
				if (type.getP1OutsideCursor()) {
					if (!getChildren().contains(outsideArrow)) {
						getChildren().add(outsideArrow);
					}
				} else {
					if (getChildren().contains(outsideArrow)) {
						getChildren().remove(outsideArrow);
					}
				}
			} else  if (parameterNumber == 2) {
				if (type.getP2InsideCursor()){
					if (!getChildren().contains(insideArrow)){
						getChildren().add(insideArrow);
					}
				} else {
					if (getChildren().contains(insideArrow)){
						getChildren().remove(insideArrow);
					}
				}
				
				if (type.getP2OutsideCursor()) {
					if (!getChildren().contains(outsideArrow)) {
						getChildren().add(outsideArrow);
					}
				} else {
					if (getChildren().contains(outsideArrow)) {
						getChildren().remove(outsideArrow);
					}
				}
			}
			
			//Negation
			if(type.getNegation()){
				outsideArrow.getStyleClass().clear();
				outsideArrow.getStyleClass().add("negArrow");
				insideArrow.getStyleClass().clear();
				insideArrow.getStyleClass().add("negArrow");
			} else {
				outsideArrow.getStyleClass().clear();
				outsideArrow.getStyleClass().add("arrow");
				insideArrow.getStyleClass().clear();
				insideArrow.getStyleClass().add("arrow");
			}
		}
	}

	public void updateCursorPosition(double angle, RelationConstraintNode cNode) {
		Cursor cursor = cursorMap.get(cNode);
		if (cursor != null){
			cursor.insideRotate.setAngle(angle - 180);
			cursor.outsideRotate.setAngle(angle - 180);
		}
	}
	
	/**
	 * nested Cursor Class to manage Cursors on top of an ActivityNode StackPane
	 * @author Lukas
	 *
	 */
	public class Cursor{
		private Polygon insideCursor;
		private Polygon outsideCursor;
		private Rotate insideRotate = new Rotate(0, arrowWidth - radius ,arrowHeight/2);
		private Rotate outsideRotate= new Rotate(0, -radius, arrowHeight/2);
		
		/**
		 * 
		 * @param cNode
		 */
		Cursor(){	
			//Cursors
			outsideCursor = new Polygon();
			outsideCursor.getPoints().addAll(new Double[]{
	            0.0, 0.0,
	            arrowWidth, arrowHeight/2,
	            0.0, arrowHeight });
			StackPane.setAlignment(outsideCursor, Pos.CENTER_RIGHT);
			StackPane.setMargin(outsideCursor, new Insets(0, (-1) * (arrowWidth), 0, 0));

	        
			insideCursor = new Polygon();
			insideCursor.getPoints().addAll(new Double[]{
	            arrowWidth, 0.0,
	            0.0, arrowHeight/2,
	            arrowWidth, arrowHeight });
			StackPane.setAlignment(insideCursor, Pos.CENTER_RIGHT);

			
			outsideCursor.getTransforms().add(outsideRotate);
			insideCursor.getTransforms().add(insideRotate);			
		}

		public Polygon getInsideCursor() {
			return insideCursor;
		}

		public Polygon getOutsideCursor() {
			return outsideCursor;
		}

		public Rotate getInsideRotate() {
			return insideRotate;
		}

		public Rotate getOutsideRotate() {
			return outsideRotate;
		}
	
	}

	/**
	 * creates and adds CursorsObject to the HashMap of cursors
	 * @param constraintNode
	 */
	public void addCursor(RelationConstraintNode constraintNode) {
		cursorMap.put(constraintNode, new Cursor());
	}

	public void removeCursor(RelationConstraintNode constraintNode) {
		Cursor cursor = cursorMap.get(constraintNode);
		
		if (getChildren().contains(cursor.insideCursor)){
			getChildren().remove(cursor.insideCursor);
		}
		if (getChildren().contains(cursor.outsideCursor)){
			getChildren().remove(cursor.outsideCursor);
		}
		
		cursorMap.remove(constraintNode);
		
	}
	
	public Cursor getCursorByConstraint(RelationConstraintNode constraintNode){
		return cursorMap.get(constraintNode);
	}
	
	/**
	 * Method to hide all cursor for SVG export, because of rotation + translation bug
	 * @param visible
	 */
	public void setCursorsInvisibleForExport(boolean visible){
		for(Cursor cursor : cursorMap.values()){
			cursor.insideCursor.setVisible(!visible);
			cursor.outsideCursor.setVisible(!visible);
		}
	}
	
	/**
	 * adds a LineNode to 
	 * @param newLine
	 */
	public void addLineNode(LineNode newLine) {
		lineNodes.add(newLine);
		RelationConstraintNode rcNode = processTab.determineRelationConstraintNode(newLine.getConstraintElement());
		
		addCursor(rcNode);
		rcNode.updateAfterChanges();
		newLine.updateLineElementsAfterChanges();
	}
	
	public void removeLineNode(LineNode removedLine){
		lineNodes.remove(removedLine);
		RelationConstraintNode rcNode = processTab.determineRelationConstraintNode(removedLine.getConstraintElement());
		removeCursor(rcNode);
		removedLine.updateLineElementsAfterChanges();
	}


	public void updateAllLineNodePositions() {
		for (LineNode line : lineNodes){
			line.updateLinePosition();
		}
		
	}
	
	/**
	 * searches for the LineNode that connects this activity to the given relation Constraint
	 * @param relationConstraintElement
	 * @return
	 */
	public LineNode getLineNodeByConstraint(RelationConstraintElement relationConstraintElement) {
		for(LineNode line : lineNodes){
			if(line.getConstraintElement() == relationConstraintElement){
				return line;
			}
		}
		return null;
	}
	
}

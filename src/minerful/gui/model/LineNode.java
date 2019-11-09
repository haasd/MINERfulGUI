package minerful.gui.model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.util.Config;

/**
 * LineNodes is used to display one of the 2 line that are needed for one constraint. 
 * The Pane is a horizontal Pane which is changed in length and rotated according to
 * the position of both activities.
 * It is based on a StackPane which contains the following elements:
 * 		Line - solid or dashed
 * 		AlternateLabel - for Alternate or Negation constraints
 * 
 * @author Lukas
 *
 */
public class LineNode extends StackPane {
	
	private int parameterNumber;
	private Config config = new Config("config");
	
	private RelationConstraintNode cNode;
	private ActivityNode aNode;
	private Line line;
	private Label constraintLabel;
	
	private final double activityRadius = config.getDouble("activity.radius");
	private final double constraintRadius = config.getDouble("constraint.radius");
	
	private final double linePaneHeight = config.getDouble("line.pane.height");
	
	private final double arrowWidth = config.getDouble("arrow.width");
	
	private String invisibleClass ="invisible";

	private Rotate rotate;
	private boolean renderingOptimization = config.getBoolean("optimize.rendering");
	

	private String newNegativeStyle = "dashedline";
	private String oldNegativeStyle = "dashedline";
	private String normalStyle = "line";
	private Double strokeWidth = 1d;

	/**
	 * Creates a Line with Labels and Arrows
	 */
	public LineNode(RelationConstraintNode c, ActivityNode a, int parameterNumber){
		
		this.cNode = c;
		this.aNode = a;
		this.parameterNumber = parameterNumber;
		
		
				
		this.getStyleClass().add("linePane");
		
		line = new Line();
		this.getChildren().add(line);
		line.setStrokeWidth(1);
		constraintLabel = new Label("I");
		constraintLabel.getStyleClass().add("alternateLabel");
		StackPane.setAlignment(constraintLabel, Pos.TOP_RIGHT);
		StackPane.setMargin(constraintLabel, new Insets(0, arrowWidth,0,0));
		
		rotate = new Rotate(0, 0 ,linePaneHeight/2);
		getTransforms().add(rotate);
		
		this.setPrefHeight(linePaneHeight);

	}
	
	/**
	 * calculates the angle of two points with one fixed Point (center of Constraint)
	 * @return angle in degree
	 */
	private double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y, double point2X, double point2Y, double fixedX, double fixedY) {

	    double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
	    double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

	    return angle1 - angle2; 
	}
	
	/**
	 * changes length and angle of LineNode 
	 */
	public void updateLinePosition(){

		
		double cCenterX = cNode.getTranslateX() + constraintRadius;
		double cCenterY = cNode.getTranslateY() + constraintRadius;
		
		double aCenterX = aNode.getTranslateX() + activityRadius;
		double aCenterY = aNode.getTranslateY() + activityRadius;

		
		double distanceFromCenterToCenter = Math.sqrt(Math.pow(aCenterX - cCenterX, 2) + Math.pow(aCenterY - cCenterY, 2));
		
		//calculate the angle which the line to the activity has to be rotated at the center of the constraint 	
		double angle = Math.toDegrees(angleBetweenTwoPointsWithFixedPoint(aCenterX, aCenterY, cCenterX + distanceFromCenterToCenter, cCenterY, cCenterX, cCenterY));
			
		rotate.setAngle(angle);
		aNode.updateCursorPosition(angle, cNode);

		this.setTranslateX(cCenterX);
		this.setTranslateY(cCenterY - linePaneHeight/2);
		
		double paneWidth = Math.max(distanceFromCenterToCenter - activityRadius, 0);
		this.setPrefWidth(paneWidth);
				
		line.setStartX(0);
		line.setStartY(linePaneHeight/2);
		line.setEndX(paneWidth);
		line.setEndY(linePaneHeight/2);
			
	}
	
	/**
	 * changes visibility of elements based on information of the constraint 
	 * in ConstraintNode. Constraint has to be updated before.
	 */
	public void updateLineElementsAfterChanges(){
		Template type = cNode.getConstraintElement().getTemplate();
		
		// Cursor
		aNode.updateCursorStyling(type, parameterNumber, cNode);
		

		//Negation
		line.getStyleClass().clear();
		if(type.getNegation()){
			if (renderingOptimization){
				constraintLabel.setText("");
				//constraintLabel.getStyleClass().add(lineOptimizedStyle);
				if (!getChildren().contains(constraintLabel)){
					this.getChildren().add(constraintLabel);
				}
				line.getStyleClass().add(newNegativeStyle);
				
			} 
			else {
				if (getChildren().contains(constraintLabel)){
					this.getChildren().remove(constraintLabel);
				}
				line.getStyleClass().add(oldNegativeStyle);
			}
			
		} else {
			line.getStyleClass().add(normalStyle);
			// Alternation
			
			if (parameterNumber == 1){
				if(type.getP1Alternation() == true){
					if (!getChildren().contains(constraintLabel)){
						constraintLabel.setText("I");
						this.getChildren().add(constraintLabel);
					}
					StackPane.setAlignment(constraintLabel, Pos.BOTTOM_RIGHT);				
				} else {
					if (getChildren().contains(constraintLabel)){
						this.getChildren().remove(constraintLabel);
					}
				}
			} else if (parameterNumber == 2){
				if(type.getP2Alternation() == true){
					if (!getChildren().contains(constraintLabel)){
						constraintLabel.setText("I");
						this.getChildren().add(constraintLabel);
					}
					StackPane.setAlignment(constraintLabel, Pos.TOP_RIGHT);
				} else {
					if (getChildren().contains(constraintLabel)){
						this.getChildren().remove(constraintLabel);
					}
				}
			}
		}

		//Chained
		if(type.getChained()){

			cNode.getCicrle().getStyleClass().add(invisibleClass);
			cNode.getAsteriskLabel().getStyleClass().add(invisibleClass);
		} else {
			cNode.getCicrle().getStyleClass().remove(invisibleClass);
			cNode.getAsteriskLabel().getStyleClass().remove(invisibleClass);
		}
		
		updateLinePosition();
	}
	
	public void updateConnection(ActivityNode node, RelationConstraintNode constraintNode) {
		this.aNode = node;
		this.cNode = constraintNode;
	}
	

	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
	}


	public Label getAlternativeLabel() {
		return constraintLabel;
	}

	public void setAlternativeLabel(Label alternativeLabel) {
		this.constraintLabel = alternativeLabel;
	}



	public RelationConstraintElement getConstraintElement() {
		return cNode.getConstraintElement();
	}
	
	public ActivityElement getActivityElement(){
		return aNode.getActivityElement();
	}
	
	public void setRenderingOptimization(boolean renderingOptimization) {
		this.renderingOptimization = renderingOptimization;
	}
	
	public Double getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(Double strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

}

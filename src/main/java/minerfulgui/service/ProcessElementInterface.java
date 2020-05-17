package minerfulgui.service;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import minerful.concept.ProcessModel;
import minerfulgui.model.ActivityElement;
import minerfulgui.model.ActivityNode;
import minerfulgui.model.ProcessElement;
import minerfulgui.model.RelationConstraintElement;
import minerfulgui.model.RelationConstraintNode;
import minerfulgui.model.ZoomableScrollPane;

public interface ProcessElementInterface {
	
	public List<ActivityNode> activityNodes = new ArrayList<>();
	public List<RelationConstraintNode> constraintNodes = new ArrayList<>();
	
	public RelationConstraintNode determineRelationConstraintNode(RelationConstraintElement relationElement);
	public ActivityNode determineActivityNode(ActivityElement activityElement);
	public void setSelectionModeToAddConstraint();
	public void deleteActivity(ActivityNode aNode);
	public void editActivity(ActivityNode activityNode);
	public void determineConstraints();
	public void determineActivities();
	public List<RelationConstraintNode> getConstraintNodes();
	public AnchorPane getAnchorPane();
	public ProcessElement getCurrentProcessElement();
	public ProcessModel getCurrentProcessModel();
	public BorderPane getBackgroundPane();
	public ZoomableScrollPane getScrollPane();
	public void setMaxTranslate();
	public DoubleProperty getMaxTranslateY();
	public DoubleProperty getMaxTranslateX();
	public List<ActivityNode> getActivityNodes();
	public boolean isParamsStylingActive();
}

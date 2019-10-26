package minerful.gui.service;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintNode;

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
	public BorderPane getBackgroundPane();

}

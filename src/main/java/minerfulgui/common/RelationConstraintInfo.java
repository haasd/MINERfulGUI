package minerfulgui.common;

public class RelationConstraintInfo {
	
	private String activitySource;
	private String activityTarget;
	private String constraintTemplate;
	
	public RelationConstraintInfo(String activitySource, String activityTarget, String constraintTemplate) {
		super();
		this.activitySource = activitySource;
		this.activityTarget = activityTarget;
		this.constraintTemplate = constraintTemplate;
	}
	
	
	public String getActivitySource() {
		return activitySource;
	}
	public void setActivitySource(String activitySource) {
		this.activitySource = activitySource;
	}
	public String getActivityTarget() {
		return activityTarget;
	}
	public void setActivityTarget(String activityTarget) {
		this.activityTarget = activityTarget;
	}
	public String getConstraintTemplate() {
		return constraintTemplate;
	}
	public void setConstraintTemplate(String constraintTemplate) {
		this.constraintTemplate = constraintTemplate;
	}
	
	

}

package minerfulgui.model;

public enum ExistenceConstraintEnum {
	PARTICIPATION("Participation"),
	AT_MOST_ONE("AtMostOne"),
	INIT("Init"),
	END("End");
	
	private String templateLabel;
	
	ExistenceConstraintEnum(String templateLabel){
		this.templateLabel = templateLabel;
	}
	
	public String getTemplateLabel() {
		return templateLabel;
	}
}
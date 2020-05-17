package minerfulgui.model;

public class StructureElement extends ConstraintElement {

	private static final long serialVersionUID = 3798862660181954186L;
	private boolean isActive = false;

	public StructureElement(boolean isActive) {
		super();
		this.isActive = isActive;
	}
	
	public StructureElement(boolean isActive, double support, double confidence, double interest) {
		super(support,confidence, interest);
		
		this.isActive = isActive;
	}
	
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	

}

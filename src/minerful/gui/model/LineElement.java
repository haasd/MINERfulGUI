package minerful.gui.model;

public class LineElement extends ConstraintElement {
	
	ActivityElement sourceElement;
	ActivityElement targetElement;
	
	
	
	public LineElement(ActivityElement sourceElement, ActivityElement targetElement) {
		super();
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;
	}
	
	public LineElement(ActivityElement sourceElement, ActivityElement targetElement, double support, double confidence, double interest) {
		super(support, confidence, interest);
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;
	}
	
	public ActivityElement getSourceElement() {
		return sourceElement;
	}
	public void setSourceElement(ActivityElement sourceElement) {
		this.sourceElement = sourceElement;
	}
	public ActivityElement getTargetElement() {
		return targetElement;
	}
	public void setTargetElement(ActivityElement targetElement) {
		this.targetElement = targetElement;
	}
	
	

}

package minerful.gui.model;

import java.io.Serializable;

public abstract class ConstraintElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6446864208169828877L;
	private double support = 1d;
	private double interest = 1d;
	private double confidence = 1d;
	
	public ConstraintElement() {
		super();
	}

	public ConstraintElement(double support, double interest, double confidence) {
		super();
		this.support = support;
		this.interest = interest;
		this.confidence = confidence;
	}
	
	public double getSupport() {
		return support;
	}
	public void setSupport(double support) {
		this.support = support;
	}
	public double getInterest() {
		return interest;
	}
	public void setInterest(double interest) {
		this.interest = interest;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	
}

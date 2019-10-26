package minerful.gui.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import minerful.concept.constraint.Constraint;

public class Card extends ConstraintElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6073788820411271600L;
	private String min;	// DataType String to allow "*" 
	private String max;
	
	/**
	 * Cardinality Constructor for JAXB
	 */
	public Card(){}
	
	/**
	 * Cardinality Constructor to create valid cardinalities
	 * @param min - String of positive Integers and * (not null)
	 * @param max - String of positive Integers and * (not null)
	 */
	public Card(String min, String max){
		this.min = min;
		this.max = max;
	}
	
	public Card(String min, String max, double support, double confidence, double interest){
		super(support, confidence, interest);
		this.min = min;
		this.max = max;
	}
	
	@XmlAttribute(required=true)
	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}
	@XmlAttribute(required=true)
	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}
	

	

}

package minerfulgui.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import minerful.concept.constraint.Constraint;

public class Card implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6073788820411271600L;
	private CardinalityElement min = new CardinalityElement("0");	// DataType String to allow "*" 
	private CardinalityElement max = new CardinalityElement("*");
	
	/**
	 * Cardinality Constructor for JAXB
	 */
	public Card(){}
	
	/**
	 * Cardinality Constructor to create valid cardinalities
	 * @param min - String of positive Integers and * (not null)
	 * @param max - String of positive Integers and * (not null)
	 */
	public Card(CardinalityElement min, CardinalityElement max){
		this.min = min;
		this.max = max;
	}
	
	@XmlAttribute(required=true)
	public CardinalityElement getMin() {
		return min;
	}

	public void setMin(CardinalityElement min) {
		this.min = min;
	}
	@XmlAttribute(required=true)
	public CardinalityElement getMax() {
		return max;
	}

	public void setMax(CardinalityElement max) {
		this.max = max;
	}
	

	

}

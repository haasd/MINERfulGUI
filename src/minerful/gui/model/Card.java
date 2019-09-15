package minerful.gui.model;

import javax.xml.bind.annotation.XmlAttribute;

public class Card {
	
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

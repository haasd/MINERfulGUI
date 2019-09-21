package minerful.gui.model.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import minerful.gui.model.Card;
import minerful.gui.model.StructuringElement;

public class XMLExistenceConstraint implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5243357124521361465L;
	private Integer activityID;
	private Card card;						// optional: may choose from PARTICIPATION and ATMOSTON
	private StructuringElement struct;					// optional: may choose from INIT and ENDE
	
	/**
	 * constructor to create Existence Constraints from XML File while loading
	 */
	public XMLExistenceConstraint(){};
	
	/**
	 * constructor for creating new Existence Constraints from UI
	 * @param activityID
	 */
	public XMLExistenceConstraint(Integer activityID, Card card, StructuringElement struct){
		this.activityID = activityID;
		this.card = card;
		this.struct = struct;
	}
	
	@XmlAttribute(required=true)
	public Integer getActivityID() {
		return activityID;
	}

	public void setActivityID(Integer activityID) {
		this.activityID = activityID;
	}

	@XmlElement(name="card")
	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}
	
	@XmlElement(name="struct")
	public StructuringElement getStruct() {
		return struct;
	}

	public void setStruct(StructuringElement struct) {
		this.struct = struct;
	}
}

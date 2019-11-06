package minerful.gui.model.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import minerful.gui.model.Card;
import minerful.gui.model.StructureElement;

public class XMLExistenceConstraint implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5243357124521361465L;
	private Integer activityID;
	private Card card = new Card();						// optional: may choose from PARTICIPATION and ATMOSTON
	private StructureElement initConstraint = new StructureElement(false);
	private StructureElement endConstraint = new StructureElement(false);
	
	/**
	 * constructor to create Existence Constraints from XML File while loading
	 */
	public XMLExistenceConstraint(){};
	
	/**
	 * constructor for creating new Existence Constraints from UI
	 * @param activityID
	 */
	public XMLExistenceConstraint(Integer activityID, Card card, StructureElement initConstraint, StructureElement endConstraint){
		this.activityID = activityID;
		this.card = card;
		this.initConstraint = initConstraint;
		this.endConstraint = endConstraint;
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

	public StructureElement getInitConstraint() {
		return initConstraint;
	}

	public void setInitConstraint(StructureElement initConstraint) {
		this.initConstraint = initConstraint;
	}

	public StructureElement getEndConstraint() {
		return endConstraint;
	}

	public void setEndConstraint(StructureElement endConstraint) {
		this.endConstraint = endConstraint;
	}
	
	
}

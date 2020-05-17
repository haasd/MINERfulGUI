package minerfulgui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class XMLRelationConstraintWrapper {

private ArrayList<XMLRelationConstraint> relationConstraints = new ArrayList<XMLRelationConstraint>();
	
	public XMLRelationConstraintWrapper(){}
	
	public XMLRelationConstraintWrapper(ArrayList<XMLRelationConstraint> relationConstraints){
		this.relationConstraints = relationConstraints;		
	}
	
	@XmlElement(name="relationConstraint")
	public ArrayList<XMLRelationConstraint> getList() {
		return relationConstraints;
	}
	public void setRelationConstraints(ArrayList<XMLRelationConstraint> relationConstraints) {
		this.relationConstraints = relationConstraints;
	}	
}

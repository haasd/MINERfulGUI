package minerfulgui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class XMLExistenceConstraintWrapper {

private ArrayList<XMLExistenceConstraint> existenceConstraints = new ArrayList<XMLExistenceConstraint>();
	
	public XMLExistenceConstraintWrapper(){}
	
	public XMLExistenceConstraintWrapper(ArrayList<XMLExistenceConstraint> existenceConstraints){
		this.existenceConstraints = existenceConstraints;		
	}
	
	@XmlElement(name="existenceConstraint")
	public ArrayList<XMLExistenceConstraint> getList() {
		return existenceConstraints;
	}
	public void setExistenceConstraint(ArrayList<XMLExistenceConstraint> existenceConstraints) {
		this.existenceConstraints = existenceConstraints;
	}	
}

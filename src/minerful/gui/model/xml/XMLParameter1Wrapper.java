package minerful.gui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class XMLParameter1Wrapper {
	
	private ArrayList<Integer> parameter1Ids = new ArrayList<Integer>();
	
	public XMLParameter1Wrapper(){
		
	}

	public XMLParameter1Wrapper(ArrayList<Integer> activationIds){
		this.parameter1Ids = activationIds;
	}
	
	@XmlElement(name="activityID")
	public ArrayList<Integer> getParameter1Ids() {
		return parameter1Ids;
	}

	public void setActivationIds(ArrayList<Integer> activationIds) {
		this.parameter1Ids = activationIds;
	}
	
	
}

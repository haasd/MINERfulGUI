package minerful.gui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class XMLParameter2Wrapper {
	
	private ArrayList<Integer> parameter2Ids = new ArrayList<Integer>();
	
	public XMLParameter2Wrapper(){
		
	}

	public XMLParameter2Wrapper(ArrayList<Integer> parameter2Ids){
		this.parameter2Ids = parameter2Ids;
	}
	
	@XmlElement(name="activityID")
	public ArrayList<Integer> getParameter2Ids() {
		return parameter2Ids;
	}

	public void setParameter2Ids(ArrayList<Integer> Parameter2Ids) {
		this.parameter2Ids = Parameter2Ids;
	}
	
	
}

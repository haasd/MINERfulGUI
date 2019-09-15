package minerful.gui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class XMLActivityWrapper{
	
	private ArrayList<XMLActivity> activities = new ArrayList<XMLActivity>();
	
	public XMLActivityWrapper(){}
	
	public XMLActivityWrapper(ArrayList<XMLActivity> activities){
		this.activities = activities;		
	}
	
	@XmlElement(name="activity")
	public ArrayList<XMLActivity> getList() {
		return activities;
	}
	public void setActivities(ArrayList<XMLActivity> activities) {
		this.activities = activities;
	}	
		
	
}

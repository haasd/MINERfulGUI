package minerful.gui.model.xml;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="process")							// RootElement of XML File is ClassName
public class XMLProcess {

	private final static File fileName = new File("process.xml");
	private final static File templateName = new File("templates.xml");
	
	private XMLActivityWrapper activityWrapper = new XMLActivityWrapper();
	private XMLExistenceConstraintWrapper existenceConstraintWrapper = new XMLExistenceConstraintWrapper();
	private XMLRelationConstraintWrapper relationConstraintWrapper = new XMLRelationConstraintWrapper();
	
	public XMLProcess(){}
		
		
	public static File getFileName() {
		return fileName;
	}
	
	public static File getTemplateFileName(){
		return templateName;
	}
	
	public XMLProcess(ArrayList<XMLActivity> activities, ArrayList<XMLRelationConstraint> relationConstraints, ArrayList<XMLExistenceConstraint> existenceConstraints) {
		super();
		this.activityWrapper = new XMLActivityWrapper(activities);
		this.relationConstraintWrapper = new XMLRelationConstraintWrapper(relationConstraints);
		this.existenceConstraintWrapper = new XMLExistenceConstraintWrapper(existenceConstraints);
	}
	
	@XmlElement(name="activities")
	public XMLActivityWrapper getActivityWrapper() {
		return activityWrapper;
	}

	public void setActivityWrapper(XMLActivityWrapper activityWrapper) {
		this.activityWrapper = activityWrapper;
	}
	
	@XmlElement(name="relationConstraints")
	public XMLRelationConstraintWrapper getRelationConstraintWrapper() {
		return relationConstraintWrapper;
	}
	public void setRelationConstraintWrapper(XMLRelationConstraintWrapper constraints) {
		this.relationConstraintWrapper = constraints;
	}
	
	@XmlElement(name="existenceConstraints")
	public XMLExistenceConstraintWrapper getExistenceConstraintWrapper() {
		return existenceConstraintWrapper;
	}
	public void setExistenceConstraintWrapper(XMLExistenceConstraintWrapper constraints) {
		this.existenceConstraintWrapper = constraints;
	}
	
	@XmlTransient
	public ArrayList<XMLActivity> getActivities(){
		return activityWrapper.getList();
	}
	
	@XmlTransient
	public ArrayList<XMLRelationConstraint> getRelationConstraints(){
		return relationConstraintWrapper.getList();
	}
	
	@XmlTransient
	public ArrayList<XMLExistenceConstraint> getExistenceConstraints(){
		return existenceConstraintWrapper.getList();
	}
	/*
	public String toString(){
		String activityList = "";
		String constraintList = "";
		if(activity != null){
			for (Activity a : activity){
				activityList += a.toString();
			}
		}
		if (constraint != null){
			for (Constraint c  : constraint){
				constraintList += c.toString();
			}
		}
		return activityList + constraintList;
	}
	*/
}



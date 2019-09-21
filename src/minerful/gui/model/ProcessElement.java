package minerful.gui.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import minerful.gui.model.xml.XMLExistenceConstraint;

public class ProcessElement implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3686562242357489964L;

	private File filename;

	private ArrayList<RelationConstraintElement> relationConstraintElementList;
	private ArrayList<ActivityElement> activityElementList;
	private ArrayList<Template> templateList;

	private int maxActivityID = 0;
	private int maxConstraintID = 0;

	/**
	 * creates empty ProcessElement and loads default constraint templates
	 */
	public ProcessElement() {

		relationConstraintElementList = new ArrayList<RelationConstraintElement>();
		activityElementList = new ArrayList<ActivityElement>();
		this.templateList = new ArrayList<Template>();
		
		loadDefaultTemplates();

	}

	public List<RelationConstraintElement> getConstraintEList() {
		return relationConstraintElementList;
	}

	public List<ActivityElement> getActivityEList() {
		return activityElementList;
	}

	public ActivityElement getActivityById(Integer id) {
		for (ActivityElement i : this.activityElementList) {
			if (i.getId() == id) {
				return i;
			}
		}
		return null;
	}

	public RelationConstraintElement getRelationConstraintById(Integer id) {

		for (RelationConstraintElement cElem : relationConstraintElementList) {
			if (cElem.getId() == id) {
				return cElem;
			}
		}
		return null;
	}

	public void addActivity(ActivityElement aElement) {
		activityElementList.add(aElement);
	}

	public void deleteActivity(ActivityElement deletedElement) {
		activityElementList.remove(deletedElement);
	}

	/**
	 * adds a relationConstraint to the processElementList and to the process
	 */
	public void addRelationConstraint(RelationConstraintElement cElement) {
		relationConstraintElementList.add(cElement);
	}

	public void deleteRelationConstraint(RelationConstraintElement deletedConstraintNode) {
		// remove constraints from constraintList of adjacent Activities
		for (ActivityElement aElement : deletedConstraintNode.getParameter1Elements()) {
			aElement.getConstraintList().remove(deletedConstraintNode);
		}
		for (ActivityElement aElement : deletedConstraintNode.getParameter2Elements()) {
			aElement.getConstraintList().remove(deletedConstraintNode);
		}
		// remove constraint from process
		relationConstraintElementList.remove(deletedConstraintNode);
	}

	/**
	 * sets field MaxConstraintID to the highest value of existing constraint
	 * IDs
	 */
	public void setMaxConstraintID() {
		for (RelationConstraintElement c : relationConstraintElementList) {
			int tmpMax = maxConstraintID;
			if (c.getId() > tmpMax) {
				tmpMax = c.getId();
			}
			maxConstraintID = tmpMax;
		}
	}

	/**
	 * sets field MaxCActivityID to the highest value of existing activity IDs
	 */
	public void setMaxActivityID() {
		for (ActivityElement a : activityElementList) {
			int tmpMax = maxActivityID;
			if (a.getId() > tmpMax) {
				tmpMax = a.getId();
			}
			maxActivityID = tmpMax;
		}
	}

	public Integer getMaxConstraintId() {
		return ++maxConstraintID;
	}

	public Integer getMaxActivityID() {
		return ++maxActivityID;
	}

	public File getFileName() {
		return filename;
	}

	public void setFileName(File fileName) {
		filename = fileName;
	}

	public void addExistenceConstraint(ActivityElement aNode) {
		XMLExistenceConstraint eConstraint = new XMLExistenceConstraint(aNode.getId(), null, null);
		aNode.setExistenceConstraint(eConstraint);
	}

	/**
	 * injects a list of ActivityElements into empty ProcessElement. Should be
	 * used only during loading process of existing consistent Files.
	 * 
	 * @param activities
	 */
	public void setActivityList(ArrayList<ActivityElement> activities) {
		this.activityElementList = activities;

	}

	/**
	 * injects a list of RelationCosntraintElements into empty ProcessElement.
	 * Should be used only during loading process of existing consistent Files.
	 * 
	 * @param relationConstraints
	 */
	public void setRelationConstraintList(ArrayList<RelationConstraintElement> relationConstraints) {
		this.relationConstraintElementList = relationConstraints;

	}

	public ArrayList<Template> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(ArrayList<Template> templateList) {
		this.templateList = templateList;
	}
	
	public Template getTemplateByName(String name){
		for(Template t : templateList){
			if(name.toLowerCase().equals(t.getName().toLowerCase())){
				return t;
			}
		}
		return null; // Template not found
	}

	/**
	 * loads default definition of templates. has to be called when no template list is loaded from file
	 */
	public void loadDefaultTemplates() {
		templateList.add(new Template("precedence", false, false, true, false, false, false, false, false));
		templateList.add(new Template("respondedExistence", true, true, false, false, false, false, false, false));
		templateList.add(new Template("coExistence", true, true, true, true, false, false, false, false));
		templateList.add(new Template("response", false, true, false, false, false, false, false, false));
		templateList.add(new Template("alternateResponse", false, true, false, false, true, false, false, false));
		templateList.add(new Template("alternatePrecedence", false, false, true, false, false, true, false, false));
		templateList.add(new Template("chainResponse", false, true, false, false, false, false, true, false));
		templateList.add(new Template("chainPrecedence", false, false, true, false, false, false, true, false));
		templateList.add(new Template("succession", false, true, true, false, false, false, false, false));
		templateList.add(new Template("alternateSuccession", false, true, true, false, true, true, false, false));
		templateList.add(new Template("chainSuccession", false, true, true, false, false, false, true, false));
		templateList.add(new Template("notSuccession", false, true, true, false, false, false, false, true));
		templateList.add(new Template("notChainSuccession", false, true, true, false, false, false, true, true));
		templateList.add(new Template("notCoExistence", true, true, true, true, false, false, false, true));
	}
	
	public Object clone() throws CloneNotSupportedException{  
		return super.clone();  
	}  
}

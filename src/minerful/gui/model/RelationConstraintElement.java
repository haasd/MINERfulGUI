package minerful.gui.model;

import java.util.ArrayList;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * This class holds all information that is needed to draw a new Node and to save it. Every change that is being made by the GUI should be applied to here to always have the latest information.
 * 
 * @author Lukas
 *
 */
public class RelationConstraintElement {
	
	//private final static Logger logger = Logger.getLogger(RelationConstraintElement.class);
	
	//Information
	private Integer id;
	private Template template;
	private Double posX;
	private Double posY;
	private boolean positionFixed = false;
	
	//connectedElements and Nodes
	private ArrayList<LineNode> parameter1Lines = new ArrayList<LineNode>();
	private ArrayList<LineNode> parameter2Lines = new ArrayList<LineNode>();
	private ArrayList<ActivityElement> parameter1Elements = new ArrayList<ActivityElement>();
	private ArrayList<ActivityElement> parameter2Elements = new ArrayList<ActivityElement>();
	private RelationConstraintNode constraintNode;
		
	/**
	 * Basic constructor which defines the information of the RelationConstraint.
	 * The Constructor does not set the connected Elements!
	 * @param id - relevant for saving and loading
	 * @param template - defines how the relationConstraintNode and LineNodes are displayed
	 */
	public RelationConstraintElement(Integer id, Template template) {
		super();
		this.id = id;
		this.posX = 200.0;
		this.posY = 200.0;
		this.template = template;
	}

	public Integer getId() {
		return id;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public Double getPosX() {
		return posX;
	}

	public Double getPosY() {
		return posY;
	}

	
	public void setPosition(double posX, double posY) {
		this.posX = posX;
		this.posY = posY;
	}

	public RelationConstraintNode getConstraintNode() {
		return constraintNode;
	}

	public void setConstraintNode(RelationConstraintNode constraintNode) {
		this.constraintNode = constraintNode;
	}

	public ArrayList<ActivityElement> getParameter1Elements() {
		return parameter1Elements;
	}

	public ArrayList<ActivityElement> getParameter2Elements() {
		return parameter2Elements;
	}
	

	public ArrayList<LineNode> getParameter1Lines() {
		return parameter1Lines;
	}

	public  ArrayList<LineNode> getParameter2Lines() {
		return parameter2Lines;
	}
	
	/**
	 * creates a LineNode from this RelationConstraint to the given ActivityElement.
	 * ActivityElement should already be in List of Activities of this relationConstraint. 
	 * Line will be added to the list of paramater1Elements of this relationConstraint.
	 * @param activityElement
	 * @param parameterNumber - defines the relevant parameter of the constraint
	 * @return - the lineNode which has to be added to the contentPane
	 */
	public LineNode createAndSetLineNode(ActivityElement activityElement, int parameterNumber){	
		LineNode newLine = new LineNode(this.getConstraintNode(), activityElement.getNode(),parameterNumber);
		if(parameterNumber == 1){
			parameter1Lines.add(newLine);
		} else {
			if (parameterNumber == 2){
				parameter2Lines.add(newLine);
			}
		}
		activityElement.addLineNode(newLine);
		newLine.updateLineElementsAfterChanges();
		constraintNode.updateAfterChanges();
		return newLine;
	}
	
	/**
	 * adds an ActivityElement to the list of Activities of the RelationConstraint and the other way around.
	 * creates a new LineNode and returns it.
	 * updates ActivityElementList and LineNodeList.
	 * 
	 * @param activityElement
	 * @return - the lineNode which has to be added to the contentPane
	 */
	public LineNode addActivityElement(ActivityElement activityElement, int parameterNumber) {
		if(parameterNumber == 1){
			parameter1Elements.add(activityElement);
		} else {
			if (parameterNumber == 2){
				parameter2Elements.add(activityElement);
			}
		}
		activityElement.getConstraintList().add(this);
		LineNode newLine = createAndSetLineNode(activityElement, parameterNumber);
		return newLine;
	}
	
	/**
	 * Removes the Connection between a this RelationConstraint and an Activity
	 * @param activityElement
	 * @return - the lineNode which has to be removed from the contentPane
	 */
	public LineNode removeActivity(ActivityElement activityElement, int parameterNumber){
		LineNode removedLine = activityElement.getLineNodeByConstraint(this);
		activityElement.removeLineNode(removedLine);
		activityElement.getConstraintList().remove(this);
		
		if (parameterNumber == 1){
			parameter1Elements.remove(activityElement);
			parameter1Lines.remove(removedLine);
		} else {
			if (parameterNumber == 2){
				parameter2Elements.remove(activityElement);
				parameter2Lines.remove(removedLine);
			}
		}
		
		return removedLine;
	}
		
	

	public void changeConstraintType(Template template){
		if(template != null){
			this.template = template;
			constraintNode.updateAfterChanges();
			for (LineNode line : getParameter1Lines()){
				line.updateLineElementsAfterChanges();
			}
			for (LineNode line : getParameter2Lines()){
				line.updateLineElementsAfterChanges();
			}
			
		} else {
			//logger.error("Constraint type could not be set because of invalid template type: NULL");
		}
	}

	public boolean isPositionFixed() {
		return positionFixed;
	}

	public void setPositionFixed(boolean positionFixed) {
		this.positionFixed = positionFixed;
	}
	
}

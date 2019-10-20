package minerful.gui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all information that is needed to draw a new Node and to save it. Every change that is being made by the GUI should be applied to here to always have the latest information.
 * 
 * @author Lukas
 *
 */
public class RelationConstraintElement implements Serializable {
	
	//private final static Logger logger = Logger.getLogger(RelationConstraintElement.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -18749943677928012L;
	//Information
	private Integer id;
	private Template template;
	private Double posX;
	private Double posY;
	private boolean positionFixed = false;
	
	//connectedElements and Nodes
	private ArrayList<LineElement> lineElements = new ArrayList<LineElement>();
	private ArrayList<ActivityElement> parameter1Elements = new ArrayList<ActivityElement>();
	private ArrayList<ActivityElement> parameter2Elements = new ArrayList<ActivityElement>();
		
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

	public ArrayList<ActivityElement> getParameter1Elements() {
		return parameter1Elements;
	}

	public ArrayList<ActivityElement> getParameter2Elements() {
		return parameter2Elements;
	}
	
	public ArrayList<LineElement> getLineElements() {
		return lineElements;
	}
	
	/**
	 * adds an ActivityElement to the list of Activities of the RelationConstraint and the other way around.
	 * creates a new LineNode and returns it.
	 * updates ActivityElementList and LineNodeList.
	 * 
	 * @param activityElement
	 * @return - the lineNode which has to be added to the contentPane
	 */
	public void addActivityElement(ActivityElement activityElement, int parameterNumber, double support, double confidence, double interest) {
		
		if(parameterNumber == 1){
			parameter1Elements.add(activityElement);
		} else if(parameterNumber == 2){
			parameter2Elements.add(activityElement);
		}
		
		addLineElements(activityElement,parameterNumber, support, confidence, interest);
		
		activityElement.getConstraintList().add(this);
	}
	
	
	private void addLineElements(ActivityElement activityElement, int parameterNumber, double support, double confidence, double interest) {
		if(parameterNumber == 1) {
			for(ActivityElement aElement2 : parameter2Elements) {
				lineElements.add(new LineElement(activityElement, aElement2, support, confidence, interest));
			}
		} else if(parameterNumber == 2) {
			for(ActivityElement aElement1 : parameter1Elements) {
				lineElements.add(new LineElement(aElement1, activityElement, support, confidence, interest));
			}
		}
	}
	
	private void removeLineElements(ActivityElement activityElement, int parameterNumber) {
		List<LineElement> deletedLineElements = new ArrayList<>();
		
		if(parameterNumber == 1) {
			for(LineElement lineElement : lineElements) {
				if(lineElement.getSourceElement() == activityElement) {
					deletedLineElements.add(lineElement);
				}
			}
			
		} else if(parameterNumber == 2) {
			for(LineElement lineElement : lineElements) {
				if(lineElement.getTargetElement() == activityElement) {
					deletedLineElements.add(lineElement);
					
				}
			}
		}
		
		lineElements.removeAll(deletedLineElements);
	}
	
	/**
	 * Removes the Connection between a this RelationConstraint and an Activity
	 * @param activityElement
	 * @return - the lineNode which has to be removed from the contentPane
	 */
	public void removeActivity(ActivityElement activityElement, int parameterNumber){
		activityElement.getConstraintList().remove(this);
		
		if (parameterNumber == 1){
			parameter1Elements.remove(activityElement);
		} else {
			if (parameterNumber == 2){
				parameter2Elements.remove(activityElement);
			}
		}
		
		removeLineElements(activityElement,parameterNumber);
	}
		
	

	public void changeConstraintType(Template template){
		if(template != null){
			this.template = template;
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

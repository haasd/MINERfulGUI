package minerful.gui.model;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import minerful.gui.model.xml.XMLExistenceConstraint;

public class ActivityElement {

	private Integer id;
	private String identifier;
	private Double posX;
	private Double posY;
	private ArrayList<RelationConstraintElement> constraintList;
	private ActivityNode aNode;
	private XMLExistenceConstraint existenceConstraint;
	private ArrayList<LineNode> lineNodes = new ArrayList<LineNode>();
	
	private StringProperty identifierProperty;
	
	public ActivityElement(Integer id, String identifier) {
		super();
		constraintList = new ArrayList<RelationConstraintElement>();
		this.id = id;
		this.identifier = identifier;
		identifierProperty = new SimpleStringProperty(identifier);
		
		
		// Standard Position if no position was found
		this.posX = 50.0;
		this.posY = 50.0;
	}

	
	public void setPosition(Double posX, Double posY) {
		this.posX = posX;
		this.posY = posY;

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifierProperty.get();
	}

	public void setIdentifier(String identifier) {
		identifierProperty.set(identifier);
	}

	public Double getPosX() {
		return posX;
	}
	

	public Double getPosY() {
		return posY;
	}

	public ArrayList<RelationConstraintElement> getConstraintList() {
		return constraintList;
	}

	public void setNode(ActivityNode aNode) {
		this.aNode = aNode;
	}
	
	public ActivityNode getNode(){
		return this.aNode;
	}

	public XMLExistenceConstraint getExistenceConstraint() {
		return existenceConstraint;
	}

	public void setExistenceConstraint(XMLExistenceConstraint existenceConstraint) {
		this.existenceConstraint = existenceConstraint;
	}

	public StringProperty getIdentifierProperty() {
		return identifierProperty;
	}

	/**
	 * searches for the LineNode that connects this activity to the given relation Constraint
	 * @param relationConstraintElement
	 * @return
	 */
	public LineNode getLineNodeByConstraint(RelationConstraintElement relationConstraintElement) {
		for(LineNode line : lineNodes){
			if(line.getConstraintElement() == relationConstraintElement){
				return line;
			}
		}
		return null;
	}
	
	public String toString(){
		return "ID " + id + ": " + identifier;
	}

	/**
	 * adds a LineNode to 
	 * @param newLine
	 */
	public void addLineNode(LineNode newLine) {
		lineNodes.add(newLine);
		getNode().addCursor(newLine.getConstraintElement().getConstraintNode());
		newLine.getConstraintElement().getConstraintNode().updateAfterChanges();
		newLine.updateLineElementsAfterChanges();
	}
	
	public void removeLineNode(LineNode removedLine){
		lineNodes.remove(removedLine);
		getNode().removeCursor(removedLine.getConstraintElement().getConstraintNode());
		removedLine.updateLineElementsAfterChanges();
	}


	public void updateAllLineNodePositions() {
		for (LineNode line : lineNodes){
			line.updateLinePosition();
		}
		
	}
	
}

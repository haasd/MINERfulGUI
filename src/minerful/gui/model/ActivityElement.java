package minerful.gui.model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.Element;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import minerful.gui.model.xml.XMLExistenceConstraint;

public class ActivityElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 776270594364082518L;
	private Integer id;
	private String taskCharIdentifier;
	private String identifier;
	private Double posX;
	private Double posY;
	private ArrayList<RelationConstraintElement> constraintList;
	private XMLExistenceConstraint existenceConstraint;
	
	private transient StringProperty identifierProperty;
	
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
	
	public ActivityElement(Integer id, String identifier, String taskCharIdentifier) {
		super();
		constraintList = new ArrayList<RelationConstraintElement>();
		this.id = id;
		this.identifier = identifier;
		this.taskCharIdentifier = taskCharIdentifier;
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
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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

	public XMLExistenceConstraint getExistenceConstraint() {
		return existenceConstraint;
	}

	public void setExistenceConstraint(XMLExistenceConstraint existenceConstraint) {
		this.existenceConstraint = existenceConstraint;
	}

	public StringProperty getIdentifierProperty() {
		if(identifierProperty == null) {
			identifierProperty = new SimpleStringProperty(identifier);
		} 
		return identifierProperty;
	}

	public String getTaskCharIdentifier() {
		return taskCharIdentifier;
	}

	public void setTaskCharIdentifier(String taskCharIdentifier) {
		this.taskCharIdentifier = taskCharIdentifier;
	}
	
	public String toString(){
		return "ID " + id + ": " + identifier;
	}
	
}

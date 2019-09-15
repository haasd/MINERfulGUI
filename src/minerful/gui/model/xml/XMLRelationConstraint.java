package minerful.gui.model.xml;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement
public class XMLRelationConstraint {
	
	private Integer id;
	private String template;
	private XMLParameter1Wrapper parameter1Wrapper = new XMLParameter1Wrapper();
	private XMLParameter2Wrapper parameter2Wrapper = new XMLParameter2Wrapper();

	
	public XMLRelationConstraint(){}
	
	/**
	 * Constructor to save existing ConstraintElement as XMLRelationConstraint
	 * @param id
	 * @param parameters1
	 * @param parameters2
	 * @param template
	 */
	public XMLRelationConstraint(Integer id, ArrayList<Integer> parameters1, ArrayList<Integer> parameters2, String template) {
		super();
		this.id = id;
		this.parameter1Wrapper= new XMLParameter1Wrapper(parameters1);
		this.parameter2Wrapper = new XMLParameter2Wrapper(parameters2);
		this.template = template;
	}
		
	@XmlAttribute(required=true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlElement(name="parameter1")
	public XMLParameter1Wrapper getActivationWrapper() {
		return parameter1Wrapper;
	}

	public void setActivationWrapper(XMLParameter1Wrapper parameter1Wrapper) {
		this.parameter1Wrapper = parameter1Wrapper;
	}
	@XmlTransient
	public ArrayList<Integer> getParameter1List() {
		return parameter1Wrapper.getParameter1Ids();
	}
	
	@XmlElement(name="parameter2")
	public XMLParameter2Wrapper getTargerWrapper() {
		return parameter2Wrapper;
	}

	public void setTargerWrapper(XMLParameter2Wrapper parameter2Wrapper) {
		this.parameter2Wrapper = parameter2Wrapper;
	}
	
	@XmlTransient
	public ArrayList<Integer> getParameter2List() {
		return parameter2Wrapper.getParameter2Ids();
	}
	
	@XmlElement
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
/*
	@XmlElement(name="chained")
	public Boolean getChained() {
		return chained;
	}

	public void setChained(Boolean chained) {
		this.chained = chained;
	}

	@XmlElement(name="negation")
	public Boolean getNegation() {
		return negation;
	}

	public void setNegation(Boolean negation) {
		this.negation = negation;
	}

	@XmlElement(name="alternation")
	public Boolean getAlternation() {
		return alternation;
	}

	public void setAlternation(Boolean alternation) {
		this.alternation = alternation;
	}

	@XmlElement(name="a1InsideCursor")
	public Boolean getA1InsideCursor() {
		return a1InsideCursor;
	}

	public void setA1InsideCursor(Boolean a1InsideCursor) {
		this.a1InsideCursor = a1InsideCursor;
	}

	@XmlElement(name="a1OutsideCursor")
	public Boolean getA1OutsideCursor() {
		return a1OutsideCursor;
	}

	public void setA1OutsideCursor(Boolean a1OutsideCursor) {
		this.a1OutsideCursor = a1OutsideCursor;
	}

	@XmlElement(name="A2InsideCursor")
	public Boolean getA2InsideCursor() {
		return a2InsideCursor;
	}

	public void setA2InsideCursor(Boolean a2InsideCursor) {
		this.a2InsideCursor = a2InsideCursor;
	}

	@XmlElement(name="A2OutsideCursor")
	public Boolean getA2OutsideCursor() {
		return a2OutsideCursor;
	}

	public void setA2OutsideCursor(Boolean a2OutsideCursor) {
		this.a2OutsideCursor = a2OutsideCursor;
	}
	*/
}

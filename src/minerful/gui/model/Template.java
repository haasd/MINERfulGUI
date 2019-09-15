package minerful.gui.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Template {
	
	private String name;
	private Boolean chained;
	private Boolean negation;
	private Boolean p1Alternation;
	private Boolean p2Alternation;
	private Boolean p1InsideCursor;
	private Boolean p1OutsideCursor;
	private Boolean p2InsideCursor;
	private Boolean p2OutsideCursor;

	public Template(){
		
	}
	
	
	public Template(String name, Boolean p1InsideCursor, Boolean p1OutsideCursor, Boolean p2InsideCursor, Boolean p2OutsideCursor, 
			Boolean p1Alternation, Boolean p2Alternation, Boolean chained, Boolean negation){
		this.name = name;
		this.p1InsideCursor = p1InsideCursor;
		this.p1OutsideCursor = p1OutsideCursor;
		this.p2InsideCursor = p2InsideCursor;
		this.p2OutsideCursor = p2OutsideCursor;
		this.p1Alternation = p1Alternation;
		this.p2Alternation = p2Alternation;
		this.chained = chained;
		this.negation = negation;
		
	}
	
	

	public String getName() {
		return name;
	}

	public Boolean getChained() {
		return chained;
	}

	public Boolean getNegation() {
		return negation;
	}

	public Boolean getP1Alternation() {
		return p1Alternation;
	}
	
	public Boolean getP2Alternation() {
		return p2Alternation;
	}

	public Boolean getP1InsideCursor() {
		return p1InsideCursor;
	}

	public Boolean getP1OutsideCursor() {
		return p1OutsideCursor;
	}

	public Boolean getP2InsideCursor() {
		return p2InsideCursor;
	}

	public Boolean getP2OutsideCursor() {
		return p2OutsideCursor;
	}
	
	public String toString(){
		return this.name;
	}
	
}

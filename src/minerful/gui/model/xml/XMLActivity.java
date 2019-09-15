package minerful.gui.model.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XMLActivity {
	
	
	private Integer id;
	private String identifier;
	
	public XMLActivity(){}
	
	public XMLActivity(Integer id, String identifier) {
		this.id = id;
		this.identifier = identifier;
	}
	
	/*
	public String toString(){
		String r = id + ", " + identifier;
		r +=  ";";
		return r;
	}*/
	
	//@XmlID
	@XmlAttribute(required=true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlElement(name="identifier")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	
}

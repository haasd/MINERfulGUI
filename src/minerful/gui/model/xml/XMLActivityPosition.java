package minerful.gui.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLActivityPosition extends XMLPosition {
	
	private Integer id;
	
	public XMLActivityPosition(){
		super(0.0,0.0);
	}

	
	public XMLActivityPosition(Integer id, double posX, double posY) {
		super(posX, posY);
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	
	
	

}

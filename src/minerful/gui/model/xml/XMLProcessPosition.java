package minerful.gui.model.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement(name="position")	
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLProcessPosition {
	
	

	@XmlTransient							
	private static final File fileName = new File("position.xml");	// defines the Filename of the processPosition (in the ZipFile)
	
	private List<XMLActivityPosition> activity = new ArrayList<XMLActivityPosition>();
	private List<XMLRelationConstraintPosition> constraint = new ArrayList<XMLRelationConstraintPosition>();
	
	public XMLProcessPosition(){}

	public XMLProcessPosition(List<XMLActivityPosition> activity, List<XMLRelationConstraintPosition> constraint) {
		this.activity = activity;
		this.constraint = constraint;
	}

	public List<XMLActivityPosition> getActivity() {
		return activity;
	}

	public void setActivity(List<XMLActivityPosition> activity) {
		this.activity = activity;
	}

	public List<XMLRelationConstraintPosition> getConstraint() {
		return constraint;
	}

	public void setConstraint(List<XMLRelationConstraintPosition> constraint) {
		this.constraint = constraint;
	}

	public static File getFileName() {
		return fileName;
	}
	
	
	
}

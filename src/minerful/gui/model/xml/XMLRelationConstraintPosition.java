package minerful.gui.model.xml;

public class XMLRelationConstraintPosition extends XMLPosition {
	
	private Integer id;
	
	public XMLRelationConstraintPosition(){
		super(0.0,0.0);
	}

	public XMLRelationConstraintPosition(Integer id, double posX, double posY) {
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

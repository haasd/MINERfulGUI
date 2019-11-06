package minerful.gui.model;

public class CardinalityElement extends ConstraintElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7999508921869223590L;

	private String border;

	public CardinalityElement(String border) {
		super();
		this.border = border;
	}

	public CardinalityElement(String border, double support, double confidence, double interest) {
		super(support, confidence, interest);
		this.border = border;
	}

	public String getBorder() {
		return border;
	}

	public void setBorder(String border) {
		this.border = border;
	}

	@Override
	public String toString() {
		return border;
	}
	
	
	

}

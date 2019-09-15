package minerful.gui.model.xml;

public abstract class XMLPosition {
	
	private Double posX = 0.0;
	private Double posY = 0.0;
	public XMLPosition(double posX, double posY) {
		this.posX = posX;
		this.posY = posY;
	}
	public double getPosX() {
		return posX;
	}
	public void setPosX(double posX) {
		this.posX = posX;
	}
	public double getPosY() {
		return posY;
	}
	public void setPosY(double posY) {
		this.posY = posY;
	}
	
}

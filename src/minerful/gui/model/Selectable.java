package minerful.gui.model;

import javafx.scene.Node;
import javafx.scene.shape.Circle;

public interface Selectable {

	public double getPosX();
	public double getPosY();
	public void setEditable(boolean editable);
	public Node getDeleteButton();
}

package minerful.gui.service.loginfo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;

public class EventFilter {
	
	private String eventName;
	private CheckBox selected;

	public EventFilter(String eventName, boolean selected) {
		super();
		this.eventName = eventName;
		this.selected = new CheckBox();
		this.selected.setSelected(selected);
	}
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public CheckBox getSelected() {
		return selected;
	}
	
	public void setSelected(CheckBox selected) {
		this.selected = selected;
	}
}

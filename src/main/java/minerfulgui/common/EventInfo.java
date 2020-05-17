package minerfulgui.common;

import org.deckfour.xes.extension.std.XExtendedEvent;

public class EventInfo {
	private Integer index;
	private String label;
	private XExtendedEvent event;
	
	public EventInfo(Integer index, String label, XExtendedEvent event) {
		this.label = label;
		this.event = event;
		this.index = index;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public XExtendedEvent getEvent() {
		return event;
	}
	public void setEvent(XExtendedEvent event) {
		this.event = event;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
	
	
}

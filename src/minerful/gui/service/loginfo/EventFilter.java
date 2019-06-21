package minerful.gui.service.loginfo;

public class EventFilter {
	
	private String eventName;
	private Boolean filterActive;

	public EventFilter(String eventName, Boolean filterActive) {
		super();
		this.eventName = eventName;
		this.filterActive = filterActive;
	}
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public Boolean getFilterActive() {
		return filterActive;
	}
	public void setFilterActive(Boolean filterActive) {
		this.filterActive = filterActive;
	}

}

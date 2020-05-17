package minerfulgui.common;

import org.deckfour.xes.model.XTrace;

public class TraceInfo {
	private String label;
	private XTrace trace;
	
	public TraceInfo(String label, XTrace trace) {
		this.label = label;
		this.trace = trace;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public XTrace getTrace() {
		return trace;
	}
	public void setTrace(XTrace trace) {
		this.trace = trace;
	}
	
	
}

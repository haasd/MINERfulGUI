package minerful.gui.common;

import java.util.Date;

import org.graphstream.graph.Graph;
import org.graphstream.ui.graphicGraph.GraphicGraph;

import minerful.concept.ProcessModel;

public class ModelInfo {
	
	private ProcessModel processModel;
	private Date saveDate;
	private String saveName;
	private Graph graph;
	
	public ModelInfo() {
		
	}
	
	public ModelInfo(ProcessModel processModel, Date saveDate, String saveName, Graph graph) {
		super();
		this.processModel = processModel;
		this.saveDate = saveDate;
		this.saveName = saveName;
		this.graph = graph;
	}
	public ProcessModel getProcessModel() {
		return processModel;
	}
	public void setProcessModel(ProcessModel processModel) {
		this.processModel = processModel;
	}
	public Date getSaveDate() {
		return saveDate;
	}
	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
	}
	public String getSaveName() {
		return saveName;
	}
	public void setSaveName(String saveName) {
		this.saveName = saveName;
	}
	public Graph getGraph() {
		return graph;
	}
	public void setGraph(Graph graph) {
		this.graph = graph;
	}	
	
}

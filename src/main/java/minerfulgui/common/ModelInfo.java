package minerfulgui.common;

import java.util.Date;

import minerful.concept.ProcessModel;
import minerful.logparser.LogParser;
import minerfulgui.model.ProcessElement;

public class ModelInfo {
	
	private ProcessModel processModel;
	private Date saveDate;
	private String saveName;
	private ProcessElement processElement;
	private LogParser logParser;
	private double supportThreshold;
	private double confidenceThreshold;
	private double interestThreshold;
	private String path;
	
	public ModelInfo() {
		
	}
	
	public ModelInfo(ProcessModel processModel, Date saveDate, String saveName, ProcessElement processElement) {
		super();
		this.processModel = processModel;
		this.saveDate = saveDate;
		this.saveName = saveName;
		this.processElement = processElement;
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

	public ProcessElement getProcessElement() {
		return processElement;
	}

	public void setProcessElement(ProcessElement processElement) {
		this.processElement = processElement;
	}

	public LogParser getLogParser() {
		return logParser;
	}

	public void setLogParser(LogParser logParser) {
		this.logParser = logParser;
	}

	public double getSupportThreshold() {
		return supportThreshold;
	}

	public void setSupportThreshold(double supportThreshold) {
		this.supportThreshold = supportThreshold;
	}

	public double getConfidenceThreshold() {
		return confidenceThreshold;
	}

	public void setConfidenceThreshold(double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}

	public double getInterestThreshold() {
		return interestThreshold;
	}

	public void setInterestThreshold(double interestThreshold) {
		this.interestThreshold = interestThreshold;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}

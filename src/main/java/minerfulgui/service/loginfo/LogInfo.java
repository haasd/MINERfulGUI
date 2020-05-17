package minerfulgui.service.loginfo;

import java.io.File;
import java.util.Date;
import java.util.Map;

import minerful.concept.ProcessModel;
import minerful.logparser.LogParser;

public class LogInfo {
	
	private LogParser logParser;
	private String path;
	private Date date;
	private Map<String,Integer> taskArchive;
	private ProcessModel processModel;
	
	public LogInfo(LogParser logParser, String path, Date date, ProcessModel processModel) {
		super();
		this.logParser = logParser;
		this.path = path;
		this.date = date;
		this.processModel = processModel;
	}
	
	public LogParser getLogParser() {
		return logParser;
	}
	public void setLogParser(LogParser logParser) {
		this.logParser = logParser;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Map<String, Integer> getTaskArchive() {
		return taskArchive;
	}
	public void setTaskArchive(Map<String, Integer> taskArchive) {
		this.taskArchive = taskArchive;
	}

	public ProcessModel getProcessModel() {
		return processModel;
	}

	public void setProcessModel(ProcessModel processModel) {
		this.processModel = processModel;
	}
	
	@Override
	public String toString() {
		File f = new File(path);
		return f.getName();
	}
	
}

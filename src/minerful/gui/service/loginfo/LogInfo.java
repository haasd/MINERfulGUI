package minerful.gui.service.loginfo;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;

public class LogInfo {
	
	private LogParser logParser;
	private String path;
	private Date date;
	private Map<String,Integer> taskArchive;
	
	public LogInfo(LogParser logParser, String path, Date date) {
		super();
		this.logParser = logParser;
		this.path = path;
		this.date = date;
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

}

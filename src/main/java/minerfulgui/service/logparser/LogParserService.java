package minerfulgui.service.logparser;

import java.util.Map;

import javafx.concurrent.Task;
import minerfulgui.service.loginfo.LogInfo;

public interface LogParserService {

	public Task<LogInfo> parseLog();
	
}

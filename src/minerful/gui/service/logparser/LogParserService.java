package minerful.gui.service.logparser;

import java.util.Map;

import javafx.concurrent.Task;
import minerful.gui.service.loginfo.LogInfo;

public interface LogParserService {

	public Task<LogInfo> parseLog();
	
}

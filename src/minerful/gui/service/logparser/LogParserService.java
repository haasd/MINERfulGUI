package minerful.gui.service.logparser;

import javafx.concurrent.Task;

public interface LogParserService {

	public Task<Void> parseLog();
	
}

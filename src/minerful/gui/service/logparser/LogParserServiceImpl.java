package minerful.gui.service.logparser;

import java.io.File;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;
import minerful.MinerFulMinerLauncher;
import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;

public class LogParserServiceImpl implements LogParserService  {
	
	Logger logger = Logger.getLogger(LogParserServiceImpl.class);
	
	private String path;
	
	public LogParserServiceImpl(String path) {
		this.path = path;
	}

	@Override
	public Task<Void> parseLog() {
		Task<Void> task = new Task<Void>() {
		    @Override public Void call() {
		    	
		    	InputLogCmdParameters inputParams =
						new InputLogCmdParameters();
				MinerFulCmdParameters minerFulParams =
						new MinerFulCmdParameters();

				inputParams.inputLogFile = new File(path);
				inputParams.inputLanguage = determineEncoding(path);

				LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputParams, minerFulParams);
				TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();
				logger.info("Found traces: " + logParser.length());
		       
				updateProgress(100, 100);
		        logger.info("Finished parsing Log-File");
		   
		        return null;
		    }
		};
		
		return task;
	}
	
	
	// determine File encoding
	private InputEncoding determineEncoding(String path) {
		switch(path) {
			case "txt": return InputEncoding.strings;
			case "mxml": return InputEncoding.mxml;
			default: return InputEncoding.xes;
		}
	}

}

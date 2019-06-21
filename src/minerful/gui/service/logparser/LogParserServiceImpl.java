package minerful.gui.service.logparser;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;
import minerful.MinerFulMinerLauncher;
import minerful.gui.service.loginfo.LogInfo;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
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
	public Task<LogInfo> parseLog() {
		Task<LogInfo> task = new Task<LogInfo>() {
		    @Override public LogInfo call() {
		    	
		    	InputLogCmdParameters inputParams =
						new InputLogCmdParameters();
				MinerFulCmdParameters minerFulParams =
						new MinerFulCmdParameters();

				inputParams.inputLogFile = new File(path);
				inputParams.inputLanguage = determineEncoding(path);

				LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputParams, minerFulParams);
				logger.info("Finished parsing Log-File");
				logger.info("Found traces: " + logParser.length());
				
				updateProgress(100, 100);
		   
		        return new LogInfo(logParser,path,new Date());
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

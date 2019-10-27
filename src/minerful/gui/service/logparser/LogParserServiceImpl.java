package minerful.gui.service.logparser;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;
import minerful.MinerFulMinerLauncher;
import minerful.concept.ProcessModel;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.service.loginfo.LogInfo;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;

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
				SystemCmdParameters systemParams =
						new SystemCmdParameters();
				PostProcessingCmdParameters postParams =
						new PostProcessingCmdParameters();
				
				minerFulParams.kbParallelProcessingThreads = 4;
				minerFulParams.queryParallelProcessingThreads = 4;
				postParams.supportThreshold = 0.95;
				postParams.confidenceThreshold = 0.25;
				postParams.interestFactorThreshold = 0.125;
				postParams.cropRedundantAndInconsistentConstraints = false;

				inputParams.inputLogFile = new File(path);
				inputParams.inputLanguage = MinerfulGuiUtil.determineInputEncoding(path);
				
				logger.info("Start parsing of Logfile and mining of Process-Model");
				long start = System.currentTimeMillis();
				MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
				ProcessModel processModel = miFuMiLa.mine();
				long time = System.currentTimeMillis() - start;
				logger.info("Finished parsing of Logfile and mining of Process-Model");
				logger.info("Mining Time: "+ TimeUnit.MILLISECONDS.toSeconds(time));
		   
		        return new LogInfo(miFuMiLa.getLogParser(),path,new Date(),processModel);
		    }
		};
		
		return task;
	}

}

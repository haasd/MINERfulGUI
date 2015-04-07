package minerful.miner.call;

import java.util.concurrent.Callable;

import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.OccurrencesStatsBuilder;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

public class MinerFulKBCore implements Callable<GlobalStatsTable> {
	protected static Logger logger;
	protected LogParser logParser;
	protected MinerFulCmdParameters minerFulParams;
	protected TaskCharArchive taskCharArchive;
	
	private long occuTabTime;

	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulKBCore.class.getCanonicalName());
        }
	}

    public MinerFulKBCore(LogParser logParser,
			MinerFulCmdParameters minerFulParams,
			TaskCharArchive taskCharArchive) {
		this.logParser = logParser;
		this.minerFulParams = minerFulParams;
		this.taskCharArchive = taskCharArchive;
	}

	public GlobalStatsTable discover() {
        logger.info("\nComputing occurrences/distances table...");
        
        Integer branchingLimit = null;
        if (!(minerFulParams.branchingLimit.equals(MinerFulCmdParameters.MINIMUM_BRANCHING_LIMIT)))
        	branchingLimit = minerFulParams.branchingLimit;

        long
        	before = 0L,
        	after = 0L;
        
        before = System.currentTimeMillis();
        // initialize the stats builder
        OccurrencesStatsBuilder statsBuilder =
//                new OccurrencesStatsBuilder(alphabet, TaskCharEncoderDecoder.CONTEMPORANEITY_CHARACTER_DELIMITER, branchingLimit);
        		new OccurrencesStatsBuilder(taskCharArchive, branchingLimit);
        // builds the (empty) stats table
        GlobalStatsTable statsTable = statsBuilder.checkThisOut(logParser);
        logger.info("Done!");
        
        after = System.currentTimeMillis();

        occuTabTime = after - before;

        logger.trace("Occurrences/distances table, computed in: " + occuTabTime + " msec");
        // By using LogMF from the extras companion write, you will not incur the cost of parameter construction if debugging is disabled for logger
        LogMF.trace(logger, "\nStats:\n{0}", statsTable);
        
        return statsTable;
	}

	@Override
	public GlobalStatsTable call() throws Exception {
		return this.discover();
	}
}
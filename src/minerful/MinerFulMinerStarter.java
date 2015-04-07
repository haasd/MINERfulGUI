package minerful;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import minerful.automaton.SubAutomaton;
import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.miner.ConstraintsMiner;
import minerful.miner.ProbabilisticExistenceConstraintsMiner;
import minerful.miner.ProbabilisticRelationBranchedConstraintsMiner;
import minerful.miner.ProbabilisticRelationConstraintsMiner;
import minerful.miner.call.MinerFulKBCore;
import minerful.miner.call.MinerFulQueryingCore;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.OccurrencesStatsBuilder;
import minerful.params.InputCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.simplification.ConflictAndRedundancyResolver;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.LogMF;

public class MinerFulMinerStarter extends AbstractMinerFulStarter {

	public static final String MINERFUL_XMLNS = "https://github.com/cdc08x/MINERful/";

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				inputOptions = InputCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions();
		
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}
	
    /**
     * @param args the command line arguments:
     * 	[regular expression]
     *  [number of strings]
     *  [minimum number of characters per string]
     *  [maximum number of characters per string]
     *  [alphabet]...
     */
    public static void main(String[] args) {
    	MinerFulMinerStarter minerMinaStarter = new MinerFulMinerStarter();
    	Options cmdLineOptions = minerMinaStarter.setupOptions();
    	
    	InputCmdParameters inputParams =
    			new InputCmdParameters(
    					cmdLineOptions,
    					args);
        MinerFulCmdParameters minerFulParams =
        		new MinerFulCmdParameters(
        				cmdLineOptions,
    					args);
        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
    	if (inputParams.inputFile == null) {
    		systemParams.printHelpForWrongUsage("Input file missing!", cmdLineOptions);
    		System.exit(1);
    	}
        
        configureLogging(systemParams.debugLevel);
        
        logger.info("Loading log...");
        
        LogParser logParser = deriveLogParserFromLogFile(inputParams, minerFulParams);
        
        TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();

        TaskCharRelatedConstraintsBag bag =
        		minerMinaStarter.mine(logParser, minerFulParams, viewParams, systemParams, taskCharArchive);
        
        new MinerFulProcessViewerStarter().print(bag, viewParams, systemParams, logParser);
    	
        if (minerFulParams.processSchemeOutputFile != null) {
        	File procSchmOutFile =  minerFulParams.processSchemeOutputFile;
        	try {
				marshalMinedProcessScheme(bag, procSchmOutFile);
			} catch (PropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    }

	private static LogParser deriveLogParserFromLogFile(InputCmdParameters inputParams, MinerFulCmdParameters minerFulParams) {
		LogParser logParser = null;
		switch (inputParams.inputLanguage) {
		case xes:
			ClassificationType evtClassi = MinerFulLauncher.fromInputParamToXesLogClassificationType(inputParams.eventClassification);
			try {
				logParser = new XesLogParser(inputParams.inputFile, evtClassi);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Remove from the analysed alphabet those activities that are
			// specified in a user-defined list
			logParser.getEventEncoderDecoder().excludeThese(minerFulParams.activitiesToExcludeFromResult);

			// Let us try to free memory from the unused XesDecoder!
			System.gc();
			break;
		case strings:
			try {
				logParser = new StringLogParser(inputParams.inputFile, ClassificationType.NAME);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			break;
		default:
			throw new UnsupportedOperationException("This encoding ("
					+ inputParams.inputLanguage + ") is not supported yet");
		}

		return logParser;
	}

	public static void marshalMinedProcessScheme(
			TaskCharRelatedConstraintsBag bag, File procSchmOutFile)
			throws JAXBException, PropertyException, FileNotFoundException,
			IOException {
		String pkgName = bag.getClass().getCanonicalName().toString();
		pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
		JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(bag, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();
		
		// OINK
		strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
				" xmlns=\"" + MinerFulMinerStarter.MINERFUL_XMLNS + "\"");
		FileWriter strixFileWriter = new FileWriter(procSchmOutFile);
		strixFileWriter.write(strixBuffer.toString());
		strixFileWriter.flush();
		strixFileWriter.close();
	}
	
	public TaskCharRelatedConstraintsBag mine(LogParser logParser,
			MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams,
			SystemCmdParameters systemParams, Character[] alphabet) {
		TaskCharArchive taskCharArchive = new TaskCharArchive(alphabet);
		return this.mine(logParser, minerFulParams, viewParams, systemParams, taskCharArchive);
	}
	
    public TaskCharRelatedConstraintsBag mine(LogParser logParser, MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, TaskCharArchive taskCharArchive) {
    	GlobalStatsTable globalStatsTable = new GlobalStatsTable(taskCharArchive);

    	if (minerFulParams.parallelProcessingThreads > MinerFulCmdParameters.MINIMUM_PARALLEL_EXECUTION_THREADS) {
        	List<LogParser> listOfLogParsers = logParser.split(minerFulParams.parallelProcessingThreads);
        	List<MinerFulKBCore> listOfMinerFulCores = new ArrayList<>(minerFulParams.parallelProcessingThreads);

        	for (LogParser slicedLogParser : listOfLogParsers) {
        		listOfMinerFulCores.add(new MinerFulKBCore(slicedLogParser, minerFulParams, taskCharArchive));
        	}
        	
        	ExecutorService executor = Executors.newFixedThreadPool(minerFulParams.parallelProcessingThreads);
        	try {
				for (Future<GlobalStatsTable> statsTab : executor.invokeAll(listOfMinerFulCores)) {
System.err.println("Ciao, io sono bimbo " + statsTab.hashCode() + " e tu sei un minchia");
					globalStatsTable.merge(statsTab.get());
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				System.exit(1);
			}
System.exit(0);
        } else {
        	MinerFulKBCore minerFulKbCore = new MinerFulKBCore(logParser, minerFulParams, taskCharArchive);
        	globalStatsTable = minerFulKbCore.discover();
        	MinerFulQueryingCore minerFulQueryingCore = new MinerFulQueryingCore(logParser, minerFulParams, viewParams, taskCharArchive, globalStatsTable);
        	return minerFulQueryingCore.discover();
        }
		return null;
    }
}
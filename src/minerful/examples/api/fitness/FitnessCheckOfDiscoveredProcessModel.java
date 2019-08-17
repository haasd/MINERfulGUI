package minerful.examples.api.fitness;

import java.io.File;
import java.nio.file.Paths;

import minerful.MinerFulFitnessCheckLauncher;
import minerful.MinerFulMinerLauncher;
import minerful.checking.params.CheckingCmdParameters;
import minerful.concept.ProcessModel;
import minerful.examples.api.discovery.MinerFulObserverInvokerOnXesFile;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

public class FitnessCheckOfDiscoveredProcessModel {
	private static final String EXAMPLE_LOG_FILE_1 = FitnessCheckOfDiscoveredProcessModel.class.getClassLoader().getResource("examples/running-example.xes").getPath();
	private static final String EXAMPLE_LOG_FILE_2 = FitnessCheckOfDiscoveredProcessModel.class.getClassLoader().getResource("examples/running-example_sample2.xes").getPath();
	
	private static final String EXAMPLE_LOG_TEST_OUT_CSV_FILE = "/fitness-log-test-example.csv";
	private static final String EXAMPLE_TRACE_TEST_OUT_CSV_FILE = "/fitness-trace-test-example.csv";

	public static void main(String[] args) {
//////////////////////////////////////////////////////////////////
//Discovery phase
//////////////////////////////////////////////////////////////////
		InputLogCmdParameters inputLogParams = new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
		SystemCmdParameters systemParams = new SystemCmdParameters();
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
		
		inputLogParams.inputLogFile = new File(EXAMPLE_LOG_FILE_1);
		inputLogParams.eventClassification = EventClassification.name;
		// Use the one below if you want to classify events not just by their task name!
		postParams.supportThreshold = 0.95; // For a sure total fit with the event log, this parameter should be set to 1.0
		postParams.confidenceThreshold = 0.66; // The higher this is, the higher the frequency of occurrence of tasks triggering the returned constraints
		postParams.interestFactorThreshold = 0.5; // The higher this is, the higher the frequency of occurrence of tasks involved in the returned constraints
		
		// Remove redundant constraints. WARNING: this may take some time.
		// The language of the model remains completely unchanged. What changes is the number of constraints in it.
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		// To leave the default post-processing, comment the line above. To completely remove any form of post-processing, comment the line above and uncomment the following one
		// postParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		
		// Run the discovery algorithm
		System.out.println("Running the discovery algorithm...");
		
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputLogParams, minerFulParams, postParams, systemParams);
		ProcessModel processModel = miFuMiLa.mine();
		
		System.out.println("...Done");
		
		String pathToExampleOutput = Paths.get("").toAbsolutePath().getParent().toString() + "/example-output";
		new File(Paths.get("").toAbsolutePath().getParent().toString() + "/example-output").mkdirs();

//////////////////////////////////////////////////////////////////
//Evaluation phase on an entire log
//////////////////////////////////////////////////////////////////
		CheckingCmdParameters chkParams = new CheckingCmdParameters();
		
		inputLogParams.inputLogFile = new File(EXAMPLE_LOG_FILE_2);
		chkParams.fileToSaveResultsAsCSV = new File(pathToExampleOutput + EXAMPLE_LOG_TEST_OUT_CSV_FILE);
		
		LogParser loPar = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);
		
		MinerFulFitnessCheckLauncher miFuCheLa = new MinerFulFitnessCheckLauncher(processModel, loPar, chkParams);
		
		// Check the process model extracted from EXAMPLE_LOG_FILE_1 against EXAMPLE_LOG_FILE_2
		miFuCheLa.check();		

//////////////////////////////////////////////////////////////////
//Evaluation phase specifically on a single trace of a log
//////////////////////////////////////////////////////////////////
				
		chkParams.fileToSaveResultsAsCSV = new File(pathToExampleOutput + EXAMPLE_TRACE_TEST_OUT_CSV_FILE);
		
		// Check the process model extracted from EXAMPLE_LOG_FILE_1 against the first trace of EXAMPLE_LOG_FILE_2
		miFuCheLa.check(loPar.traceIterator().next());		
		
		System.exit(0);
	}
}
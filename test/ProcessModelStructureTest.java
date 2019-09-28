



import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.io.params.OutputModelParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;

public class ProcessModelStructureTest {
	
	
	@Test
	public void checkProcessModelsConstraints() {
		
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		OutputModelParameters outParams =
				new OutputModelParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters();
		
		postParams.supportThreshold = 0.0;
		postParams.confidenceThreshold = 0.0;
		postParams.interestFactorThreshold = 0.0;
		postParams.cropRedundantAndInconsistentConstraints = false;
		
		inputParams.inputLogFile = new File("test/files/ProcessModelStructureTest.txt");
		inputParams.inputLanguage = InputEncoding.strings;

		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		
		ProcessModel processModel = miFuMiLa.mine();
		
		assertEquals(36, processModel.getAllConstraints().size());
		
		postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.0;
		postParams.interestFactorThreshold = 0.0;
		
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
		miFuSiLa.simplify();
		
		// amount of Constraints should stay the same 
		assertEquals(36, processModel.getAllConstraints().size());
		
		System.out.println(processModel.getAllConstraints());
		
	}

}

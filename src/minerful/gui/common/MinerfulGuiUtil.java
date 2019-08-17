package minerful.gui.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import minerful.MinerFulMinerLauncher;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.gui.controller.EventLogGeneratorController;
import minerful.gui.service.loginfo.LogInfo;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters.Encoding;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.postprocessing.params.PostProcessingCmdParameters;

public class MinerfulGuiUtil {
	
		public static Logger logger = Logger.getLogger(MinerfulGuiUtil.class);
	
		// determine File encoding
		public static InputEncoding determineInputEncoding(String extension) {
			switch(extension) {
				case "txt": return InputEncoding.strings;
				case "mxml": return InputEncoding.mxml;
				default: return InputEncoding.xes;
			}
		}
		
		// determine File encoding
		public static Encoding determineEncoding(String extension) {
			switch(extension) {
				case "txt": return Encoding.strings;
				case "mxml": return Encoding.mxml;
				case "xes": return Encoding.xes;
				default: return null;
			}
		}
		
		//display Alert on error
		public static void displayAlert(String title, String headerText, String contentText) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(title);
			alert.setHeaderText(headerText);
			alert.setContentText(contentText);
			alert.showAndWait();
		}
		
		public static List<FitnessCheckInfo> deriveFitnessCheckInfo(ModelFitnessEvaluation mfe) {
			
			List<FitnessCheckInfo> fciList = new ArrayList<>();
			
			TreeSet<Constraint> constraints = new TreeSet<Constraint>(mfe.evaloMap.evaluationsOnLog.keySet());
			
			for(Constraint con : constraints) {
				FitnessCheckInfo fci = new FitnessCheckInfo();
				fci.setTemplate(con.getTemplateName());
				fci.setConstraint(con.toString());
				fci.setFitness(con.getFitness());
				fci.setFullSatisfactions(mfe.evaloMap.evaluationsOnLog.get(con).numberOfFullySatisfyingTraces);
				fci.setVacuousSatisfactions(mfe.evaloMap.evaluationsOnLog.get(con).numberOfVacuouslySatisfyingTraces);
				fci.setViolations(mfe.evaloMap.evaluationsOnLog.get(con).numberOfViolatingTraces);
				fciList.add(fci);
			}
			
			
			return fciList;
		}
		
		public static Task<XLog> generateLog(MinerFulLogMaker logMak, ProcessModel processModel) {
			Task<XLog> task = new Task<XLog>() {
			    @Override public XLog call() {

					logger.info("Start creating Log");
					long start = System.currentTimeMillis();
					XLog log = logMak.createLog(processModel);
					updateProgress(100, 100);
					long time = System.currentTimeMillis() - start;
					logger.info("Finished creating Log!");
					logger.info("Creation Time: "+ TimeUnit.MILLISECONDS.toSeconds(time));

			        return log;
			    }
			};
			
			return task;
		}

}

package minerful.gui.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters.Encoding;
import minerful.params.InputLogCmdParameters.InputEncoding;

public class MinerfulGuiUtil {
	
		public static Logger logger = Logger.getLogger(MinerfulGuiUtil.class);
	
		// determine File encoding
		public static InputEncoding determineInputEncoding(String path) {
			String extension = FilenameUtils.getExtension(path);
			
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
		
		//display Alert
		public static Optional<ButtonType> displayAlert(String title, String headerText, String contentText, AlertType type) {
			Alert alert = new Alert(type);
			alert.getDialogPane().getStylesheets().add(MinerfulGuiUtil.class.getClassLoader().getResource("css/main.css").toExternalForm());
			alert.getDialogPane().getStyleClass().add("alert");
			
			if(type == AlertType.CONFIRMATION) {
				((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Okay");
				((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancel");
			}
			alert.setTitle(title);
			alert.setHeaderText(headerText);
			alert.setContentText(contentText);
			return alert.showAndWait();
		}
		
		public static List<FitnessCheckInfo> deriveFitnessCheckInfo(ModelFitnessEvaluation mfe) {
			
			List<FitnessCheckInfo> fciList = new ArrayList<>();
			
			TreeSet<Constraint> constraints = new TreeSet<Constraint>(mfe.evaloMap.evaluationsOnLog.keySet());
			
			for(Constraint con : constraints) {
				FitnessCheckInfo fci = new FitnessCheckInfo();
				fci.setTemplate(con.getTemplateName());
				fci.setConstraintSource(con.getBase().toString());
				if(con.getImplied() != null) {
					fci.setConstraintTarget(con.getImplied().toString());
				}
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

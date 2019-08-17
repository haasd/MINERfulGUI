package minerful.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.constraint.Constraint;
import minerful.params.InputLogCmdParameters.InputEncoding;

public class MinerfulGuiUtil {
	
		// determine File encoding
		public static InputEncoding determineEncoding(String path) {
			switch(path) {
				case "txt": return InputEncoding.strings;
				case "mxml": return InputEncoding.mxml;
				default: return InputEncoding.xes;
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

}

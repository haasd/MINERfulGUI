package minerful.gui.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.model.io.JFXToSVGConverter;
import minerful.gui.model.io.XmlModelWriter;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.service.loginfo.LogInfo;
import minerful.io.params.OutputModelParameters;
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
		
		public static void exportFile(ProcessElementInterface peInterface, List<ModelInfo> savedModels) {
			// init FileChooser and set extension-filter
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Event-Log");
			FileChooser.ExtensionFilter extFilter = 
		             new FileChooser.ExtensionFilter("ZIP","*.zip");
		    fileChooser.getExtensionFilters().add(extFilter);
			
			extFilter = 
		             new FileChooser.ExtensionFilter("XML", "*.xml");
		    fileChooser.getExtensionFilters().add(extFilter);
			extFilter = 
		             new FileChooser.ExtensionFilter("JSON","*.json");
		    fileChooser.getExtensionFilters().add(extFilter);
			extFilter = 
		             new FileChooser.ExtensionFilter("CSV","*.csv");
		    fileChooser.getExtensionFilters().add(extFilter);
		    extFilter = 
		             new FileChooser.ExtensionFilter("DeclareMap","*.decl");
		    fileChooser.getExtensionFilters().add(extFilter);
			extFilter = 
		             new FileChooser.ExtensionFilter("HTML","*.html");
		    fileChooser.getExtensionFilters().add(extFilter);
		    
		    extFilter = 
		             new FileChooser.ExtensionFilter("SVG","*.svg");
		    fileChooser.getExtensionFilters().add(extFilter);
		    
		    // open FileChooser and handle response
			File saveFile = fileChooser.showSaveDialog(new Stage());
			if(saveFile != null) {
				
				OutputModelParameters outParams = new OutputModelParameters();
				String path = saveFile.getAbsolutePath();
				File outputFile = new File(path);

				logger.info("Save as File: " + path);
				
				String fileName = saveFile.getName();           
				String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, saveFile.getName().length());
				
				logger.info("Saving...");
				boolean customOutput = false;
				
				switch(fileExtension.toLowerCase()) {
					case "xml": 
						outParams.fileToSaveAsXML = outputFile;	
						break;
					case "json":
						outParams.fileToSaveAsJSON = outputFile;
						break;
					case "csv":
						outParams.fileToSaveConstraintsAsCSV = outputFile;
						break;
					case "decl":
						outParams.fileToSaveAsConDec = outputFile;
						break;
					case "html":
						File htmlTemplateFile = new File(MinerfulGuiUtil.class.getClassLoader().getResource("templates/export.html").getFile());
						
						try {
							String htmlString = FileUtils.readFileToString(htmlTemplateFile,"UTF-8");
							String title = "New Page";
							htmlString = htmlString.replace("$title", title);
							File newHtmlFile = new File(saveFile.getAbsolutePath());
							FileUtils.writeStringToFile(newHtmlFile, htmlString, "UTF-8");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						customOutput = true;
						break;
					case "zip": 
						XmlModelWriter mWriter = new XmlModelWriter(peInterface.getCurrentProcessElement());
						mWriter.writeXmlsFromProcessModel(path);
						customOutput = true;
						break;
					case "svg": 
						JFXToSVGConverter svgWriter = new JFXToSVGConverter(peInterface);
						svgWriter.createDocument(outputFile);
						customOutput = true;
						break;
				}
				
				if(customOutput) {
					MinerfulGuiUtil.displayAlert("Information", "Finished export", "Finished export of: " + outputFile, AlertType.INFORMATION);
					return;
				}
				
				MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
				outputMgt.manageOutput(peInterface.getCurrentProcessModel(), outParams);
				
				ModelInfo modelInfo = new ModelInfo(peInterface.getCurrentProcessModel(),new Date(),outputFile.getName(), GraphUtil.cloneProcessElement(peInterface.getCurrentProcessElement()));
				savedModels.add(modelInfo);
				
			} else {
				logger.info("Modelsaving canceled!"); 
			}
			
		}
		
		public static Task<ProcessModel> updateModel(MinerFulSimplificationLauncher miFuSiLa) {
			Task<ProcessModel> task = new Task<ProcessModel>() {
			    @Override public ProcessModel call() {
			    	return miFuSiLa.simplify();
			    }		    
			};
			
			return task;
			
		}
		
		public static Task<ProcessModel> updateModel(MinerFulMinerLauncher miFuMiLa) {
			Task<ProcessModel> task = new Task<ProcessModel>() {
			    @Override public ProcessModel call() {
			    	return miFuMiLa.mine();
			    }		    
			};
			
			return task;
			
		}
}

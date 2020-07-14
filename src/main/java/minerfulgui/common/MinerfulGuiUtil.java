package minerfulgui.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.deckfour.xes.model.XLog;

import dk.brics.automaton.Automaton;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.MinerFulFitnessCheckLauncher;
import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.automaton.concept.weight.WeightedAutomaton;
import minerful.automaton.encdec.WeightedAutomatonFactory;
import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.params.OutputModelParameters;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters.Encoding;
import minerful.logparser.LogParser;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;
import minerfulgui.graph.util.GraphUtil;
import minerfulgui.model.ActivityElement;
import minerfulgui.model.ProcessElement;
import minerfulgui.model.io.JFXToSVGConverter;
import minerfulgui.model.io.XmlModelReader;
import minerfulgui.model.io.XmlModelWriter;
import minerfulgui.service.ProcessElementInterface;
import minerfulgui.service.loginfo.LogInfo;

public class MinerfulGuiUtil {
	
		public static Logger logger = Logger.getLogger(MinerfulGuiUtil.class);
	
		// determine File encoding
		public static InputEncoding determineInputEncoding(String path) {
			String extension = FilenameUtils.getExtension(path);
			
			if(path.contains(".xes.gz")) {
				extension = "xes";
			} else if(path.contains("mxml.gz")) {
				extension = "mxml";
			}
			
			switch(extension) {
				case "txt": return InputEncoding.strings;
				case "mxml": return InputEncoding.mxml;
				case "xes": return InputEncoding.xes;
				default: return null;
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
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			return alert.showAndWait();
		}
		
		public static Task<ModelInfo> importModel(File openFile) {
		    
			Task<ModelInfo> task = new Task<ModelInfo>() {
			    @Override public ModelInfo call() {
			    	ModelInfo modelInfo = new ModelInfo();

			    	if(openFile != null) {
			    		XmlModelReader modelReader = new XmlModelReader(openFile.getAbsolutePath());
			    		ProcessElement processElement = modelReader.importXmlsAsProcessModel();
						modelInfo.setProcessElement(processElement);
						modelInfo.setSaveDate(new Date());
						modelInfo.setSaveName(openFile.getName());
						modelInfo.setProcessModel(GraphUtil.transformProcessElementIntoProcessModel(processElement));
					} 
			    	
			        return modelInfo;
			    }
			};
			
			return task;
		}
		
		public static Task<List<String>> createAutomaton(ProcessModel processModel, LogParser logParser, boolean transparencyForNotTraversed) {
		    
			Task<List<String>> task = new Task<List<String>>() {
			    @Override public List<String> call() throws IOException, TransformerException, JAXBException {
			    	List<String> automaton = new ArrayList<>();

					// Create Automaton XML
					String weightedXml = printWeightedXmlAutomaton(processModel,logParser, false);
					if (weightedXml != null) {
						File automatonXml = new File("help.xml");
						PrintWriter outWriter = new PrintWriter(automatonXml);
						outWriter.print(weightedXml);
						outWriter.flush();
						outWriter.close();
						transitionCluster();
						transitionFurtherCluster();
						weightedAutomatonDotter(transparencyForNotTraversed);

						try {
							File myObj = new File("help.clustered.withnot.xml.dot");
							Scanner myReader = new Scanner(myObj);
							List<String> textFile = new ArrayList<>();

							while (myReader.hasNextLine()) {
								textFile.add(myReader.nextLine().replace("&gt;", ">").replace("'", "\\'"));
							}
							myReader.close();

							deleteAllFiles();
							automaton = textFile;

						} catch (FileNotFoundException e) {
							System.out.println("An error occurred.");
							e.printStackTrace();
						}
					}
			    	
			        return automaton;
			    }
			};
			
			return task;
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
				
				fci.setFullSatisfyingTraces(mfe.evaloMap.evaluationsOnLog.get(con).getFullySatisfyingTraces());
				fci.setVacuousSatisfyingTraces(mfe.evaloMap.evaluationsOnLog.get(con).getVacuousSatisfyingTraces());
				fci.setViolatingSatisfyingTraces(mfe.evaloMap.evaluationsOnLog.get(con).getViolatingTraces());
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
					logger.info("Creation Time: "+ formatInterval(time));

			        return log;
			    }
			};
			
			return task;
		}
		
		public static String formatInterval(final long interval)
		{
		    final long min = TimeUnit.MILLISECONDS.toMinutes(interval) %60;
		    final long sec = TimeUnit.MILLISECONDS.toSeconds(interval) %60;
		    final long ms = TimeUnit.MILLISECONDS.toMillis(interval) %1000;
		    return String.format("%02d min %02d sec %03d millis", min, sec, ms);
		}
		
		public static void exportFile(ProcessElementInterface peInterface, List<ModelInfo> savedModels) {
			// init FileChooser and set extension-filter
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(GuiConstants.OPEN_EVENT_LOG);
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
						svgWriter.createDocument(outputFile, peInterface.isParamsStylingActive());
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
		
		public static void setHeight(Control tableView, Integer height) {
			DoubleBinding heightBinding = new SimpleDoubleProperty().add(height);
			
			tableView.minHeightProperty().bind(heightBinding);
			tableView.prefHeightProperty().bind(heightBinding);
			tableView.maxHeightProperty().bind(heightBinding);
		}
		
		public static void initPostProcessingType(VBox processingType, ToggleGroup togglePostAnalysisGroup) {
			RadioButton typeNone = new RadioButton("None");
			typeNone.setToggleGroup(togglePostAnalysisGroup);
			typeNone.setUserData(PostProcessingAnalysisType.NONE);
			typeNone.setSelected(false);
			RadioButton typeHierarchy = new RadioButton("Hierarchy");
			typeHierarchy.setToggleGroup(togglePostAnalysisGroup);
			typeHierarchy.setUserData(PostProcessingAnalysisType.HIERARCHY);
			typeHierarchy.setSelected(true);
			RadioButton typeHierarchyConflict = new RadioButton("Hierarchy and Conflict");
			typeHierarchyConflict.setToggleGroup(togglePostAnalysisGroup);
			typeHierarchyConflict.setUserData(PostProcessingAnalysisType.HIERARCHYCONFLICT);
			typeHierarchyConflict.setSelected(false);
			RadioButton typeHierarchyConflictRedundancy = new RadioButton("Hierarchy, Conflict and Redundancy");
			typeHierarchyConflictRedundancy.setToggleGroup(togglePostAnalysisGroup);
			typeHierarchyConflictRedundancy.setUserData(PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCY);
			typeHierarchyConflictRedundancy.setSelected(false);
			RadioButton typeHierarchyConflictRedundancyDouble = new RadioButton("Hierarchy, Conflict, double Redundancy");
			typeHierarchyConflictRedundancyDouble.setToggleGroup(togglePostAnalysisGroup);
			typeHierarchyConflictRedundancyDouble.setUserData(PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE);
			typeHierarchyConflictRedundancyDouble.setSelected(false);
			processingType.getChildren().addAll(typeNone,typeHierarchy,typeHierarchyConflict,typeHierarchyConflictRedundancy,typeHierarchyConflictRedundancyDouble);

		}
		
		public static List<String> getIdsOfActivityElementList(List<ActivityElement> activityElements){
			List<String> ids = new ArrayList<>();
			
			for(ActivityElement aElement : activityElements) {
				ids.add(aElement.getTaskCharIdentifier());
			}
			
			return ids;
		}
		
		public static void exportFitnessCheckResult(ModelInfo modelInfo, LogInfo eventLogInfo) {
			// init FileChooser and set extension-filter
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Export Fitness check result");
			FileChooser.ExtensionFilter extFilter = 
		             new FileChooser.ExtensionFilter("CSV","*.csv");
		    fileChooser.getExtensionFilters().add(extFilter);
		    
		    // open FileChooser and handle response
			File saveFile = fileChooser.showSaveDialog(new Stage());
			if(saveFile != null) {
				
				CheckingCmdParameters chkParams = new CheckingCmdParameters();
				String path = saveFile.getAbsolutePath();
				File outputFile = new File(path);

				logger.info("Save as File: " + path);
				
				String fileName = saveFile.getName();           
				String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, saveFile.getName().length());
				
				logger.info("Saving...");
				
				switch(fileExtension.toLowerCase()) {
					case "csv": 
						chkParams.fileToSaveResultsAsCSV = outputFile;	
						break;
				}
				
				MinerFulFitnessCheckLauncher miFuCheLa = new MinerFulFitnessCheckLauncher(modelInfo.getProcessModel(), eventLogInfo.getLogParser(), chkParams);
				miFuCheLa.check();
				MinerfulGuiUtil.displayAlert("Information", "Finished export", "Finished export of: " + outputFile, AlertType.INFORMATION);
				
			} else {
				logger.info("Export canceled!"); 
			}
			
		}
		
		public static String printWeightedXmlAutomaton(ProcessModel processModel, LogParser logParser, boolean skimIt) throws JAXBException {
			Automaton processAutomaton = processModel.buildAutomaton();

			WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(
					TaskCharEncoderDecoder.getTranslationMap(processModel.bag));
			WeightedAutomaton wAut = wAF.augmentByReplay(processAutomaton, logParser, skimIt);

			if (wAut == null)
				return null;

			JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
			Marshaller marsh = jaxbCtx.createMarshaller();
			marsh.setProperty("jaxb.formatted.output", true);
			StringWriter strixWriter = new StringWriter();
			marsh.marshal(wAut, strixWriter);
			strixWriter.flush();
			StringBuffer strixBuffer = strixWriter.getBuffer();

			// OINK
			strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
					strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
					" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");

			return strixWriter.toString();
		}
		
		private static void deleteAllFiles() {
			logger.info("help.xml was deleted " + new File("help.xml").delete());
			logger.info("help.clustered.xml was deleted " + new File("help.clustered.xml").delete());
			logger.info("help.clustered.withnot.xml was deleted " + new File("help.clustered.withnot.xml").delete());
			logger.info(
					"help.clustered.withnot.xml.dot was deleted " + new File("help.clustered.withnot.xml.dot").delete());
		}

		public static void transitionCluster() throws FileNotFoundException, TransformerException {
			TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(
					(MinerfulGuiUtil.class.getClassLoader().getResource("dot-xsl/transitionCluster.xsl")).toString()));
			transformer.transform(new StreamSource("help.xml"),
					new StreamResult(new FileOutputStream("help.clustered.xml")));
		}

		public static void transitionFurtherCluster() throws FileNotFoundException, TransformerException {
			TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(
					(MinerfulGuiUtil.class.getClassLoader().getResource("dot-xsl/transitionFurtherCluster.xsl")).toString()));
			transformer.transform(new StreamSource("help.clustered.xml"),
					new StreamResult(new FileOutputStream("help.clustered.withnot.xml")));
		}

		public static void weightedAutomatonDotter(boolean transparencyForNotTraversed) throws FileNotFoundException, TransformerException {
			TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(
					(MinerfulGuiUtil.class.getClassLoader().getResource("dot-xsl/weightedAutomatonDotter.xsl")).toString()));
			transformer.setParameter("DO_WEIGH_LINES", "true()");
			transformer.setParameter("DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED", transparencyForNotTraversed);
			transformer.transform(new StreamSource("help.clustered.withnot.xml"),
					new StreamResult(new FileOutputStream("help.clustered.withnot.xml.dot")));
		}
}

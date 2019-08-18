package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.InteractiveElement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.ConstraintsBag;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.graph.util.ModelGeneratorGraphMouseManager;
import minerful.io.params.OutputModelParameters;

public class ModelGeneratorTabController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(ModelGeneratorTabController.class);
	
	@FXML
	VBox canvasBox;
	
	private ModelInfo modelInfo;
	
	private ConstraintsBag bag = new ConstraintsBag();
	
	private ProcessModel processModel = new ProcessModel(bag);
	
	private Graph graph = new MultiGraph("MINERful");
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Viewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		FxViewPanel view = (FxViewPanel) viewer.addDefaultView(true);
		view.setMouseManager(new ModelGeneratorGraphMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE), processModel, this.getStage()));
		viewer.enableAutoLayout();
		canvasBox.getChildren().add(view);
	}
	
	@FXML
	private void exportFile() {
		
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Event-Log");
		FileChooser.ExtensionFilter extFilter = 
	             new FileChooser.ExtensionFilter("XML/JSON/CSV/HTML", "*.xml", "*.json", "*.csv", "*.html");
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
			
			switch(fileExtension.toLowerCase()) {
				case "xml": 
					//outParams.fileToSaveAsXML = new File(saveFile.getAbsolutePath());
					outParams.fileToSaveAsConDec = outputFile;		
					break;
				case "json":
					outParams.fileToSaveAsJSON = outputFile;
					break;
				case "csv":
					outParams.fileToSaveConstraintsAsCSV = outputFile;
					break;
				case "html":
					File htmlTemplateFile = new File(getClass().getClassLoader().getResource("templates/export.html").getFile());
					
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
					return;
			}
			
			MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
			outputMgt.manageOutput(processModel, outParams);
			
			ModelInfo modelInfo = new ModelInfo(processModel,new Date(),outputFile.getName());
			
			getMainController().addSavedProcessModels(modelInfo);

		} else {
			logger.info("Modelsaving canceled!"); 
		}

	}
	
	@FXML
	public void saveModel(ActionEvent event) {
		logger.info("Save Model");
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Save Model");
		dialog.setHeaderText("Save Model as");
		dialog.setContentText("Modelname:");
		dialog.getDialogPane().setMinWidth(500.0);
		
		ModelInfo modelInfo = new ModelInfo();
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			modelInfo.setSaveName(result.get());
		} else {
			MinerfulGuiUtil.displayAlert("Error Saving", "Error Saving", "No Name was provided!");
			return;
		}
		
		modelInfo.setProcessModel(processModel);
		modelInfo.setSaveDate(new Date());
		
		getMainController().addSavedProcessModels(modelInfo);

	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

}

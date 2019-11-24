package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.ModelInfo;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.model.io.XmlModelReader;

public class SimplifierController extends AbstractController implements Initializable {

	Logger logger = Logger.getLogger(SimplifierController.class);
	
	@FXML
	TabPane simplifierTabPane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	/*TODO Refactor Modal of saved Objects */ 
	@FXML
	public void selectModel(ActionEvent event) {
		logger.info("Start model selection!");
		try {
			Stage stage = new Stage();
		    Parent root;
			stage.setHeight(400.0);
			stage.setWidth(500.0);
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/SavedModel.fxml"));
			root = loader.load();
			SavedModelController modelController = loader.getController();
			modelController.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
			modelController.setMainController(getMainController());
			root.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
			modelController.updateEntries();
			
			stage.setScene(new Scene(root));

		    stage.setTitle(GuiConstants.SAVED_PROCESS_MAPS);
		    stage.initModality(Modality.WINDOW_MODAL);
		    stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
		    stage.showAndWait();
		    
		    if(modelController.getSelectedRow() != null) {
		    	openModelinNewTab(modelController.getSelectedRow());
		    }
			
		} catch (IOException e) {
			logger.info("Problem occured during selection!");
			e.printStackTrace();
		}
	}
	
	@FXML
	public void importModel(ActionEvent event) {
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(GuiConstants.IMPORT_PROCESS_MAP);
		FileChooser.ExtensionFilter extFilter = 
	             new FileChooser.ExtensionFilter("ZIP", "*.zip");
	    fileChooser.getExtensionFilters().add(extFilter);
	    
	    File openFile = fileChooser.showOpenDialog(new Stage());
		if(openFile != null) {
			ModelInfo modelInfo = new ModelInfo();
			XmlModelReader modelReader = new XmlModelReader(openFile.getAbsolutePath());
			modelInfo.setProcessElement(modelReader.importXmlsAsProcessModel());
			modelInfo.setSaveDate(new Date());
			modelInfo.setSaveName(openFile.getName());
			modelInfo.setProcessModel(GraphUtil.transformProcessElementIntoProcessModel(modelInfo.getProcessElement()));
 
			openModelinNewTab(modelInfo);
		}
	    
	    
	}
	
	public void openModelinNewTab(ModelInfo modelInfo) {
		try {

	    	ModelInfo newModelInfo = new ModelInfo();
	    	newModelInfo.setSaveName(modelInfo.getSaveName());
	    	newModelInfo.setProcessModel(modelInfo.getProcessModel());
	    	newModelInfo.setSaveDate((Date) modelInfo.getSaveDate().clone());
	    	newModelInfo.setProcessElement(GraphUtil.cloneProcessElement(modelInfo.getProcessElement()));
			
			logger.info("User selected " + newModelInfo.getSaveName());
	    	Tab tab = new Tab();
	    	FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/simplifier/SimplifierTab.fxml"));
			GridPane gridPane = loader.load();
			SimplifierTabController controller = loader.getController();
			controller.setStage((Stage)((Node) simplifierTabPane).getScene().getWindow());
			controller.setMainController(getMainController());
			controller.setModelInfo(newModelInfo);
			controller.loadGraph();
			
			tab.setContent(gridPane);
			tab.setText(newModelInfo.getSaveName());
			simplifierTabPane.getTabs().add(tab);
			simplifierTabPane.getSelectionModel().select(tab);
		} catch (IOException e) {
			logger.info("Problem occured during processing model!");
			e.printStackTrace();
		}
	}
	
	
}

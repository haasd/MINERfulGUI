package minerfulgui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerfulgui.common.GuiConstants;

public class AutomataGeneratorController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(EventLogGeneratorController.class);
	
	@FXML
	GridPane eventLogGeneratorPane;
	
	@FXML
	TabPane automataGeneratorTabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}

	@FXML
	public void selectEventLog(ActionEvent event) {
		
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
		    	logger.info("User selected " + modelController.getSelectedRow().getSaveName());
		    	Tab tab = new Tab();
				loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/automatagenerator/AutomataGeneratorTab.fxml"));
	    		GridPane gridPane = loader.load();
	    		AutomataGeneratorTabController controller = loader.getController();
				controller.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
				controller.setMainController(getMainController());
				controller.setModelInfo(modelController.getSelectedRow());
				try {
					controller.displayAutomaton();
				} catch (TransformerException | JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
				tab.setContent(gridPane);
				tab.setText(modelController.getSelectedRow().getSaveName());
				automataGeneratorTabPane.getTabs().add(tab);
		    }
			
		} catch (IOException e) {
			logger.info("Problem occured during selection!");
			e.printStackTrace();
		}
		
	}

}

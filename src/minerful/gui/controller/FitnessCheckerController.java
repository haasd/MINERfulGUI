package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerful.MinerFulFitnessCheckLauncher;
import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.service.loginfo.LogInfo;

public class FitnessCheckerController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(FitnessCheckerController.class);
	
	@FXML
	Label selectedModel;
	
	@FXML
	Label selectedEventLog;
	
	@FXML
	TabPane fitnessCheckerTabPane;
	
	private ModelInfo selectedModelInfo;
	
	private LogInfo selectedEventLogInfo;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}
	
	
	/*TODO Refactor Modal of saved Objects */ 
	@FXML
	public void selectModel(ActionEvent event) {
		try {
			Stage stage = new Stage();
		    Parent root;
			stage.setHeight(400.0);
			stage.setWidth(500.0);
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/SavedModel.fxml"));
			root = loader.load();
			SavedModelController controller = loader.getController();
			controller.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
			controller.setMainController(getMainController());
			root.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
			controller.updateEntries();
			
			stage.setScene(new Scene(root));

		    stage.setTitle(GuiConstants.SAVED_PROCESS_MAPS);
		    stage.initModality(Modality.WINDOW_MODAL);
		    stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
		    stage.showAndWait();
		    
		    if(controller.getSelectedRow() != null) {
		    	selectedModelInfo = controller.getSelectedRow();
		    	selectedModel.setText(selectedModelInfo.getSaveName());
		    }
			
		} catch (IOException e) {
			logger.info("Problem occured during saving!");
			e.printStackTrace();
		}
	}
	
	@FXML
	public void selectEventLog(ActionEvent event) {
		try {
			Stage stage = new Stage();
		    Parent root;
			stage.setHeight(400.0);
			stage.setWidth(500.0);
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/SavedEventLog.fxml"));
			root = loader.load();
			SavedEventLogController controller = loader.getController();
			controller.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
			controller.setMainController(getMainController());
			root.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
			controller.updateEntries();
			
			stage.setScene(new Scene(root));

		    stage.setTitle(GuiConstants.SAVED_EVENT_LOGS);
		    stage.initModality(Modality.WINDOW_MODAL);
		    stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
		    stage.showAndWait();
		    
		    if(controller.getSelectedRow() != null) {
		    	selectedEventLogInfo = controller.getSelectedRow();
		    	selectedEventLog.setText(new File(selectedEventLogInfo.getPath()).getName());
		    }
			
		} catch (IOException e) {
			logger.info("Problem occured during saving!");
			e.printStackTrace();
		}
	}

	@FXML
	public void performFitnessCheck(ActionEvent event) {
		if((selectedModelInfo == null) && (selectedEventLogInfo == null)) {
			MinerfulGuiUtil.displayAlert("Missing Selection", "Missing selection", "Please select a model and an event log!", AlertType.ERROR);
		} else if (selectedModelInfo == null) {
			MinerfulGuiUtil.displayAlert("Missing Selection", "Missing selection", "Please select a model!", AlertType.ERROR);
		} else if (selectedEventLogInfo == null) {
			MinerfulGuiUtil.displayAlert("Missing Selection", "Missing selection", "Please select an event log!", AlertType.ERROR);
		} else {
			CheckingCmdParameters chkParams = new CheckingCmdParameters();
			MinerFulFitnessCheckLauncher miFuCheLa = new MinerFulFitnessCheckLauncher(selectedModelInfo.getProcessModel(), selectedEventLogInfo.getLogParser(), chkParams);
			
			ModelFitnessEvaluation mfe = miFuCheLa.check();	
			

			try {
				
				Tab tab = new Tab();
				FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/fitnesschecker/FitnessCheckerTab.fxml"));
	    		GridPane gridPane = loader.load();
				FitnessCheckerTabController controller = loader.getController();
				controller.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
				controller.setMainController(getMainController());
				controller.setMfe(mfe);
				controller.updateFitnessResults();
				controller.setEventLogInfo(selectedEventLogInfo);
				controller.setModelInfo(selectedModelInfo);
	    		
				tab.setContent(gridPane);
				tab.setText("Check");
				fitnessCheckerTabPane.getTabs().add(tab);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}

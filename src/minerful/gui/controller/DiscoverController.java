package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class DiscoverController extends AbstractController implements Initializable  {

	Logger logger = Logger.getLogger(DiscoverController.class);
	
	@FXML
	TabPane discoverTabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}	
	
	@FXML
    private void openFile(ActionEvent actionEvent) {
    	
    	// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Event-Log");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XES, MXML, txt, xes.gz", "*.xes", "*.mxml", "*.txt", "*.xes.gz");
	    fileChooser.getExtensionFilters().add(extFilter);
	    
	    // open FileChooser and handle response
		File selectedFile = fileChooser.showOpenDialog(new Stage());
		if(selectedFile != null) {

			logger.info("Process File: " + selectedFile.getAbsolutePath());
			
			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm("Load Log-File!");
			
			// create Task bind it to ProgressForm and start
			LogParserService logParser = new LogParserServiceImpl(selectedFile.getAbsolutePath());
			Task<LogInfo> parseLog = logParser.parseLog();

			progressForm.activateProgress(parseLog);
			new Thread(parseLog).start();
			
	        parseLog.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent event) {
	            	try {
	    				getMainController().addLoadedLogFile(parseLog.get());
	    				openInfoLogInNewTab(parseLog.get());
	    				progressForm.closeProgressForm();

	    			} catch (InterruptedException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} catch (ExecutionException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            }
	        });
	        
	        parseLog.setOnFailed(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent event) {
	            	progressForm.closeProgressForm();
	            	Throwable throwable = parseLog.getException(); 
	                logger.error("Error occurred while importing file: " + selectedFile.getAbsolutePath(), throwable);
	            	MinerfulGuiUtil.displayAlert("Error", "Stopped Import!", "Error occurred while importing file: " + selectedFile.getAbsolutePath(), AlertType.ERROR);
	            }
	        });

		} else {
			logger.info("Fileselection canceled!"); 
		}
    }
	
	public void openInfoLogInNewTab(LogInfo logInfo) {
		try {
			Tab tab = new Tab();
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/discover/DiscoverTab.fxml"));
    		GridPane gridPane = loader.load();
			DiscoverTabController controller = loader.getController();
			controller.setStage((Stage) ((Node) discoverTabPane).getScene().getWindow());
			controller.setMainController(getMainController());
			controller.setCurrentEventLog(logInfo);
			controller.updateLogInfo();
    		
			tab.setContent(gridPane);
			tab.setText(new File(logInfo.getPath()).getName());
			discoverTabPane.getTabs().add(tab);
			discoverTabPane.getSelectionModel().select(tab);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    private void openFile() throws IOException {
    	
    	// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Event-Log");
		FileChooser.ExtensionFilter extFilter = 
	             new FileChooser.ExtensionFilter("XES/MXML/txt", "*.xes", "*.mxml","*.txt");
	    fileChooser.getExtensionFilters().add(extFilter);
	    
	    // open FileChooser and handle response
		File selectedFile = fileChooser.showOpenDialog(new Stage());
		if(selectedFile != null) {

			logger.info("Process File: " + selectedFile.getAbsolutePath());
			
			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm();
			
			// create Task bind it to ProgressForm and start
			LogParserService logParser = new LogParserServiceImpl(selectedFile.getAbsolutePath());
			Task<LogInfo> parseLog = logParser.parseLog();
			progressForm.activateProgressBar(parseLog);
			new Thread(parseLog).start();
			
			try {
				getMainController().addLoadedLogFile(parseLog.get());
				Tab tab = new Tab();
				FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/DiscoverTab.fxml"));
	    		GridPane gridPane = loader.load();
				DiscoverTabController controller = loader.getController();
				controller.setCurrentEventLog(parseLog.get());
				controller.updateLogInfo();
	    		
				tab.setContent(gridPane);
				tab.setText(new File(parseLog.get().getPath()).getName());
				discoverTabPane.getTabs().add(tab);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			logger.info("Fileselection canceled!"); 
		}
    }

}

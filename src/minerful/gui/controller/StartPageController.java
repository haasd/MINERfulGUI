package minerful.gui.controller;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import minerful.gui.common.ProgressForm;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartPageController {
	
	Logger logger = Logger.getLogger(StartPageController.class);

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
			Task<Void> parseLog = logParser.parseLog();
			progressForm.activateProgressBar(parseLog);
			new Thread(parseLog).start();
			
		} else {
			logger.info("Fileselection canceled!"); 
		}
    }
    
}

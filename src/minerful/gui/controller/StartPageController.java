package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class StartPageController implements Initializable {
	
	Logger logger = Logger.getLogger(StartPageController.class);
	String currentView ="";
	
	@FXML
	private GridPane rootPane;
	
	@FXML
	private GridPane contentPane;
	
	@FXML
    private ImageView imageView;
	
	@FXML
	private FlowPane helpIconPane;
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {	
		
    }
    
    @FXML
    private void openDiscovery(ActionEvent event) {
    	if(currentView != "discover") {
        	logger.info("Open Discovery");
        	loadContent("pages/Discover.fxml");
        	currentView = "discover";
    	} 
    }
    
    private void loadContent(String pathToFxml) {
    	try {
    		GridPane gridPane = FXMLLoader.load(getClass().getClassLoader().getResource(pathToFxml));
    		
    		// replace context pane
			rootPane.getChildren().set(0, gridPane);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(String.format("Cannot load %s!",pathToFxml));
		}
    }
    
    @FXML
    private void openGenerator() {
    	if(currentView != "generator") {
    		logger.info("Open Generator");
        	loadContent("pages/Generator.fxml");
        	currentView = "generator";
    	}
    }
    
    @FXML
    private void openSimplification() {
    	logger.info("Open Simplification");
    }
    
    @FXML
    private void openAutomata() {
    	logger.info("Open Automata");
    }
    
    @FXML
    private void openFitnessCheck() {
    	logger.info("Open Fitness-Check");
    }
    
}

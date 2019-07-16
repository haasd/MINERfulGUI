package minerful.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import minerful.gui.common.GuiConstants;

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
	
	@FXML
	private GridPane infoDiscover;
	
	@FXML
	private GridPane infoSimulate;
	
	@FXML
	private GridPane infoSimplify;
	
	@FXML
	private GridPane infoGenerateAutomata;
	
	@FXML
	private GridPane infoPerformCheck;
	
	private Stage infoStage = new Stage();
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
		initDocumentation();
		infoDiscover.setOnMouseClicked(e -> openDocumentation(GuiConstants.DISCOVER));
		infoSimulate.setOnMouseClicked(e -> openDocumentation(GuiConstants.SIMULATE));
		infoSimplify.setOnMouseClicked(e -> openDocumentation(GuiConstants.SIMPLIFY));
		infoGenerateAutomata.setOnMouseClicked(e -> openDocumentation(GuiConstants.GENERATE_AUTOMATA));
		infoPerformCheck.setOnMouseClicked(e -> openDocumentation(GuiConstants.PERFORM_CHECK));
    }
	
	private void initDocumentation() {
		try {
	        Scene scene = new Scene(FXMLLoader.load(getClass().getClassLoader().getResource("pages/Documentation.fxml")), 600, 400);
	        infoStage.setTitle("Documentation");
	        infoStage.setScene(scene);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	private void openDocumentation(String area) {
		try {
			infoStage.setMaximized(true);
			infoStage.show();
			infoStage.requestFocus();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
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
    private void openStartPage(MouseEvent me) {
    	if(currentView != "startpage") {
    		logger.info("Open Startpage");
    		loadContent("pages/Startpage.fxml");
    		currentView = "startpage";
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

package minerfulgui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerfulgui.common.GuiConstants;
import minerfulgui.common.ModelInfo;
import minerfulgui.service.loginfo.LogInfo;

public class StartPageController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(StartPageController.class);
	String currentView ="";
	
	private final ObservableList<LogInfo> loadedLogFiles =
	        FXCollections.observableArrayList();
	
	private final ObservableList<ModelInfo> savedProcessModels =
	        FXCollections.observableArrayList();

	@FXML
	private GridPane rootPane;
	
	@FXML
	private GridPane contentPane;
	
	@FXML
    private ImageView imageView;
	
	@FXML
    private Button discoverButton;
	
	@FXML
    private Button generateButton;
	
	@FXML
    private Button simplifyButton;
	
	@FXML
    private Button eventLogButton;
	
	@FXML
    private Button automataButton;
	
	@FXML
    private Button fitnessButton;
	
	@FXML
	private FlowPane helpIconPane;
	
	@FXML
	private GridPane infoDiscover;
	
	@FXML
	private GridPane infoSimulate;
	
	@FXML
	private GridPane infoSimplify;
	
	@FXML
	private GridPane infoDraw;
	
	@FXML
	private GridPane infoGenerateAutomata;
	
	@FXML
	private GridPane infoPerformCheck;
	
	private Stage infoStage = new Stage();
	
	private Stage inventoryStage = new Stage();
	
	private InventoryController inventoryController;
	
	private Map<String,GridPane> gridPanes = new HashMap<>();
	
	private List<Button> buttons = new ArrayList<>();
	
	private Map<String,AbstractController> controllerMap = new HashMap<>();
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
		initDocumentation();
		
		buttons.add(discoverButton);
		buttons.add(eventLogButton);
		buttons.add(generateButton);
		buttons.add(simplifyButton);
		buttons.add(fitnessButton);
		buttons.add(automataButton);
    }
	
	private void initDocumentation() {
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/Documentation.fxml"));
			GridPane gridPane = loader.load();
			
	        Scene scene = new Scene(gridPane, 600, 400);
	        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
	        infoStage.setTitle("Documentation");
	        infoStage.setScene(scene);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		
	}
	
	@FXML
	public void openDocumentation() {
		try {
			infoStage.setMaximized(true);
			infoStage.show();
			infoStage.requestFocus();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
    
    @FXML
    public void openDiscovery() {
    	if(currentView != "discover") {
        	logger.info("Open Discovery");
        	
        	handlePane("discover", "pages/discover/Discover.fxml");
 
        	currentView = "discover";
        	removeHighlightOfMenu();
        	discoverButton.getStyleClass().add("active-menu");
    	} 
    }
    
    private GridPane loadContent(String pathToFxml, Boolean newWindow, String paneName) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(pathToFxml));
    		GridPane gridPane = loader.load(); 
    		
    		AbstractController abstractController = loader.getController();
    		abstractController.setMainController(this);
    		
    		controllerMap.put(paneName, abstractController);
    		
    		// replace context pane
    		if(!newWindow) {
    			rootPane.getChildren().set(0, gridPane);
    		} 

			return gridPane;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(String.format("Cannot load %s!",pathToFxml));
		}
    	
    	return null;
    }
    
    
    @FXML
    public void openStartPage(MouseEvent me) {
    	if(currentView != "startpage") {
    		logger.info("Open Startpage");
    		handlePane("tutorial", "pages/Tutorial.fxml");
    		currentView = "startpage";
    	}
    }
    
    @FXML
    public void openModelGenerator() {
    	if(currentView != "modelgenerator") {
    		logger.info("Open Model Generator");
    		
    		handlePane("modelgenerator", "pages/modelgenerator/ModelGenerator.fxml");
    		
        	currentView = "modelgenerator";
        	removeHighlightOfMenu();
        	generateButton.getStyleClass().add("active-menu");
    	}
    }
    
    @FXML
    public void openEventLogGenerator() {
    	if(currentView != "eventloggenerator") {
    		logger.info("Open EventLog Generator");
    		
    		handlePane("eventloggenerator", "pages/eventloggenerator/EventLogGenerator.fxml");
    		
        	currentView = "eventloggenerator";
        	removeHighlightOfMenu();
        	eventLogButton.getStyleClass().add("active-menu");
    	}
    }
    
    @FXML
    public void openSimplifier() {
    	if(currentView != "simplifier") {
        	logger.info("Open Simplification");
        	
        	handlePane("simplifier", "pages/simplifier/Simplifier.fxml");
        	
        	currentView = "simplifier";
        	removeHighlightOfMenu();
        	simplifyButton.getStyleClass().add("active-menu");
    	}
    }
    
    @FXML
    public void openAutomataGenerator() {
    	if(currentView != "automatagenerator") {
        	logger.info("Open Automata Generator");
        	
        	handlePane("automatagenerator", "pages/automatagenerator/AutomataGenerator.fxml");

        	currentView = "automatagenerator";
        	removeHighlightOfMenu();
        	automataButton.getStyleClass().add("active-menu");
    	}
    }
    
    @FXML
    public void openFitnessChecker() {
    	if(currentView != "fitnesschecker") {
        	logger.info("Open Fitness-Check");
        	
        	handlePane("fitnesschecker", "pages/fitnesschecker/FitnessChecker.fxml");

        	currentView = "fitnesschecker";
        	removeHighlightOfMenu();
        	fitnessButton.getStyleClass().add("active-menu");
    	}
    }
    
    @FXML
    private void openInventory() {
    	try {
    		if(inventoryStage.getOwner() == null) {
    			initInventory();
    		}
	        
    		inventoryStage.showAndWait();
    		inventoryStage.requestFocus();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
    }
    
    private void initInventory() {
    	FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/Inventory.fxml"));
        Scene scene;
		try {
			scene = new Scene(loader.load());
			inventoryController = loader.getController();
	        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
	        inventoryController.setMainController(this);
			inventoryController.performAfterInit();
			inventoryStage.initOwner(rootPane.getScene().getWindow());
	        inventoryStage.setScene(scene);
	        inventoryStage.initModality(Modality.WINDOW_MODAL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    public void openLogInArea(String area, LogInfo logInfo) {
    	
    	switch(area) {
	    	case "discover" :
	    		openDiscovery();
		    	DiscoverController controller = (DiscoverController) controllerMap.get("discover");
		    	controller.openInfoLogInNewTab(logInfo);
		    	inventoryStage.close();
		    	break;
    	}
    	
    }
    
    public void openModelInArea(String area, ModelInfo modelInfo) {
    	
    	switch(area) {
	    	case "modelgenerator" : 
	    		openModelGenerator();
	    		ModelGeneratorController modelGeneratorcontroller = (ModelGeneratorController) controllerMap.get("modelgenerator");
	    		modelGeneratorcontroller.openModelinNewTab(modelInfo);
		    	inventoryStage.close();
		    	break;
	    	case "eventloggenerator" :
	    		openEventLogGenerator();
	    		EventLogGeneratorController eventlogController = (EventLogGeneratorController) controllerMap.get("eventloggenerator");
	    		eventlogController.openModelinNewTab(modelInfo);
		    	inventoryStage.close();
		    	break;
    	}
    	
    }

	public ObservableList<LogInfo> getLoadedLogFiles() {
		return loadedLogFiles;
	}
    
    public void addLoadedLogFile(LogInfo logInfo) {
    	loadedLogFiles.add(logInfo);
    }
    
	public ObservableList<ModelInfo> getSavedProcessModels() {
		return savedProcessModels;
	}
	
    public void addSavedProcessModels(ModelInfo processModel) {
    	savedProcessModels.add(processModel);
    }
    
    private void handlePane(String paneName, String path) {
    	
    	if(gridPanes.get(paneName) == null) {
			gridPanes.put(paneName, loadContent(path, false, paneName));
		} else {
			rootPane.getChildren().set(0, gridPanes.get(paneName));
		}
    
    }
    
    private void removeHighlightOfMenu() {
    	for(Button btn : buttons) {
    		btn.getStyleClass().remove("active-menu");
    	}
    }
    
}

package minerful.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.ModelInfo;
import minerful.gui.service.loginfo.LogInfo;

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
	
	private Stage inventoryStage = new Stage();
	
	private InventoryController inventoryController;
	
	private Map<String,GridPane> gridPanes = new HashMap<>();
	
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
        	
        	handlePane("discover", "pages/discover/Discover.fxml");
 
        	currentView = "discover";
    	} 
    }
    
    private GridPane loadContent(String pathToFxml, Boolean newWindow) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(pathToFxml));
    		GridPane gridPane = loader.load(); 
    		
    		AbstractController abstractController = loader.getController();
    		abstractController.setMainController(this);
    		
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
    private void openStartPage(MouseEvent me) {
    	if(currentView != "startpage") {
    		logger.info("Open Startpage");
    		handlePane("tutorial", "pages/Tutorial.fxml");
    		currentView = "startpage";
    	}
    }
    
    @FXML
    private void openModelGenerator() {
    	if(currentView != "modelgenerator") {
    		logger.info("Open Model Generator");
    		
    		handlePane("modelgenerator", "pages/modelgenerator/ModelGenerator.fxml");
    		
        	currentView = "modelgenerator";
    	}
    }
    
    @FXML
    private void openEventLogGenerator() {
    	if(currentView != "eventloggenerator") {
    		logger.info("Open EventLog Generator");
    		
    		handlePane("eventloggenerator", "pages/eventloggenerator/EventLogGenerator.fxml");
    		
        	currentView = "eventloggenerator";
    	}
    }
    
    @FXML
    private void openSimplifier() {
    	if(currentView != "simplifier") {
        	logger.info("Open Simplification");
        	
        	handlePane("simplifier", "pages/simplifier/Simplifier.fxml");
        	
        	currentView = "simplifier";
    	}
    }
    
    @FXML
    private void openAutomataGenerator() {
    	if(currentView != "automatagenerator") {
        	logger.info("Open Automata Generator");
        	
        	handlePane("automatagenerator", "pages/automatagenerator/AutomataGenerator.fxml");

        	currentView = "automatagenerator";
    	}
    }
    
    @FXML
    private void openFitnessChecker() {
    	if(currentView != "fitnesschecker") {
        	logger.info("Open Fitness-Check");
        	
        	handlePane("fitnesschecker", "pages/fitnesschecker/FitnessChecker.fxml");

        	currentView = "fitnesschecker";
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
			gridPanes.put(paneName, loadContent(path, false));
		} else {
			rootPane.getChildren().set(0, gridPanes.get(paneName));
		}
    
    }
    
}

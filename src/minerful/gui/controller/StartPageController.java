package minerful.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javafx.scene.control.Button;
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
	private GridPane infoGenerateAutomata;
	
	@FXML
	private GridPane infoPerformCheck;
	
	private Stage infoStage = new Stage();
	
	private Stage inventoryStage = new Stage();
	
	private InventoryController inventoryController;
	
	private Map<String,GridPane> gridPanes = new HashMap<>();
	
	private List<Button> buttons = new ArrayList<>();
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
		initDocumentation();
		infoDiscover.setOnMouseClicked(e -> openDocumentation(GuiConstants.DISCOVER));
		infoSimulate.setOnMouseClicked(e -> openDocumentation(GuiConstants.SIMULATE));
		infoSimplify.setOnMouseClicked(e -> openDocumentation(GuiConstants.SIMPLIFY));
		infoGenerateAutomata.setOnMouseClicked(e -> openDocumentation(GuiConstants.GENERATE_AUTOMATA));
		infoPerformCheck.setOnMouseClicked(e -> openDocumentation(GuiConstants.PERFORM_CHECK));
		
		buttons.add(discoverButton);
		buttons.add(eventLogButton);
		buttons.add(generateButton);
		buttons.add(simplifyButton);
		buttons.add(fitnessButton);
		buttons.add(automataButton);
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
        	highlightCurrentMenu(event);
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
    private void openModelGenerator(ActionEvent event) {
    	if(currentView != "modelgenerator") {
    		logger.info("Open Model Generator");
    		
    		handlePane("modelgenerator", "pages/modelgenerator/ModelGenerator.fxml");
    		
        	currentView = "modelgenerator";
        	highlightCurrentMenu(event);
    	}
    }
    
    @FXML
    private void openEventLogGenerator(ActionEvent event) {
    	if(currentView != "eventloggenerator") {
    		logger.info("Open EventLog Generator");
    		
    		handlePane("eventloggenerator", "pages/eventloggenerator/EventLogGenerator.fxml");
    		
        	currentView = "eventloggenerator";
        	highlightCurrentMenu(event);
    	}
    }
    
    @FXML
    private void openSimplifier(ActionEvent event) {
    	if(currentView != "simplifier") {
        	logger.info("Open Simplification");
        	
        	handlePane("simplifier", "pages/simplifier/Simplifier.fxml");
        	
        	currentView = "simplifier";
        	highlightCurrentMenu(event);
    	}
    }
    
    @FXML
    private void openAutomataGenerator(ActionEvent event) {
    	if(currentView != "automatagenerator") {
        	logger.info("Open Automata Generator");
        	
        	handlePane("automatagenerator", "pages/automatagenerator/AutomataGenerator.fxml");

        	currentView = "automatagenerator";
        	highlightCurrentMenu(event);
    	}
    }
    
    @FXML
    private void openFitnessChecker(ActionEvent event) {
    	if(currentView != "fitnesschecker") {
        	logger.info("Open Fitness-Check");
        	
        	handlePane("fitnesschecker", "pages/fitnesschecker/FitnessChecker.fxml");

        	currentView = "fitnesschecker";
        	highlightCurrentMenu(event);
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
    
    private void highlightCurrentMenu(ActionEvent event) {
    	for(Button btn : buttons) {
    		if(event.getSource() == btn) {
    			btn.getStyleClass().add("active-menu");
    		} else {
    			btn.getStyleClass().remove("active-menu");
    		}
    	}
    	
    }
    
}

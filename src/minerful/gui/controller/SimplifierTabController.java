package minerful.gui.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.ToggleSwitch;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.AreaChartWithMarker;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.common.ValidationEngine;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.EventHandlerManager;
import minerful.gui.model.LineNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintEnum;
import minerful.gui.model.RelationConstraintNode;
import minerful.gui.model.io.XmlModelReader;
import minerful.gui.service.DiscoverUtil;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.service.loginfo.EventFilter;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

public class SimplifierTabController extends AbstractController implements Initializable, PropertyChangeListener, ProcessElementInterface {

	Logger logger = Logger.getLogger(SimplifierTabController.class);
	
	@FXML
	TabPane simplifierTabPane;
	
	@FXML
	ListView<String> eventsList;
	
	@FXML
	TableColumn<EventFilter, String> eventNameColumn;
	
	@FXML
	VBox processingType;
	
	@FXML
	ToggleSwitch negativeConstraints;
	
	@FXML
	ToggleSwitch positiveConstraints;
	
	@FXML
	Label numberOfConstraints;
	
	@FXML
	ScrollPane scrollPane;
	
	@FXML
	AnchorPane anchorPane;
	
	@FXML
	BorderPane backgroundPane;
	
	@FXML
	AreaChartWithMarker<Double, Integer> supportChart;
	
	@FXML
	AreaChartWithMarker<Double, Integer> confidenceChart;
	
	@FXML
	AreaChartWithMarker<Double, Integer> interestChart;
	
	@FXML
	NumberAxis supportXAxis;
	
	@FXML
	NumberAxis supportYAxis;
	
	@FXML
	NumberAxis confidenceXAxis;
	
	@FXML
	NumberAxis confidenceYAxis;
	
	@FXML
	NumberAxis interestXAxis;
	
	@FXML
	NumberAxis interestYAxis;
	
	@FXML
	TextField supportThresholdField;
	
	@FXML
	Slider supportThresholdSlider;
	
	@FXML
	TextField confidenceThresholdField;
	
	@FXML
	Slider confidenceThresholdSlider;
	
	@FXML
	TextField interestThresholdField;
	
	@FXML
	Slider interestThresholdSlider;
	
	@FXML
	GridPane constraintsTableWrapper;
	
	@FXML
	HiddenSidesPane hiddenSidesPane;
	
	@FXML
	TableView<Constraint> constraintsTable;
	
	@FXML
	TableColumn<Constraint, String> constraintColumn;
	
	@FXML
	TableColumn<Constraint, Double> supportColumn;
	
	@FXML
	TableColumn<Constraint, Double> confidenceColumn;
	
	@FXML
	TableColumn<Constraint, Double> interestColumn;
	
	private EventHandlerManager eventManager = new EventHandlerManager(this);
	private Boolean onLoad = false;
	private ModelInfo modelInfo;
	private double scrollPanePadding = 250d;
	private DoubleProperty maxTranslateX = new SimpleDoubleProperty(scrollPanePadding);
	private DoubleProperty maxTranslateY = new SimpleDoubleProperty(scrollPanePadding);
	private Boolean cropRedundantAndInconsistentConstraints = false;
    private String fixedParameter;
	
	private Double fixedThreshold;
	
	private PostProcessingAnalysisType postProcessingType = PostProcessingAnalysisType.HIERARCHY;
	
	private final ObservableList<String> eventInfos =
	        FXCollections.observableArrayList();
	
	private final ObservableList<Constraint> discoveredConstraints =
	        FXCollections.observableArrayList();
	
	// ProcessNode workaround
	private List<ActivityNode> activityNodes = new ArrayList<>();
	private List<RelationConstraintNode> constraintNodes = new ArrayList<>();
	
	private ProcessModel processModel;
	private ProcessElement processElement;
	
	private Boolean fixed = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// eventsTable init
		eventsList.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		MinerfulGuiUtil.setHeight(eventsList, 250);
		eventsList.setItems(eventInfos);
		
		// define Post Analysis Type
		ToggleGroup togglePostAnalysisGroup = new ToggleGroup();
		MinerfulGuiUtil.initPostProcessingType(processingType, togglePostAnalysisGroup);
		
		togglePostAnalysisGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

		         if (togglePostAnalysisGroup.getSelectedToggle() != null) {
		             postProcessingType = (PostProcessingAnalysisType) togglePostAnalysisGroup.getSelectedToggle().getUserData();
		             updateModel();
		         }

		     } 
		});
		
		// define cropNegativeConstraints
		negativeConstraints.selectedProperty().addListener(it -> {
			if(negativeConstraints.isSelected()) {
				GraphUtil.hideConstraints(activityNodes, constraintNodes, false);
				
			} else {
				GraphUtil.displayConstraints(activityNodes, constraintNodes, false);
			}
			
			setConstraintsTable();
        });
		
		// define cropPositiveConstraints
		positiveConstraints.selectedProperty().addListener(it -> {
			if(positiveConstraints.isSelected()) {
				GraphUtil.hideConstraints(activityNodes, constraintNodes, true);
			} else {
				GraphUtil.displayConstraints(activityNodes, constraintNodes, true);
			}
			
			setConstraintsTable();
        });
		
		supportThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(0.95));
		supportThresholdSlider.setValue(0.95);
		supportThresholdField.textProperty().addListener(getTextFieldChangeListener(supportThresholdSlider));
		supportThresholdField.setOnKeyPressed(onEnterPressed());
		supportThresholdSlider.valueProperty().addListener(getSliderChangeListener(supportThresholdField, "support"));
		supportThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());
		
		confidenceThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(0.25));
		confidenceThresholdSlider.setValue(0.25);
		confidenceThresholdField.textProperty().addListener(getTextFieldChangeListener(confidenceThresholdSlider));
		confidenceThresholdField.setOnKeyPressed(onEnterPressed());
		confidenceThresholdSlider.valueProperty().addListener(getSliderChangeListener(confidenceThresholdField, "confidence"));
		confidenceThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());
		
		interestThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(0.125));
		interestThresholdSlider.setValue(0.125);
		interestThresholdField.textProperty().addListener(getTextFieldChangeListener(interestThresholdSlider));
		interestThresholdField.setOnKeyPressed(onEnterPressed());
		interestThresholdSlider.valueProperty().addListener(getSliderChangeListener(interestThresholdField, "interest"));
		interestThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());
		
		//init Charts
		supportChart.getXAxis().setLabel("Threshold value");
		supportChart.getYAxis().setLabel("Number of constraints");
		supportChart.setLegendVisible(false);
		supportXAxis.setLowerBound(0.0);
		supportXAxis.setAutoRanging(false);
		supportXAxis.setTickUnit(0.2);
		supportXAxis.setUpperBound(1.0);
		supportYAxis.setMinorTickVisible(false);
		
		confidenceChart.getXAxis().setLabel("Threshold value");
		confidenceChart.getYAxis().setLabel("Number of constraints");
		confidenceChart.setLegendVisible(false);
		confidenceXAxis.setLowerBound(0.0);
		confidenceXAxis.setAutoRanging(false);
		confidenceXAxis.setTickUnit(0.2);
		confidenceXAxis.setUpperBound(1.0);
		confidenceYAxis.setMinorTickVisible(false);
		
		interestChart.getXAxis().setLabel("Threshold value");
		interestChart.getYAxis().setLabel("Number of constraints");
		interestChart.setLegendVisible(false);
		interestXAxis.setLowerBound(0.0);
		interestXAxis.setAutoRanging(false);
		interestXAxis.setTickUnit(0.2);
		interestXAxis.setUpperBound(1.0);
		interestYAxis.setMinorTickVisible(false);
		
		// add Listeners for Background
		maxTranslateX.addListener((obs,oldValue,newValue) -> {
			backgroundPane.setPrefWidth(newValue.doubleValue());
		});
		maxTranslateY.addListener((obs,oldValue,newValue) -> {
			backgroundPane.setPrefHeight(newValue.doubleValue());
		});
		
		//Keep right side pinned
		constraintsTableWrapper.setOnMouseEntered(e->hiddenSidesPane.setPinnedSide(Side.RIGHT)); 
		constraintsTableWrapper.setOnMouseExited(e->hiddenSidesPane.setPinnedSide(null));
		
		constraintsTable.setItems(discoveredConstraints);
		constraintColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().toString()));
		
		supportColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("support"));
		supportColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
		
		confidenceColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("confidence"));
		confidenceColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
		
		interestColumn.setCellValueFactory(
                new PropertyValueFactory<Constraint, Double>("interestFactor"));
		interestColumn.setCellFactory(col -> ValidationEngine.cellConstraintFormat());
	}
	
	private void setConstraintsTable() {
		List<Constraint> selectedConstraints = new ArrayList<>();
		discoveredConstraints.clear();
		for(Constraint constraint : processModel.getAllUnmarkedConstraints()) {
			if(RelationConstraintEnum.findTemplateByTemplateLabel(constraint.getTemplateName()) == null) {
				selectedConstraints.add(constraint);
			} else if(!negativeConstraints.isSelected() && !positiveConstraints.isSelected()) {
				selectedConstraints.add(constraint);
			} else if(negativeConstraints.isSelected() && !positiveConstraints.isSelected()) {
				if(RelationConstraintEnum.isPositiveConstraint(constraint.getTemplateName())) {
					selectedConstraints.add(constraint);
				}
			} else if(!negativeConstraints.isSelected() && positiveConstraints.isSelected()) {
				if(!RelationConstraintEnum.isPositiveConstraint(constraint.getTemplateName())) {
					selectedConstraints.add(constraint);
				}
			}
		}
		
		discoveredConstraints.addAll(selectedConstraints);
		numberOfConstraints.setText(String.valueOf(discoveredConstraints.size()));
	}
	
	public void loadGraph() {
		onLoad = true;
		processElement = modelInfo.getProcessElement();
		processModel = modelInfo.getProcessModel();
		
		for(TaskChar taskChar : processModel.getTasks()) {
			eventInfos.add(taskChar.getName());
		}
		
		supportChart.getData().clear();
		supportChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "support"));

		supportChart.addVerticalValueMarker(new Data<>(Double.parseDouble(supportThresholdField.getText()), 0));
		
		confidenceChart.getData().clear();
		confidenceChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "confidence"));
		
		confidenceChart.addVerticalValueMarker(new Data<>(Double.parseDouble(confidenceThresholdField.getText()), 0));
		
		interestChart.getData().clear();
		interestChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "interest"));
		
		interestChart.addVerticalValueMarker(new Data<>(Double.parseDouble(interestThresholdField.getText()), 0));
		discoveredConstraints.addAll(processModel.getAllUnmarkedConstraints());
		
		numberOfConstraints.setText(String.valueOf(discoveredConstraints.size()));
		
		createAndAddNodesOfProcessElement();
	}

	@FXML
	public void selectModel(ActionEvent event) {
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

		    stage.setTitle("Saved Models");
		    stage.initModality(Modality.WINDOW_MODAL);
		    stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
		    stage.showAndWait();
		    
		    if(modelController.getSelectedRow() != null) {
		    	openModelinNewTab(modelController.getSelectedRow());
		    }
			
		} catch (IOException e) {
			logger.info("Problem occured during selection!");
			e.printStackTrace();
		}
	}
	
	@FXML
	public void importModel(ActionEvent event) {
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Model");
		FileChooser.ExtensionFilter extFilter = 
	             new FileChooser.ExtensionFilter("ZIP", "*.zip");
	    fileChooser.getExtensionFilters().add(extFilter);
	    
	    File openFile = fileChooser.showOpenDialog(new Stage());
		if(openFile != null) {
			ModelInfo modelInfo = new ModelInfo();
			XmlModelReader modelReader = new XmlModelReader(openFile.getAbsolutePath());
			modelInfo.setProcessElement(modelReader.importXmlsAsProcessModel());
			modelInfo.setSaveDate(new Date());
			modelInfo.setSaveName(openFile.getName());
 
			openModelinNewTab(modelInfo);
		}  
	}
	
	public void openModelinNewTab(ModelInfo modelInfo) {
		try {

	    	ModelInfo newModelInfo = new ModelInfo();
	    	newModelInfo.setSaveName(modelInfo.getSaveName());
	    	newModelInfo.setProcessModel(modelInfo.getProcessModel());
	    	newModelInfo.setSaveDate((Date) modelInfo.getSaveDate().clone());
	    	newModelInfo.setProcessElement(GraphUtil.cloneProcessElement(modelInfo.getProcessElement()));
			
			logger.info("User selected " + newModelInfo.getSaveName());
	    	Tab tab = new Tab();
	    	FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("pages/simplifier/SimplifierTab.fxml"));
			GridPane gridPane = loader.load();
			ModelGeneratorTabController controller = loader.getController();
			controller.setStage((Stage)((Node) simplifierTabPane).getScene().getWindow());
			controller.setMainController(getMainController());
			controller.setModelInfo(newModelInfo);
			controller.loadGraph();
			
			tab.setContent(gridPane);
			tab.setText(newModelInfo.getSaveName());
			simplifierTabPane.getTabs().add(tab);
			simplifierTabPane.getSelectionModel().select(tab);
		} catch (IOException e) {
			logger.info("Problem occured during processing model!");
			e.printStackTrace();
		}
	}
	
	private void updateModel() {
		
		if(processModel != null) {
			logger.info("Update Parameters: " + supportThresholdField.getText() + " " + confidenceThresholdField.getText() + " " + interestThresholdField.getText() + " " + postProcessingType);
			
			InputLogCmdParameters inputParams = new InputLogCmdParameters();
			MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
			SystemCmdParameters systemParams = new SystemCmdParameters();
			PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
			postParams.supportThreshold = Double.parseDouble(supportThresholdField.getText());
			postParams.confidenceThreshold = Double.parseDouble(confidenceThresholdField.getText());
			postParams.interestFactorThreshold = Double.parseDouble(interestThresholdField.getText());
			postParams.cropRedundantAndInconsistentConstraints = cropRedundantAndInconsistentConstraints;
			postParams.postProcessingAnalysisType = postProcessingType;
			
			minerFulParams.activitiesToExcludeFromResult = new ArrayList<>();
			
			MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
			
			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm("Update Model!");
			Task<ProcessModel> task = MinerfulGuiUtil.updateModel(miFuSiLa);
			
			progressForm.activateProgress(task);
			new Thread(task).start();
			
	        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent event) {
	            	try {
	    				processModel = task.get();
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
			
			processModel.addPropertyChangeListener(this);
			
			discoveredConstraints.clear();
			discoveredConstraints.addAll(processModel.getAllUnmarkedConstraints());
			
			/*
			 * Handle Graphs
			 */
			if(!"support".equals(fixedParameter) && fixedParameter != null) {
				supportChart.getData().clear();
				supportChart.getData().add(DiscoverUtil.countConstraintForThresholdValueFixed(processModel.getAllConstraints(), "support", fixedParameter, fixedThreshold));
			} else if(fixedParameter == null) {
				supportChart.getData().clear();
				supportChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "support"));
			}

			supportChart.addVerticalValueMarker(new Data<>(Double.parseDouble(supportThresholdField.getText()), 0));
			
			
			if(fixedParameter != null && !"confidence".equals(fixedParameter)) {
				confidenceChart.getData().clear();
				confidenceChart.getData().add(DiscoverUtil.countConstraintForThresholdValueFixed(processModel.getAllConstraints(), "confidence", fixedParameter, fixedThreshold));
			} else {
				confidenceChart.getData().clear();
				confidenceChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "confidence"));
			}
			
			confidenceChart.addVerticalValueMarker(new Data<>(Double.parseDouble(confidenceThresholdField.getText()), 0));
			
			if(!"interest".equals(fixedParameter) && fixedParameter != null) {
				interestChart.getData().clear();
				interestChart.getData().add(DiscoverUtil.countConstraintForThresholdValueFixed(processModel.getAllConstraints(), "interest", fixedParameter, fixedThreshold));
			} else if(fixedParameter == null){
				interestChart.getData().clear();
				interestChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "interest"));
			}
			
			interestChart.addVerticalValueMarker(new Data<>(Double.parseDouble(interestThresholdField.getText()), 0));
							
			anchorPane.getChildren().remove(1, anchorPane.getChildren().size());
			processElement = GraphUtil.transformProcessModelIntoProcessElement(processModel,anchorPane,eventManager, this);
			setMaxTranslate();
			
			if(negativeConstraints.isSelected()) {
				GraphUtil.hideConstraints(activityNodes, constraintNodes, false);
			}
			
			if(positiveConstraints.isSelected()) {
				GraphUtil.hideConstraints(activityNodes, constraintNodes, true);
			}
			setConstraintsTable();
			numberOfConstraints.setText(String.valueOf(discoveredConstraints.size()));
			
		}
	}
	
	/**
	 * Draws the process saved in currentProcess
	 * Elements of the process are the backgroundPane, ActivityNodes and ConstraintNodes
	 * ActivityNodes and ConstraintNodes are created based on the current information of
	 * the currentProcessElement
	 */
	private void createAndAddNodesOfProcessElement(){
		
		for (ActivityElement a: processElement.getActivityEList()){
			ActivityNode aNode = new ActivityNode(a, this);
			activityNodes.add(aNode);
			anchorPane.getChildren().add(aNode);
			eventManager.setEventHandler(aNode);
			this.determineActivityNode(a).updateNode();
		}
		
		onLoad=false;
		
		//create RelationConstraintNodes
		ArrayList<RelationConstraintNode> relationConstraintNodes = new ArrayList<RelationConstraintNode>();
		ArrayList<LineNode> lineNodes = new ArrayList<LineNode>();
		for (RelationConstraintElement c : processElement.getConstraintEList()){
			RelationConstraintNode rcNode = new RelationConstraintNode(c, this);
			constraintNodes.add(rcNode);
			relationConstraintNodes.add(rcNode);
			for (ActivityElement a : c.getParameter1Elements()){
				lineNodes.add(rcNode.createAndSetLineNode(determineActivityNode(a),1));
			}
			for (ActivityElement a : c.getParameter2Elements()){
				lineNodes.add(rcNode.createAndSetLineNode(determineActivityNode(a),2));
			}
			eventManager.setEventHandler(rcNode);
			rcNode.changeConstraintType();
		}
		
		anchorPane.getChildren().addAll(1, relationConstraintNodes);
		anchorPane.getChildren().addAll(1, lineNodes);
	
		
		// initiate IDCounter on Process
		processElement.setMaxActivityID();
		processElement.setMaxConstraintID();
		
		// Adjust BackgroundPane
		setMaxTranslate();
		
	}
	
	private ChangeListener<String> getTextFieldChangeListener(Slider slider) {
		return new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {

					if(newValue != null && !newValue.isEmpty()) {
						slider.setValue(Double.parseDouble(newValue));
					}
			}
		};
	}
	
	private ChangeListener<Number> getSliderChangeListener(TextField textField, String parameter) {
		return new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				String doubleValue = String.format("%.3f", newValue.doubleValue());
				doubleValue = doubleValue.replace(",", ".");
				textField.setText(doubleValue);
				fixed = true;
				fixedParameter = parameter;
				fixedThreshold = newValue.doubleValue();
			}
			
		};
	}
	
	private EventHandler<KeyEvent> onEnterPressed() {
		return new EventHandler<KeyEvent>() {
	        @Override
	        public void handle(KeyEvent ke)
	        {
	            if (ke.getCode().equals(KeyCode.ENTER))
	            {
	                updateModel();
	            }
	        }
	    };
	}
	
	private EventHandler<Event> onMouseReleaseSlider() {
		return new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				updateModel();
			}
		};
	}

	public List<ActivityNode> getActivityNodes() {
		return activityNodes;
	}

	public void setActivityNodes(List<ActivityNode> activityNodes) {
		this.activityNodes = activityNodes;
	}

	public List<RelationConstraintNode> getConstraintNodes() {
		return constraintNodes;
	}

	public void setConstraintNodes(List<RelationConstraintNode> constraintNodes) {
		this.constraintNodes = constraintNodes;
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	@Override
	public void setSelectionModeToAddConstraint() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteActivity(ActivityNode aNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editActivity(ActivityNode activityNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void determineConstraints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void determineActivities() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AnchorPane getAnchorPane() {
		// TODO Auto-generated method stub
		return anchorPane;
	}

	@Override
	public ProcessElement getCurrentProcessElement() {
		// TODO Auto-generated method stub
		return processElement;
	}

	@Override
	public BorderPane getBackgroundPane() {
		return backgroundPane;
	}

	@Override
	public ScrollPane getScrollPane() {
		// TODO Auto-generated method stub
		return scrollPane;
	}

	@Override
	public ProcessModel getCurrentProcessModel() {
		// TODO Auto-generated method stub
		return processModel;
	}

	public DoubleProperty getMaxTranslateX() {
		return maxTranslateX;
	}


	public DoubleProperty getMaxTranslateY() {
		return maxTranslateY;
	}

	public ActivityNode determineActivityNode(ActivityElement activityElement) {
		for(ActivityNode activityNode : activityNodes) {
			if(activityNode.getActivityElement() == activityElement) {
				return activityNode;
			}
		}
		
		return null;
	}
	
	public RelationConstraintNode determineRelationConstraintNode(RelationConstraintElement relationElement) {
		for(RelationConstraintNode rcNode : constraintNodes) {
			if(rcNode.getConstraintElement() == relationElement) {
				return rcNode;
			}
		}
		
		return null;
	}

	@Override
	public void setMaxTranslate() {
		double maxX = scrollPanePadding;
		double maxY = scrollPanePadding;

		for(Node n : anchorPane.getChildren()){
			if (n.getTranslateX() + scrollPanePadding > maxX) {
				maxX = n.getTranslateX() + scrollPanePadding;
			}
			if (n.getTranslateY() + scrollPanePadding > maxY){
				maxY = n.getTranslateY() + scrollPanePadding;
			}
		}
		
		maxTranslateX.set(maxX);
		maxTranslateY.set(maxY);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isParamsStylingActive() {
		return false;
	}
	
	
}

package minerful.gui.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.ToggleSwitch;
import org.graphstream.stream.ProxyPipe;

import javafx.beans.binding.Bindings;
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
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import minerful.gui.model.ZoomableScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import minerful.MinerFulMinerLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
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
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintEnum;
import minerful.gui.model.RelationConstraintNode;
import minerful.gui.service.DiscoverUtil;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.service.loginfo.EventFilter;
import minerful.gui.service.loginfo.LogInfo;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.InputLogCmdParameters.EventClassification;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

public class DiscoverTabController extends AbstractController implements Initializable, PropertyChangeListener, ProcessElementInterface {
	
	Logger logger = Logger.getLogger(DiscoverTabController.class);
	
	private final ObservableList<String> logInfos =
	        FXCollections.observableArrayList();
	
	private final ObservableList<EventFilter> eventInfos =
	        FXCollections.observableArrayList();
	
	private final ObservableList<Constraint> discoveredConstraints =
	        FXCollections.observableArrayList();
	
	@FXML
	TableView<LogInfo> eventLogTable;
	
	@FXML
	TableView<EventFilter> eventsTable;
	
	@FXML
	HiddenSidesPane hiddenSidesPane;
	
	@FXML
	TableView<Constraint> constraintsTable;
	
	@FXML
	ZoomableScrollPane scrollPane;
	
	@FXML
	AnchorPane anchorPane;
	
	@FXML
	GridPane constraintsTableWrapper;
	
	@FXML
	BorderPane backgroundPane;
	
	@FXML
	ListView<String> logInfoList;
	
	@FXML
	TableColumn<LogInfo, String> filenameColumn;
	
	@FXML
	TableColumn<LogInfo, Date> dateColumn;
	
	@FXML
	TableColumn<EventFilter, String> eventNameColumn;
	
	@FXML
	TableColumn<EventFilter, Boolean> filterColumn;
	
	@FXML
	TableColumn<Constraint, String> constraintColumn;
	
	@FXML
	TableColumn<Constraint, Double> supportColumn;
	
	@FXML
	TableColumn<Constraint, Double> confidenceColumn;
	
	@FXML
	TableColumn<Constraint, Double> interestColumn;
	
	@FXML
	ToggleSwitch negativeConstraints;
	
	@FXML
	ToggleSwitch positiveConstraints;
	
	@FXML
	ToggleSwitch parameterStyling;
	
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
	TextField startAtTrace;
	
	@FXML
	TextField stopAtTrace;
	
	@FXML
	VBox processingType;
	
	@FXML
	VBox cropType;
	
	@FXML
	VBox canvasBox;
	
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
	Label numberOfConstraints;
	
	@FXML
	ToggleGroup eventClassificationGroup;
	
	@FXML
	RadioButton radioName;
	
	@FXML
	RadioButton radioLogSpec;
	
	private ProcessModel processModel; 
	
	private LogInfo currentEventLog;
	
	private Boolean reminingRequired = false;
	
	private PostProcessingAnalysisType postProcessingType = PostProcessingAnalysisType.HIERARCHY;
	
	private EventClassification eventClassification = EventClassification.name;
	
	private Boolean cropRedundantAndInconsistentConstraints = false;
	
	private EventHandlerManager eventManager = new EventHandlerManager(this);
	
	private ProcessElement processElement;
	
    private String fixedParameter;
	
	private Double fixedThreshold;
	
	private double scrollPanePadding = 250d;
	private DoubleProperty maxTranslateX = new SimpleDoubleProperty(scrollPanePadding);
	private DoubleProperty maxTranslateY = new SimpleDoubleProperty(scrollPanePadding);
	
	// ProcessNode workaround
	private List<ActivityNode> activityNodes = new ArrayList<>();
	private List<RelationConstraintNode> constraintNodes = new ArrayList<>();
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		scrollPane.getStyleClass().add("model-canvas");
		
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		eventLogTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		logInfoList.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		eventsTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		
		radioName.setSelected(true);
		radioName.setUserData(EventClassification.name);
		
		radioLogSpec.setSelected(false);
		radioLogSpec.setUserData(EventClassification.logspec);
		
		eventClassificationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

		         if (eventClassificationGroup.getSelectedToggle() != null) {
		        	 eventClassification = (EventClassification) eventClassificationGroup.getSelectedToggle().getUserData();
		             reminingRequired = true;
		             updateModel();
		         }

		     } 
		});
		
		startAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		startAtTrace.setOnKeyPressed(onEnterPressed());
		stopAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		stopAtTrace.setOnKeyPressed(onEnterPressed());
		
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

		// define eventLogTable
		filenameColumn.setCellValueFactory(
                new PropertyValueFactory<LogInfo, String>("path"));
		filenameColumn.setCellFactory(column -> {
            TableCell<LogInfo, String> cell = new TableCell<LogInfo, String>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if(empty) {
                        setText(null);
                    }
                    else {
                        if(item != null) {
                        	File f = new File(item);
                            this.setText(f.getName());
                        }
                    }
                }
            };

            return cell;
        });
		
		MinerfulGuiUtil.setHeight(eventLogTable, 150);
	    eventLogTable.managedProperty().bind(eventLogTable.visibleProperty());
	    eventLogTable.visibleProperty().bind(Bindings.isEmpty(eventLogTable.getItems()).not());
	    
	    MinerfulGuiUtil.setHeight(logInfoList, 125);
	    MinerfulGuiUtil.setHeight(eventsTable, 200);
		
		// define date-column and set format
		dateColumn.setCellValueFactory(
                new PropertyValueFactory<LogInfo, Date>("date"));
		
		dateColumn.setCellFactory(column -> {
            TableCell<LogInfo, Date> cell = new TableCell<LogInfo, Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                        setText(null);
                    }
                    else {
                        if(item != null)
                        this.setText(format.format(item));
                    }
                }
            };

            return cell;
        });
		
		eventsTable.setItems(eventInfos);
		constraintsTable.setItems(discoveredConstraints);
		
		// define eventTable
		eventNameColumn.setCellValueFactory(
                new PropertyValueFactory<EventFilter, String>("eventName"));
		eventNameColumn.setCellFactory(column -> {
            TableCell<EventFilter, String> cell = new TableCell<EventFilter, String>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if(empty) {
                        setText(null);
                    }
                    else {
                        if(item != null) {
                            this.setText(item);
                        }
                    }
                }
            };

            return cell;
        });
		
		// define eventTable
		filterColumn.setCellValueFactory(new PropertyValueFactory<EventFilter, Boolean>("selected"));
		
		
		// define Post Analysis Type
		ToggleGroup togglePostAnalysisGroup = new ToggleGroup();
		MinerfulGuiUtil.initPostProcessingType(processingType, togglePostAnalysisGroup);
		
		togglePostAnalysisGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

		         if (togglePostAnalysisGroup.getSelectedToggle() != null) {
		             postProcessingType = (PostProcessingAnalysisType) togglePostAnalysisGroup.getSelectedToggle().getUserData();
		             reminingRequired = true;
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
		
		// define parameterStyling
		parameterStyling.selectedProperty().addListener(it -> {
			if(parameterStyling.isSelected()) {
				GraphUtil.setParameterStyling(activityNodes, constraintNodes,true);
			} else {
				GraphUtil.setParameterStyling(activityNodes, constraintNodes,false);
			}
        });
		
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
		
	}
	
	public void updateLogInfo() {
		logger.info("Update Event-Log Info");
		logInfos.clear();
		
		TaskCharArchive taskArchive = currentEventLog.getLogParser().getTaskCharArchive();
		stopAtTrace.setText(String.valueOf(currentEventLog.getLogParser().length()));
		
		Set<TaskChar> taskSet = taskArchive.getCopyOfTaskChars();
		for(TaskChar taskChar : taskSet) {
			EventFilter filter = new EventFilter(taskChar.getName(), true);
			filter.getSelected().selectedProperty().addListener(new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			    	reminingRequired = true;
			        updateModel();
			    }
			});
			
			eventInfos.add(filter);
		}
		
		processModel = currentEventLog.getProcessModel();
		processModel.addPropertyChangeListener(this);
		discoveredConstraints.clear();
		discoveredConstraints.addAll(processModel.getAllUnmarkedConstraints());
		
		supportChart.getData().clear();
		supportChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "support"));

		supportChart.addVerticalValueMarker(new Data<>(Double.parseDouble(supportThresholdField.getText()), 0));
		
		confidenceChart.getData().clear();
		confidenceChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "confidence"));
		
		confidenceChart.addVerticalValueMarker(new Data<>(Double.parseDouble(confidenceThresholdField.getText()), 0));
		
		interestChart.getData().clear();
		interestChart.getData().add(DiscoverUtil.countConstraintForThresholdValue(processModel.getAllConstraints(), "interest"));
		
		interestChart.addVerticalValueMarker(new Data<>(Double.parseDouble(interestThresholdField.getText()), 0));
		
		if(processModel.getAllUnmarkedConstraints().size() > GuiConstants.NUMBER_CONSTRAINTS_WARNING) {
			Optional<ButtonType> result = MinerfulGuiUtil.displayAlert("Warning", "Proceed rendering of graph?", "Rendering of Graph was stopped due to a high number of constraints.", AlertType.CONFIRMATION);
			if (result.get() == ButtonType.OK){
				processElement = GraphUtil.transformProcessModelIntoProcessElement(processModel,anchorPane,eventManager, this);
				setMaxTranslate();
			} else {
				logger.info("Graphrendering was canceled!");
			}
		} else {
			processElement = GraphUtil.transformProcessModelIntoProcessElement(processModel,anchorPane,eventManager, this);
			setMaxTranslate();
		}
		
		numberOfConstraints.setText(String.valueOf(discoveredConstraints.size()));

		logInfos.add(GuiConstants.FILENAME+new File(currentEventLog.getPath()).getName());
		logInfos.add(GuiConstants.NUMBER_OF_EVENTS+currentEventLog.getLogParser().numberOfEvents());
		logInfos.add(GuiConstants.NUMBER_OF_TRACES+currentEventLog.getLogParser().length());
		logInfos.add(GuiConstants.SHORTEST_TRACE+currentEventLog.getLogParser().minimumTraceLength());
		logInfos.add(GuiConstants.LONGEST_TRACE+currentEventLog.getLogParser().maximumTraceLength());
		
		logInfoList.setItems(logInfos);
	}
	
	private void updateModel() {
		
		if(processModel != null) {
			logger.info("Update Parameters: " + supportThresholdField.getText() + " " + confidenceThresholdField.getText() + " " + interestThresholdField.getText() + " " + postProcessingType);
			
			InputLogCmdParameters inputParams = new InputLogCmdParameters();
			inputParams.eventClassification = eventClassification;
			inputParams.inputLogFile = new File(currentEventLog.getPath());
			inputParams.inputLanguage = MinerfulGuiUtil.determineInputEncoding(currentEventLog.getPath());
			MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
			SystemCmdParameters systemParams = new SystemCmdParameters();
			
			PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
			postParams.supportThreshold = Double.parseDouble(supportThresholdField.getText());
			postParams.confidenceThreshold = Double.parseDouble(confidenceThresholdField.getText());
			postParams.interestFactorThreshold = Double.parseDouble(interestThresholdField.getText());
			postParams.cropRedundantAndInconsistentConstraints = cropRedundantAndInconsistentConstraints;
			postParams.postProcessingAnalysisType = postProcessingType;
			
			minerFulParams.activitiesToExcludeFromResult = new ArrayList<>();
			
			if(startAtTrace != null && startAtTrace.getText() != "" && stopAtTrace != null && stopAtTrace.getText() != "") {
				if(Integer.parseInt(startAtTrace.getText()) > Integer.parseInt(stopAtTrace.getText()) || Integer.parseInt(startAtTrace.getText()) > currentEventLog.getLogParser().length()) {
					startAtTrace.setText(String.valueOf(0));
					inputParams.startFromTrace = 0;
					startAtTrace.setStyle("-fx-focus-color: red ");
				} else {
					inputParams.startFromTrace = Integer.parseInt(startAtTrace.getText());
				}
				
				if(Integer.parseInt(stopAtTrace.getText()) > currentEventLog.getLogParser().length()) {
					stopAtTrace.setText(String.valueOf(currentEventLog.getLogParser().length()));
					inputParams.subLogLength = currentEventLog.getLogParser().length() - 1;
				} else {
					inputParams.subLogLength = Integer.parseInt(stopAtTrace.getText()) - 1;
				}
			} else {
				inputParams.startFromTrace = 0;
				inputParams.subLogLength = currentEventLog.getLogParser().length() - 1;
			}	
			
			// exclude all events that aren't marked
			for(EventFilter filter : eventInfos) {
				if(!filter.getSelected().isSelected()) {
					minerFulParams.activitiesToExcludeFromResult.add(filter.getEventName());
				}
			}
			
			if(!reminingRequired) {
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
			} else {
				reminingRequired = false;
				MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
				
				// set up ProgressForm
				ProgressForm progressForm = new ProgressForm("Update Model!");
				Task<ProcessModel> task = MinerfulGuiUtil.updateModel(miFuMiLa);
				
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
			}
			
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
			
			if(parameterStyling.isSelected()) {
				GraphUtil.setParameterStyling(activityNodes, constraintNodes,true);
			} else {
				GraphUtil.setParameterStyling(activityNodes, constraintNodes,false);
			}
			
		}
		
		setConstraintsTable();
		numberOfConstraints.setText(String.valueOf(discoveredConstraints.size()));
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
	
	@FXML
	private void exportFile() {
		MinerfulGuiUtil.exportFile(this, getMainController().getSavedProcessModels());
	}


	public LogInfo getCurrentEventLog() {
		return currentEventLog;
	}

	public void setCurrentEventLog(LogInfo currentEventLog) {
		this.currentEventLog = currentEventLog;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
	
	@FXML
	public void saveSnapshotOfModel(ActionEvent event) {
		logger.info("Save Snapshot of Model");
		
		TextInputDialog dialog = new TextInputDialog(new File(currentEventLog.getPath()).getName());
		dialog.setTitle("Take snapshot of Model");
		dialog.setHeaderText("Save Snapshot of Model as");
		dialog.setContentText("Modelname:");
		dialog.getDialogPane().setMinWidth(500.0);
		
		
		ModelInfo modelInfo = new ModelInfo();
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			modelInfo.setSaveName(result.get());
		} else {
			modelInfo.setSaveName(new File(currentEventLog.getPath()).getName());
		}
		
		modelInfo.setProcessModel(processModel);
		modelInfo.setSaveDate(new Date());

		modelInfo.setProcessElement(GraphUtil.cloneProcessElement(processElement));
		
		getMainController().addSavedProcessModels(modelInfo);

	}
	
	/**
	 * determines the Node that is translated the most to the right, and the one to 
	 * the bottom and saves the coordinates to define the edge of the background.
	 * Field Scrollpadding defines how much padding there is between the outer elements 
	 * and the edge of the background.
	 */
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

	@Override
	public boolean isParamsStylingActive() {
		return parameterStyling.isSelected();
	}

}

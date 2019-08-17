package minerful.gui.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.InteractiveElement;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ValidationEngine;
import minerful.gui.graph.util.GraphMouseManager;
import minerful.gui.graph.util.GraphUtil;
import minerful.gui.service.loginfo.EventFilter;
import minerful.gui.service.loginfo.LogInfo;
import minerful.io.params.OutputModelParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;

public class DiscoverTabController extends AbstractController implements Initializable, PropertyChangeListener {
	
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
	TableView<Constraint> constraintsTable;
	
	@FXML
	ListView<String> logInfoList;
	
	@FXML
	ListView<String> resultList;
	
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
	VBox canvasBox;
	
	private ProcessModel processModel; 
	
	private LogInfo currentEventLog;
	
	private Boolean activitySelectionChanged = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		eventLogTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		logInfoList.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		eventsTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		
		startAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		startAtTrace.setOnKeyPressed(onEnterPressed());
		stopAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		stopAtTrace.setOnKeyPressed(onEnterPressed());
		
		supportThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter());
		supportThresholdField.textProperty().addListener(getTextFieldChangeListener(supportThresholdSlider));
		supportThresholdField.setOnKeyPressed(onEnterPressed());
		supportThresholdSlider.valueProperty().addListener(getSliderChangeListener(supportThresholdField));
		supportThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());
		
		confidenceThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter());
		confidenceThresholdField.textProperty().addListener(getTextFieldChangeListener(confidenceThresholdSlider));
		confidenceThresholdField.setOnKeyPressed(onEnterPressed());
		confidenceThresholdSlider.valueProperty().addListener(getSliderChangeListener(confidenceThresholdField));
		confidenceThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());
		
		interestThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter());
		interestThresholdField.textProperty().addListener(getTextFieldChangeListener(interestThresholdSlider));
		interestThresholdField.setOnKeyPressed(onEnterPressed());
		interestThresholdSlider.valueProperty().addListener(getSliderChangeListener(interestThresholdField));
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

	    setHeight(eventLogTable, 150);
	    eventLogTable.managedProperty().bind(eventLogTable.visibleProperty());
	    eventLogTable.visibleProperty().bind(Bindings.isEmpty(eventLogTable.getItems()).not());
	    
	    setHeight(logInfoList, 125);
	    setHeight(resultList, 125);  
	    setHeight(eventsTable, 250);
	    setHeight(constraintsTable, 250);
		
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
			    	activitySelectionChanged = true;
			        updateModel();
			    }
			});
			
			eventInfos.add(filter);
		}
		
		processModel = currentEventLog.getProcessModel();
		processModel.addPropertyChangeListener(this);
		discoveredConstraints.addAll(processModel.getAllConstraints());
		Graph graph = GraphUtil.drawGraph(processModel);
		Viewer viewer = new FxViewer( graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		FxViewPanel view = (FxViewPanel) viewer.addDefaultView(true);
		view.setMouseManager(new GraphMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE), processModel, this.getStage()));
		viewer.enableAutoLayout();
		canvasBox.getChildren().add(view);

		logInfos.add(GuiConstants.FILENAME+new File(currentEventLog.getPath()).getName());
		logInfos.add(GuiConstants.NUMBER_OF_EVENTS+currentEventLog.getLogParser().numberOfEvents());
		logInfos.add(GuiConstants.NUMBER_OF_TRACES+currentEventLog.getLogParser().length());
		logInfos.add(GuiConstants.SHORTEST_TRACE+currentEventLog.getLogParser().minimumTraceLength());
		logInfos.add(GuiConstants.LONGEST_TRACE+currentEventLog.getLogParser().maximumTraceLength());
		
		logInfoList.setItems(logInfos);
	}
	
	private void updateModel() {
		if(processModel != null) {
			logger.info("Update Parameters: " + supportThresholdField.getText() + " " + confidenceThresholdField.getText() + " " + interestThresholdField.getText());
			
			InputLogCmdParameters inputParams = new InputLogCmdParameters();
			inputParams.inputLogFile = new File(currentEventLog.getPath());
			inputParams.inputLanguage = MinerfulGuiUtil.determineInputEncoding(currentEventLog.getPath());
			MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
			SystemCmdParameters systemParams = new SystemCmdParameters();
			PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
			postParams.supportThreshold = Double.parseDouble(supportThresholdField.getText());
			postParams.confidenceThreshold = Double.parseDouble(confidenceThresholdField.getText());
			postParams.interestFactorThreshold = Double.parseDouble(interestThresholdField.getText());
			postParams.cropRedundantAndInconsistentConstraints = true;
			
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
			
			if(!activitySelectionChanged) {
				MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
				miFuSiLa.simplify();
			} else {
				MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
				processModel = miFuMiLa.mine();				
			}

			discoveredConstraints.clear();
			discoveredConstraints.addAll(processModel.getAllConstraints());
			
			Graph graph = GraphUtil.drawGraph(processModel);
			Viewer viewer = new FxViewer( graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD );
			FxViewPanel view1 = (FxViewPanel) viewer.addDefaultView(true);
			view1.setMouseManager(new GraphMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE), processModel, this.getStage()));
			viewer.enableAutoLayout();
			canvasBox.getChildren().clear();
			canvasBox.getChildren().add(view1);
			
		}
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
	
	private ChangeListener<Number> getSliderChangeListener(TextField textField) {
		return new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				String doubleValue = String.format("%.3f", newValue.doubleValue());
				doubleValue = doubleValue.replace(",", ".");
				textField.setText(doubleValue);
			}
			
		};
	}
	
	private void setHeight(Control tableView, Integer height) {
		DoubleBinding heightBinding = new SimpleDoubleProperty().add(height);
		
		tableView.minHeightProperty().bind(heightBinding);
		tableView.prefHeightProperty().bind(heightBinding);
		tableView.maxHeightProperty().bind(heightBinding);
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
		
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Event-Log");
		FileChooser.ExtensionFilter extFilter = 
	             new FileChooser.ExtensionFilter("XML/JSON/CSV/HTML", "*.xml", "*.json", "*.csv", "*.html");
	    fileChooser.getExtensionFilters().add(extFilter);
	    
	    // open FileChooser and handle response
		File saveFile = fileChooser.showSaveDialog(new Stage());
		if(saveFile != null) {
			
			OutputModelParameters outParams = new OutputModelParameters();
			String path = saveFile.getAbsolutePath();
			File outputFile = new File(path);

			logger.info("Save as File: " + path);
			
			String fileName = saveFile.getName();           
			String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, saveFile.getName().length());
			
			logger.info("Saving...");
			
			switch(fileExtension.toLowerCase()) {
				case "xml": 
					//outParams.fileToSaveAsXML = new File(saveFile.getAbsolutePath());
					outParams.fileToSaveAsConDec = outputFile;		
					break;
				case "json":
					outParams.fileToSaveAsJSON = outputFile;
					break;
				case "csv":
					outParams.fileToSaveConstraintsAsCSV = outputFile;
					break;
				case "html":
					File htmlTemplateFile = new File(getClass().getClassLoader().getResource("templates/export.html").getFile());
					
					try {
						String htmlString = FileUtils.readFileToString(htmlTemplateFile,"UTF-8");
						String title = "New Page";
						htmlString = htmlString.replace("$title", title);
						File newHtmlFile = new File(saveFile.getAbsolutePath());
						FileUtils.writeStringToFile(newHtmlFile, htmlString, "UTF-8");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
			}
			
			MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
			outputMgt.manageOutput(processModel, outParams);
			
			ModelInfo modelInfo = new ModelInfo(processModel,new Date(),outputFile.getName());
			
			getMainController().addSavedProcessModels(modelInfo);

		} else {
			logger.info("Modelsaving canceled!"); 
		}

	}


	public LogInfo getCurrentEventLog() {
		return currentEventLog;
	}


	public void setCurrentEventLog(LogInfo currentEventLog) {
		this.currentEventLog = currentEventLog;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	public void saveModel(ActionEvent event) {
		logger.info("Save Model");
		
		TextInputDialog dialog = new TextInputDialog(new File(currentEventLog.getPath()).getName());
		dialog.setTitle("Save Model");
		dialog.setHeaderText("Save Model as");
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
		
		getMainController().addSavedProcessModels(modelInfo);

	}

}

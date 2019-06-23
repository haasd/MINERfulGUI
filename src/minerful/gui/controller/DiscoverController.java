package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.ProgressForm;
import minerful.gui.common.ValidationEngine;
import minerful.gui.service.loginfo.EventFilter;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class DiscoverController implements Initializable {

	Logger logger = Logger.getLogger(DiscoverController.class);

	private final ObservableList<LogInfo> loadedLogFiles =
		        FXCollections.observableArrayList();
	
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
	TextField startAtTrace;
	
	@FXML
	TextField stopAtTrace;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		eventLogTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		logInfoList.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		eventsTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		
		startAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		stopAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		
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
		
		eventLogTable.setItems(loadedLogFiles);
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
		filterColumn.setCellValueFactory(
                new PropertyValueFactory<EventFilter, Boolean>("filterActive"));
		filterColumn.setCellFactory(column -> new CheckBoxTableCell<EventFilter,Boolean>());
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
				loadedLogFiles.add(parseLog.get());
				updateLogInfo(parseLog.get());
				
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
	
	private void updateLogInfo(LogInfo logInfo) {
		logger.info("Update Event-Log Info");
		logInfos.clear();
		
		TaskCharArchive taskArchive = logInfo.getLogParser().getTaskCharArchive();
		
		Set<TaskChar> taskSet = taskArchive.getCopyOfTaskChars();
		for(TaskChar taskChar : taskSet) {
			eventInfos.add(new EventFilter(taskChar.getName(), false));
		}
		
		ProcessModel processModel = logInfo.getProcessModel();
		discoveredConstraints.addAll(processModel.getAllConstraints());
		
		logInfos.add(GuiConstants.FILENAME+new File(logInfo.getPath()).getName());
		logInfos.add(GuiConstants.NUMBER_OF_EVENTS+logInfo.getLogParser().numberOfEvents());
		logInfos.add(GuiConstants.NUMBER_OF_TRACES+logInfo.getLogParser().length());
		logInfos.add(GuiConstants.SHORTEST_TRACE+logInfo.getLogParser().minimumTraceLength());
		logInfos.add(GuiConstants.LONGEST_TRACE+logInfo.getLogParser().maximumTraceLength());
		
		logInfoList.setItems(logInfos);
	}
	
	private void setHeight(Control tableView, Integer height) {
		DoubleBinding heightBinding = new SimpleDoubleProperty().add(height);
		
		tableView.minHeightProperty().bind(heightBinding);
		tableView.prefHeightProperty().bind(heightBinding);
		tableView.maxHeightProperty().bind(heightBinding);
	}

}

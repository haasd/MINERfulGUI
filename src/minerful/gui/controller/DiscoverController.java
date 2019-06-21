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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.loginfo.EventFilter;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class DiscoverController implements Initializable {
	
	private static String FILENAME="Filename: ";
	private static String NUMBER_OF_EVENTS="Number of Events: ";
	private static String NUMBER_OF_TRACES="Number of Traces: ";
	private static String SHORTEST_TRACE="Minimum Trace Length: ";
	private static String LONGEST_TRACE="Maximum Trace Length: ";
	private static String NO_EVENT_LOG="No Event-Log loaded!";
	
	Logger logger = Logger.getLogger(DiscoverController.class);

	private final ObservableList<LogInfo> loadedLogFiles =
		        FXCollections.observableArrayList();
	
	private final ObservableList<String> logInfos =
	        FXCollections.observableArrayList();
	
	private final ObservableList<EventFilter> eventInfos =
	        FXCollections.observableArrayList();
	
	@FXML
	TableView<LogInfo> eventLogTable;
	
	@FXML
	TableView<EventFilter> eventsTable;
	
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		eventLogTable.setPlaceholder(new Label(NO_EVENT_LOG));
		logInfoList.setPlaceholder(new Label(NO_EVENT_LOG));
		eventsTable.setPlaceholder(new Label(NO_EVENT_LOG));

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
		
	    DoubleBinding eventLogtableHeightBinding = new SimpleDoubleProperty().add(30*5);
	    DoubleBinding informationListHeightBinding = new SimpleDoubleProperty().add(25*5);
	    DoubleBinding eventTableHeightBinding = new SimpleDoubleProperty().add(25*10);

	    eventLogTable.minHeightProperty().bind(eventLogtableHeightBinding);
	    eventLogTable.prefHeightProperty().bind(eventLogtableHeightBinding);
	    eventLogTable.maxHeightProperty().bind(eventLogtableHeightBinding);
	    eventLogTable.managedProperty().bind(eventLogTable.visibleProperty());
	    eventLogTable.visibleProperty().bind(Bindings.isEmpty(eventLogTable.getItems()).not());
	    
	    logInfoList.minHeightProperty().bind(informationListHeightBinding);
	    logInfoList.prefHeightProperty().bind(informationListHeightBinding);
	    logInfoList.maxHeightProperty().bind(informationListHeightBinding);
	    
	    eventsTable.minHeightProperty().bind(eventTableHeightBinding);
	    eventsTable.prefHeightProperty().bind(eventTableHeightBinding);
	    eventsTable.maxHeightProperty().bind(eventTableHeightBinding);
		
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
		
		logInfos.add(FILENAME+new File(logInfo.getPath()).getName());
		logInfos.add(NUMBER_OF_EVENTS+logInfo.getLogParser().numberOfEvents());
		logInfos.add(NUMBER_OF_TRACES+logInfo.getLogParser().length());
		logInfos.add(SHORTEST_TRACE+logInfo.getLogParser().minimumTraceLength());
		logInfos.add(LONGEST_TRACE+logInfo.getLogParser().maximumTraceLength());
		
		logInfoList.setItems(logInfos);
	}

}

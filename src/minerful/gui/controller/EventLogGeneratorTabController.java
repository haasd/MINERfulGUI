package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import minerful.gui.common.EventInfo;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.common.TraceInfo;
import minerful.gui.common.ValidationEngine;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;

public class EventLogGeneratorTabController extends AbstractController implements Initializable {
	
	private final ObservableList<TraceInfo> eventLogList =
	        FXCollections.observableArrayList();
	
	private final ObservableList<EventInfo> traceInfoList =
	        FXCollections.observableArrayList();
	
	@FXML
	TextField minEventsPerTrace;
	
	@FXML
	TextField maxEventsPerTrace;
	
	@FXML
	TextField tracesInLog;
	
	@FXML
	Pagination pagination;
	
	@FXML
	ListView<TraceInfo> eventLog;
	
	@FXML
	ListView<EventInfo> traceInfo;
	
	private ModelInfo modelInfo;
	
	private Integer traceNumber = 0;
	
	private MinerFulLogMaker logMak;
	
	private LogMakerParameters logMakParameters;
	
	private Integer minEvents, maxEvents;
	
	private Long tracesNumber;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		minEventsPerTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		maxEventsPerTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		tracesInLog.setTextFormatter(ValidationEngine.getNumericFilter());
		
		eventLog.setCellFactory(param -> {
				ListCell<TraceInfo> listCell = new ListCell<>() {
			    @Override
			    protected void updateItem(TraceInfo item, boolean empty) {
			        super.updateItem(item, empty);
	
			        if (empty || item == null || item.getLabel() == null) {
			            setText(null);
			        } else {
			            setText(item.getLabel());
			        }
			    }
			};
			
			listCell.setOnMouseClicked(event -> {
		        if (! listCell.isEmpty() && event.getButton()==MouseButton.PRIMARY 
			             && event.getClickCount() == 2) {
		        	
		        		Iterator<XEvent> it = eventLogList.get(listCell.getIndex()).getTrace().iterator();
		        		traceInfoList.clear();
		        		int i = 0;
		        		
		        		while(it.hasNext()) {
		        			i++;
		        			XExtendedEvent extxevent = new XExtendedEvent(it.next());

		        			traceInfoList.add(new EventInfo(i,extxevent.getName(), extxevent));
		        		}
			        	
			        }
			});
			return listCell;
		});
		
		eventLog.setItems(eventLogList);
		
		traceInfo.setCellFactory(new Callback<ListView<EventInfo>, ListCell<EventInfo>>() {
            @Override
            public ListCell<EventInfo> call(ListView<EventInfo> listView) {
                return new EventListCell();
            }
	    });
		
		traceInfo.setItems(traceInfoList);
	}
	
	@FXML
	public void exportEventLog(ActionEvent event) {
		generateLog(true);
	}
	
	@FXML
	public void generateEventLog(ActionEvent event) {
		generateLog(false);
	}
	
	private class EventListCell extends ListCell<EventInfo> {
        private HBox content;
        private Label activityName = new Label();
        private Label timestamp = new Label();
        private Label role = new Label();
        private Label transition = new Label();
        private Label resource = new Label();
        private Label index = new Label();

        public EventListCell() {
            super();

            VBox vBox = new VBox(activityName,timestamp,role,transition,resource);
            content = new HBox(index, vBox);
            content.setSpacing(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.getStyleClass().add("trace-info");
            activityName.getStyleClass().add("trace-info-activity-label");
            timestamp.getStyleClass().add("trace-info-content");
            role.getStyleClass().add("trace-info-content");
            transition.getStyleClass().add("trace-info-content");
            resource.getStyleClass().add("trace-info-content");
            index.getStyleClass().add("trace-info-index");
        }

        @Override
        protected void updateItem(EventInfo item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) { // <== test for null item and empty parameter
            	activityName.setText(item.getLabel());
            	timestamp.setText(item.getEvent().getTimestamp().toString());
            	role.setText(item.getEvent().getRole());
            	transition.setText(item.getEvent().getTransition());
            	resource.setText(item.getEvent().getResource());
            	index.setText(item.getIndex().toString());
                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }
	
	private void generateLog(Boolean save) {

		// generate and store Log
		if(save && logMak == null) {
			
			if(!checkInput()) {
				return;
			}
			eventLogList.clear();

			logMakParameters = new LogMakerParameters(minEvents, maxEvents, tracesNumber);
			logMak = new MinerFulLogMaker(logMakParameters);
			
			// init FileChooser and set extension-filter
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Event-Log");
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT (*.txt)", "*.txt");
			fileChooser.getExtensionFilters().add(extFilter);
			extFilter = new FileChooser.ExtensionFilter("XES (*.xes)", "*.xes");
			fileChooser.getExtensionFilters().add(extFilter);
			extFilter = new FileChooser.ExtensionFilter("MXML (*.mxml)", "*.mxml");
			fileChooser.getExtensionFilters().add(extFilter);
		    
			
		    // open FileChooser and handle response
			File saveFile = fileChooser.showSaveDialog(new Stage());
			if(saveFile != null) {
				String extension = saveFile.getName().substring(saveFile.getName().lastIndexOf(".") + 1, saveFile.getName().length());
				
				logMakParameters.outputEncoding = MinerfulGuiUtil.determineEncoding(extension.toLowerCase());
				logMakParameters.outputLogFile = saveFile;
				
				// set up ProgressForm
				ProgressForm progressForm = new ProgressForm("Create Log!");
				
				Task<XLog> createLog = MinerfulGuiUtil.generateLog(logMak, modelInfo.getProcessModel());
				progressForm.activateProgress(createLog);
				new Thread(createLog).start();
				
				createLog.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		            @Override
		            public void handle(WorkerStateEvent event) {
		            	try {
		            		XLog log = createLog.get();
							Iterator<XTrace> it = log.iterator();
							Integer number=0;
							while(it.hasNext()) {
								number++;
								XTrace xtrace = it.next();
								eventLogList.add(new TraceInfo("Trace " + number, xtrace));
							}
							logMak.storeLog();
		    				progressForm.closeProgressForm();
		    			} catch (InterruptedException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			} catch (ExecutionException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		        });
			}
			
		} else if(save && logMak != null) {
			// init FileChooser and set extension-filter
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Event-Log");
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT (*.txt)", "*.txt");
			fileChooser.getExtensionFilters().add(extFilter);
			extFilter = new FileChooser.ExtensionFilter("XES (*.xes)", "*.xes");
			fileChooser.getExtensionFilters().add(extFilter);
			extFilter = new FileChooser.ExtensionFilter("MXML (*.mxml)", "*.mxml");
			fileChooser.getExtensionFilters().add(extFilter);
		    
			
		    // open FileChooser and handle response
			File saveFile = fileChooser.showSaveDialog(new Stage());
			if(saveFile != null) {
				String extension = saveFile.getName().substring(saveFile.getName().lastIndexOf(".") + 1, saveFile.getName().length());
				
				logMakParameters.outputEncoding = MinerfulGuiUtil.determineEncoding(extension.toLowerCase());
				logMakParameters.outputLogFile = saveFile;
				
				//only store Log
				try {
					logMak.storeLog();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
		} else {
			
			if(!checkInput()) {
				return;
			}
			
			// only generate Log
			logMakParameters = new LogMakerParameters(minEvents, maxEvents, tracesNumber);
			logMak = new MinerFulLogMaker(logMakParameters);
			eventLogList.clear();
			
			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm("Create Log!");
			
			Task<XLog> createLog = MinerfulGuiUtil.generateLog(logMak, modelInfo.getProcessModel());
			progressForm.activateProgress(createLog);
			new Thread(createLog).start();
			
			createLog.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent event) {
	            	try {
	            		XLog log = createLog.get();
	    				Iterator<XTrace> it = log.iterator();
	    				Integer number=0;
	    				while(it.hasNext()) {
	    					number++;
	    					XTrace xtrace = it.next();
	    					eventLogList.add(new TraceInfo("Trace " + number, xtrace));
	    				}
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
		}

	}
	
	private Boolean checkInput() {
		minEvents = Integer.parseInt(minEventsPerTrace.getText());
		maxEvents = Integer.parseInt(maxEventsPerTrace.getText());
		tracesNumber = Long.parseLong(tracesInLog.getText());
		
		if(minEvents > maxEvents) {
			MinerfulGuiUtil.displayAlert("Input Error", "Input Error", "Maximum number of events per trace has to be less than the minimum number of events per trace!", AlertType.ERROR);
			return false;
		} else if(tracesNumber == 0) {
			MinerfulGuiUtil.displayAlert("Input Error", "Input Error", "Number of generated Traces should not be 0!", AlertType.ERROR);
			return false;
		}
		
		return true;
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

}

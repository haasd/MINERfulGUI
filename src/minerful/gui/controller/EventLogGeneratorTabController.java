package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.common.ValidationEngine;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;

public class EventLogGeneratorTabController extends AbstractController implements Initializable {
	
	@FXML
	TextField minEventsPerTrace;
	
	@FXML
	TextField maxEventsPerTrace;
	
	@FXML
	TextField tracesInLog;
	
	@FXML
	Pagination pagination;
	
	@FXML
	TableView<String> traceLog;
	
	private ModelInfo modelInfo;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		minEventsPerTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		maxEventsPerTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		tracesInLog.setTextFormatter(ValidationEngine.getNumericFilter());
	}
	
	@FXML
	public void generateEventLog(ActionEvent event) {
		
		Integer minEvents = Integer.parseInt(minEventsPerTrace.getText());
		Integer maxEvents = Integer.parseInt(maxEventsPerTrace.getText());
		Long tracesNumber = Long.parseLong(tracesInLog.getText());
		
		if(minEvents > maxEvents) {
			MinerfulGuiUtil.displayAlert("Input Error", "Input Error", "Maximum number of events per trace has to be less than the minimum number of events per trace!");
			return;
		} else if(tracesNumber == 0) {
			MinerfulGuiUtil.displayAlert("Input Error", "Input Error", "Number of generated Traces should not be 0!");
			return;
		}
		
		LogMakerParameters logMakParameters = new LogMakerParameters(minEvents, maxEvents, tracesNumber);
	
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

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
			
			try {
				// set up ProgressForm
				ProgressForm progressForm = new ProgressForm("Create Log!");
				
				Task<XLog> createLog = MinerfulGuiUtil.generateLog(logMak, modelInfo.getProcessModel());
				progressForm.activateProgress(createLog);
				new Thread(createLog).start();
				XLog log = createLog.get();
				Iterator<XTrace> it = log.iterator();
				while(it.hasNext()) {
					XTrace xtrace = it.next();
				}
				
				logMak.storeLog();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

}

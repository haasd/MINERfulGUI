package minerfulgui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.MinerFulMinerLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerfulgui.common.GuiConstants;
import minerfulgui.common.MinerfulGuiUtil;
import minerfulgui.common.ModelInfo;
import minerfulgui.common.ProgressForm;
import minerfulgui.common.ValidationEngine;
import minerfulgui.service.loginfo.EventFilter;

public class AutomataGeneratorTabController extends AbstractController implements Initializable {

	Logger logger = Logger.getLogger(AutomataGeneratorTabController.class);

	private final ObservableList<String> logInfos = FXCollections.observableArrayList();

	private final ObservableList<EventFilter> eventInfos = FXCollections.observableArrayList();

	private ModelInfo modelInfo;

	private ProcessModel processModel;

	private int maxTraceNumber = 0;

	private LogParser currentLogParser;

	private List<String> automaton = new ArrayList<>();

	@FXML
	WebView webView;

	@FXML
	ListView<String> logInfoList;

	@FXML
	TextField startAtTrace;

	@FXML
	TextField stopAtTrace;

	@FXML
	TableView<EventFilter> eventsTable;

	@FXML
	TableColumn<EventFilter, String> eventNameColumn;

	@FXML
	TableColumn<EventFilter, Boolean> filterColumn;

	@FXML
	TextField supportThresholdField;

	@FXML
	TextField confidenceThresholdField;

	@FXML
	TextField interestThresholdField;

	@FXML
	Slider supportThresholdSlider;

	@FXML
	Slider confidenceThresholdSlider;

	@FXML
	Slider interestThresholdSlider;

	@FXML
	Label numberOfConstraints;

	private EventClassification eventClassification = EventClassification.name;
	private Boolean reminingRequired = false;
	private Boolean classificationChanged = false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		logInfoList.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		eventsTable.setPlaceholder(new Label(GuiConstants.NO_EVENT_LOG));
		startAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		startAtTrace.setOnKeyPressed(onEnterPressed());
		stopAtTrace.setTextFormatter(ValidationEngine.getNumericFilter());
		stopAtTrace.setOnKeyPressed(onEnterPressed());

		MinerfulGuiUtil.setHeight(logInfoList, 125);
		MinerfulGuiUtil.setHeight(eventsTable, 200);

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
		confidenceThresholdSlider.valueProperty()
				.addListener(getSliderChangeListener(confidenceThresholdField, "confidence"));
		confidenceThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());

		interestThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(0.125));
		interestThresholdSlider.setValue(0.125);
		interestThresholdField.textProperty().addListener(getTextFieldChangeListener(interestThresholdSlider));
		interestThresholdField.setOnKeyPressed(onEnterPressed());
		interestThresholdSlider.valueProperty()
				.addListener(getSliderChangeListener(interestThresholdField, "interest"));
		interestThresholdSlider.setOnMouseReleased(onMouseReleaseSlider());

		// define eventTable
		eventNameColumn.setCellValueFactory(new PropertyValueFactory<EventFilter, String>("eventName"));
		eventNameColumn.setCellFactory(column -> {
			TableCell<EventFilter, String> cell = new TableCell<EventFilter, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (empty) {
						setText(null);
					} else {
						if (item != null) {
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

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public void displayAutomaton() throws FileNotFoundException, TransformerException, JAXBException {

		// Fill components
		initTab();
		processModel = modelInfo.getProcessModel();
		currentLogParser = modelInfo.getLogParser();

		InputLogCmdParameters inputParams = new InputLogCmdParameters();
		setTraceInfo(inputParams);

		createAutomaton(modelInfo.getProcessModel(), modelInfo.getLogParser());

	}

	private void createAutomaton(ProcessModel processModel, LogParser logParser) {
		Task<List<String>> createAutomatonTask = MinerfulGuiUtil.createAutomaton(processModel, logParser);

		// set up ProgressForm
		ProgressForm progressForm = new ProgressForm("Create Automata!");
		progressForm.activateProgress(createAutomatonTask);
		new Thread(createAutomatonTask).start();

		createAutomatonTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				progressForm.closeProgressForm();
				webView.getEngine().load((getClass().getClassLoader().getResource("javascript/test.html")).toString());

				webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

					@Override
					public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
						if (newValue == State.SUCCEEDED) {
							try {
								if (createAutomatonTask.get() != null) {
									automaton = createAutomatonTask.get();
									StringBuilder sb = new StringBuilder();
									for(String entry : automaton) {
										sb.append(entry);
										sb.append(" ");
									}
									
									webView.getEngine().executeScript("setModel('" + sb.toString() + "')");
								}
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});

				webView.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
					if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) {
						webView.setZoom(webView.getZoom() * 1.1);
					} else if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
						webView.setZoom(webView.getZoom() / 1.1);
					}
				});
			}
		});

		createAutomatonTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				progressForm.closeProgressForm();
				Throwable throwable = createAutomatonTask.getException();
				logger.error("Error occurred while creating automata", throwable);
				MinerfulGuiUtil.displayAlert("Error", "Stopped import!",
						"A problem occured during the creation of the automata!", AlertType.ERROR);
			}
		});
	}

	private void initTab() {
		// Set Log-Infos
		setLogInfos();
		// Set Event-Infos
		setEventInfos();
		// Set Threshold-Values
		setThresholdValues();
		numberOfConstraints.setText(String.valueOf(modelInfo.getProcessModel().getAllUnmarkedConstraints().size()));
		maxTraceNumber = modelInfo.getLogParser().length();
	}

	private void setEventInfos() {
		TaskCharArchive taskArchive = modelInfo.getLogParser().getTaskCharArchive();
		stopAtTrace.setText(String.valueOf(modelInfo.getLogParser().length()));

		Set<TaskChar> taskSet = taskArchive.getCopyOfTaskChars();
		for (TaskChar taskChar : taskSet) {
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

		eventsTable.setItems(eventInfos);
	}

	private void setThresholdValues() {
		supportThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(modelInfo.getSupportThreshold()));
		supportThresholdSlider.setValue(modelInfo.getSupportThreshold());
		confidenceThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(modelInfo.getConfidenceThreshold()));
		confidenceThresholdSlider.setValue(modelInfo.getConfidenceThreshold());
		interestThresholdField.setTextFormatter(ValidationEngine.getDoubleFilter(modelInfo.getInterestThreshold()));
		interestThresholdSlider.setValue(modelInfo.getInterestThreshold());
	}

	private void setLogInfos() {
		logInfos.add(GuiConstants.FILENAME + new File(modelInfo.getSaveName()).getName());
		logInfos.add(GuiConstants.NUMBER_OF_EVENTS + modelInfo.getLogParser().numberOfEvents());
		logInfos.add(GuiConstants.NUMBER_OF_TRACES + modelInfo.getLogParser().length());
		logInfos.add(GuiConstants.SHORTEST_TRACE + modelInfo.getLogParser().minimumTraceLength());
		logInfos.add(GuiConstants.LONGEST_TRACE + modelInfo.getLogParser().maximumTraceLength());

		logInfoList.setItems(logInfos);
	}

	private void setTraceInfo(InputLogCmdParameters inputParams) {
		if (startAtTrace != null && startAtTrace.getText() != "" && stopAtTrace != null
				&& stopAtTrace.getText() != "") {
			if (Integer.parseInt(startAtTrace.getText()) > Integer.parseInt(stopAtTrace.getText())
					|| Integer.parseInt(startAtTrace.getText()) > maxTraceNumber) {
				startAtTrace.setText(String.valueOf(0));
				inputParams.startFromTrace = 0;
				startAtTrace.setStyle("-fx-focus-color: red ");
			} else {
				inputParams.startFromTrace = Integer.parseInt(startAtTrace.getText());
			}

			if (Integer.parseInt(stopAtTrace.getText()) > maxTraceNumber) {
				stopAtTrace.setText(String.valueOf(maxTraceNumber));
				inputParams.subLogLength = maxTraceNumber - 1;
			} else {
				inputParams.subLogLength = Integer.parseInt(stopAtTrace.getText()) - 1;
			}
		} else {
			inputParams.startFromTrace = 0;
			inputParams.subLogLength = maxTraceNumber - 1;
		}
	}

	private ChangeListener<String> getTextFieldChangeListener(Slider slider) {
		return new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				if (newValue != null && !newValue.isEmpty()) {
					slider.setValue(Double.parseDouble(newValue));
					if (Double.parseDouble(newValue) < Double.parseDouble(oldValue)) {
					}
				}
			}
		};
	}

	private ChangeListener<Number> getSliderChangeListener(TextField textField, String parameter) {
		return new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				String doubleValue = String.format("%.3f", newValue.doubleValue());
				doubleValue = doubleValue.replace(",", ".");
				textField.setText(doubleValue);
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

	private EventHandler<KeyEvent> onEnterPressed() {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					reminingRequired = true;
					updateModel();
				}
			}
		};
	}

	private void updateModel() {

		if (processModel != null) {
			logger.info("Update Parameters: " + supportThresholdField.getText() + " "
					+ confidenceThresholdField.getText() + " " + interestThresholdField.getText());

			InputLogCmdParameters inputParams = new InputLogCmdParameters();
			inputParams.eventClassification = eventClassification;
			inputParams.inputLogFile = new File(modelInfo.getPath());
			inputParams.inputLanguage = MinerfulGuiUtil.determineInputEncoding(modelInfo.getPath());
			MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
			SystemCmdParameters systemParams = new SystemCmdParameters();

			PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
			postParams.supportThreshold = Double.parseDouble(supportThresholdField.getText());
			postParams.confidenceThreshold = Double.parseDouble(confidenceThresholdField.getText());
			postParams.interestFactorThreshold = Double.parseDouble(interestThresholdField.getText());

			minerFulParams.activitiesToExcludeFromResult = new ArrayList<>();
			setTraceInfo(inputParams);

			// exclude all events that aren't marked
			for (EventFilter filter : eventInfos) {
				if (!filter.getSelected().isSelected()) {
					minerFulParams.activitiesToExcludeFromResult.add(filter.getEventName());
				}
			}

			if (!reminingRequired) {
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
							updateInfos();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

			} else {
				MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams,
						systemParams);

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
							currentLogParser = miFuMiLa.getLogParser();
							updateInfos();

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

	}

	private void updateInfos() {
		numberOfConstraints.setText(String.valueOf(processModel.getAllUnmarkedConstraints().size()));

		if (classificationChanged) {
			TaskCharArchive taskArchive = currentLogParser.getTaskCharArchive();
			Set<TaskChar> taskSet = taskArchive.getCopyOfTaskChars();
			eventInfos.clear();
			for (TaskChar taskChar : taskSet) {
				EventFilter filter = new EventFilter(taskChar.getName(), true);
				filter.getSelected().selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						reminingRequired = true;
						updateModel();
					}
				});

				eventInfos.add(filter);
			}
		}

		createAutomaton(modelInfo.getProcessModel(), currentLogParser);

		classificationChanged = false;
		reminingRequired = false;
	}

	@FXML
	private void exportFile() {
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(GuiConstants.EXPORT_AUTOMATON);
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DOT", "*.dot");
		fileChooser.getExtensionFilters().add(extFilter);
		extFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
		fileChooser.getExtensionFilters().add(extFilter);

		// open FileChooser and handle response
		File saveFile = fileChooser.showSaveDialog(new Stage());
		if (saveFile != null) {
			String path = saveFile.getAbsolutePath();
			File outputFile = new File(path);

			logger.info("Save as File: " + path);

			String fileName = saveFile.getName();
			String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, saveFile.getName().length());

			logger.info("Saving...");
			boolean customOutput = false;

			switch (fileExtension.toLowerCase()) {
			case "pdf":

				break;
			case "dot":
				try {
					File file = new File(path);
					FileWriter fileWriter = new FileWriter(file);
					for(String entry : automaton) {
						fileWriter.write(entry + System.lineSeparator());
					}
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			MinerfulGuiUtil.displayAlert("Information", "Finished export", "Finished export of: " + outputFile,
					AlertType.INFORMATION);
			return;

		} else {
			logger.info("Modelsaving canceled!");
		}
	}

}

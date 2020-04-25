package minerful.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class SavedEventLogController extends AbstractController implements Initializable {

	Logger logger = Logger.getLogger(SavedEventLogController.class);

	private final ObservableList<LogInfo> logInfos = FXCollections.observableArrayList();

	@FXML
	TableView<LogInfo> modelTable;

	@FXML
	TableColumn<LogInfo, Date> timestampColumn;

	@FXML
	TableColumn<LogInfo, String> modelnameColumn;

	private LogInfo selectedRow;
	
	private boolean pressedOkay = false;
	
	@FXML
	Button okayButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		modelTable.setPlaceholder(new Label(GuiConstants.NO_LOG_LOADED));

		// define date-column and set format
		timestampColumn.setCellValueFactory(new PropertyValueFactory<LogInfo, Date>("date"));

		timestampColumn.setCellFactory(column -> {
			TableCell<LogInfo, Date> cell = new TableCell<LogInfo, Date>() {
				private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

				@Override
				protected void updateItem(Date item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						if (item != null)
							this.setText(format.format(item));
					}
				}
			};

			return cell;
		});

		// define eventLogTable
		modelnameColumn.setCellValueFactory(new PropertyValueFactory<LogInfo, String>("path"));
		modelnameColumn.setCellFactory(column -> {
			TableCell<LogInfo, String> cell = new TableCell<LogInfo, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (empty) {
						setText(null);
					} else {
						if (item != null) {
							File f = new File(item);
							this.setText(f.getName());
						}
					}
				}
			};

			return cell;
		});

		modelTable.setRowFactory(tv -> {
			TableRow<LogInfo> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
					selectRow(row.getItem());
					okayButton.setDisable(false);
				}
			});
			return row;
		});

		modelTable.setItems(logInfos);

	}

	@FXML
	public void loadLog(ActionEvent event) {
		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Event-Log");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XES, MXML, txt, xes.gz", "*.xes",
				"*.mxml", "*.txt", "*.xes.gz");
		fileChooser.getExtensionFilters().add(extFilter);

		// open FileChooser and handle response
		File selectedFile = fileChooser.showOpenDialog(new Stage());

		if (selectedFile != null) {
			logger.info("Process File: " + selectedFile.getAbsolutePath());

			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm("Load Log-File!");

			// create Task bind it to ProgressForm and start
			LogParserService logParser = new LogParserServiceImpl(selectedFile.getAbsolutePath());
			Task<LogInfo> parseLog = logParser.parseLog();

			progressForm.activateProgress(parseLog);
			new Thread(parseLog).start();

			parseLog.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					try {
						getMainController().addLoadedLogFile(parseLog.get());
						progressForm.closeProgressForm();
						updateEntries();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			parseLog.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					progressForm.closeProgressForm();
					Throwable throwable = parseLog.getException();
					logger.error("Error occurred while importing file: " + selectedFile.getAbsolutePath(), throwable);
					MinerfulGuiUtil.displayAlert("Error", "Stopped Import!",
							"Error occurred while importing file: " + selectedFile.getAbsolutePath(), AlertType.ERROR);
				}
			});
		} else {
			logger.info("Fileselection canceled!");
		}
	}

	public void selectRow(LogInfo row) {
		this.selectedRow = row;
	}

	public LogInfo getSelectedRow() {
		if (pressedOkay) {
			return selectedRow;
		} else {
			return null;
		}
	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	private void closeStage(MouseEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}
	
	public void pressOk(ActionEvent event) {
		pressedOkay = true;
		closeStage(event);
	}

	public void pressCancel(ActionEvent event) {
		closeStage(event);
	}

	public void updateEntries() {
		logInfos.clear();
		logInfos.setAll(getMainController().getLoadedLogFiles());
	}
}

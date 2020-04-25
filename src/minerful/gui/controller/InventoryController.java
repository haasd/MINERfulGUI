package minerful.gui.controller;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import minerful.gui.common.GuiConstants;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class InventoryController extends AbstractController implements Initializable {

	Logger logger = Logger.getLogger(InventoryController.class);

	@FXML
	ListView<LogInfo> eventLogList;

	@FXML
	ListView<ModelInfo> modelsList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		eventLogList.setCellFactory(new Callback<ListView<LogInfo>, ListCell<LogInfo>>() {
			@Override
			public ListCell<LogInfo> call(ListView<LogInfo> listView) {
				return new EventLogListCell();
			}
		});
	}

	@Override
	public void performAfterInit() {
		// TODO Auto-generated method stub
		super.performAfterInit();
		eventLogList.setItems(getMainController().getLoadedLogFiles());

		eventLogList.setCellFactory(new Callback<ListView<LogInfo>, ListCell<LogInfo>>() {
			@Override
			public ListCell<LogInfo> call(ListView<LogInfo> listView) {
				return new EventLogListCell();
			}
		});

		modelsList.setItems(getMainController().getSavedProcessModels());

		modelsList.setCellFactory(new Callback<ListView<ModelInfo>, ListCell<ModelInfo>>() {
			@Override
			public ListCell<ModelInfo> call(ListView<ModelInfo> listView) {
				return new ModelListCell();
			}
		});
	}

	private class EventLogListCell extends ListCell<LogInfo> {
		private HBox content;
		private Label logName = new Label();
		private Label logDate = new Label();
		private FontIcon icon = new FontIcon("typ-document");
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		private ContextMenu contextMenu = new ContextMenu();

		public EventLogListCell() {
			super();
			icon.setIconSize(30);
			icon.setIconColor(Paint.valueOf("white"));
			VBox vbox = new VBox(logName, logDate);
			logName.getStyleClass().add("list-element-name");
			logDate.getStyleClass().add("list-element-date");

			content = new HBox(icon, vbox);
			content.getStyleClass().add("list-element");
			content.setSpacing(10);
			content.setAlignment(Pos.CENTER_LEFT);

			MenuItem openInDiscover = new MenuItem("Open Log in Discover");
			openInDiscover.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					logger.info("Open Log in Discover");
					getMainController().openLogInArea("discover", getItem());
				}
			});

			contextMenu.getItems().add(openInDiscover);

			content.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

				@Override
				public void handle(ContextMenuEvent event) {
					contextMenu.show(content, event.getScreenX(), event.getScreenY());
				}
			});

		}

		@Override
		protected void updateItem(LogInfo log, boolean empty) {
			super.updateItem(log, empty);
			if (log != null && !empty) { // <== test for null item and empty parameter
				logName.setText(log.toString());
				logDate.setText(format.format(log.getDate()));
				setGraphic(content);
			} else {
				setGraphic(null);
			}
		}
	}

	private class ModelListCell extends ListCell<ModelInfo> {
		private HBox content;
		private Label logName = new Label();
		private Label logDate = new Label();
		private FontIcon icon = new FontIcon("typ-flow-children");
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		private ContextMenu contextMenu = new ContextMenu();

		public ModelListCell() {
			super();
			icon.setIconSize(30);
			icon.setIconColor(Paint.valueOf("white"));
			VBox vbox = new VBox(logName, logDate);
			logName.getStyleClass().add("list-element-name");
			logDate.getStyleClass().add("list-element-date");
			content = new HBox(icon, vbox);
			content.getStyleClass().add("list-element");
			content.setSpacing(10);
			content.setAlignment(Pos.CENTER_LEFT);

			MenuItem openInDiscover = new MenuItem("Open Model in Model-Generator");
			openInDiscover.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					logger.info("Open Model in Model-Generator");
					getMainController().openModelInArea("modelgenerator", getItem());
				}
			});

			contextMenu.getItems().add(openInDiscover);

			content.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

				@Override
				public void handle(ContextMenuEvent event) {
					contextMenu.show(content, event.getScreenX(), event.getScreenY());
				}
			});

		}

		@Override
		protected void updateItem(ModelInfo modelInfo, boolean empty) {
			super.updateItem(modelInfo, empty);
			if (modelInfo != null && !empty) { // <== test for null item and empty parameter
				logName.setText(modelInfo.getSaveName());
				logDate.setText(format.format(modelInfo.getSaveDate()));
				setGraphic(content);
			} else {
				setGraphic(null);
			}
		}
	}

	@FXML
	public void importEventLog(ActionEvent event) {
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

	@FXML
	public void importModel(ActionEvent event) {

		// init FileChooser and set extension-filter
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(GuiConstants.IMPORT_PROCESS_MAP);
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP", "*.zip");
		fileChooser.getExtensionFilters().add(extFilter);

		File openFile = fileChooser.showOpenDialog(new Stage());

		if (openFile != null) {
			logger.info("Process File: " + openFile.getAbsolutePath());
			Task<ModelInfo> modelInfoTask = MinerfulGuiUtil.importModel(openFile);

			// set up ProgressForm
			ProgressForm progressForm = new ProgressForm("Import process model!");
			progressForm.activateProgress(modelInfoTask);
			new Thread(modelInfoTask).start();

			modelInfoTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					try {
						getMainController().addSavedProcessModels(modelInfoTask.get());
						progressForm.closeProgressForm();
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			modelInfoTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					progressForm.closeProgressForm();
					Throwable throwable = modelInfoTask.getException();
					logger.error("Error occurred while importing file: " + openFile.getAbsolutePath(), throwable);
					MinerfulGuiUtil.displayAlert("Error", "Stopped import!",
							String.format("A problem occured during the import of the model: %s", openFile.getName()),
							AlertType.ERROR);
				}
			});

		} else {
			logger.info("Fileselection canceled!");
		}
	}

}

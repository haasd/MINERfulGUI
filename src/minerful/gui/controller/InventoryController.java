package minerful.gui.controller;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import minerful.gui.common.ModelInfo;
import minerful.gui.service.loginfo.LogInfo;

public class InventoryController extends AbstractController implements Initializable{
	
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

        public EventLogListCell() {
            super();
            icon.setIconSize(30);
            icon.setIconColor(Paint.valueOf("white"));
            VBox vbox = new VBox(logName,logDate);
            logName.getStyleClass().add("list-element-name");
            logDate.getStyleClass().add("list-element-date");
            content = new HBox(icon, vbox);
            content.getStyleClass().add("list-element");
            content.setSpacing(10);
            content.setAlignment(Pos.CENTER_LEFT);
            
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

        public ModelListCell() {
            super();
            icon.setIconSize(30);
            icon.setIconColor(Paint.valueOf("white"));
            VBox vbox = new VBox(logName,logDate);
            logName.getStyleClass().add("list-element-name");
            logDate.getStyleClass().add("list-element-date");
            content = new HBox(icon, vbox);
            content.getStyleClass().add("list-element");
            content.setSpacing(10);
            content.setAlignment(Pos.CENTER_LEFT);
            
        }

        @Override
        protected void updateItem(ModelInfo log, boolean empty) {
            super.updateItem(log, empty);
            if (log != null && !empty) { // <== test for null item and empty parameter
            	logName.setText(log.getSaveName());
            	logDate.setText(format.format(log.getSaveDate()));
            	setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }

}
package minerful.gui.controller;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ProgressForm;
import minerful.gui.service.loginfo.LogInfo;
import minerful.gui.service.logparser.LogParserService;
import minerful.gui.service.logparser.LogParserServiceImpl;

public class SavedModelController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(SavedModelController.class);
	
	private final ObservableList<ModelInfo> modelInfos =
	        FXCollections.observableArrayList();
	
	@FXML
	TableView<ModelInfo> modelTable;
	
	@FXML
	TableColumn<ModelInfo, Date> timestampColumn;
	
	@FXML
	TableColumn<ModelInfo, String> modelnameColumn;
	
	private ModelInfo selectedRow;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		modelTable.setPlaceholder(new Label(GuiConstants.NO_MODEL_LOADED));

		// define date-column and set format
		timestampColumn.setCellValueFactory(
		                new PropertyValueFactory<ModelInfo, Date>("saveDate"));
				
		timestampColumn.setCellFactory(column -> {
            TableCell<ModelInfo, Date> cell = new TableCell<ModelInfo, Date>() {
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
		
		// define eventLogTable
		modelnameColumn.setCellValueFactory(
		                new PropertyValueFactory<ModelInfo, String>("saveName"));
		modelnameColumn.setCellFactory(column -> {
            TableCell<ModelInfo, String> cell = new TableCell<ModelInfo, String>() {

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
		
		modelTable.setRowFactory(tv -> {
		    TableRow<ModelInfo> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (! row.isEmpty() && event.getButton()==MouseButton.PRIMARY 
		             && event.getClickCount() == 2) {

		        	selectRow(row.getItem());
		        	closeStage(event);
		        }
		    });
		    return row ;
		});
		
		modelTable.setItems(modelInfos);
		
	}
	
	public void selectRow(ModelInfo row) {
		this.selectedRow = row;
	}
	
	public ModelInfo getSelectedRow() {
		return selectedRow;
	}
	
	private void closeStage(MouseEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
	
	public void updateEntries() {
		modelInfos.clear();
		modelInfos.setAll(getMainController().getSavedProcessModels());
	}
}

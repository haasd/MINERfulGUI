package minerful.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.FitnessCheckInfo;
import minerful.gui.common.MinerfulGuiUtil;
import minerful.gui.common.ModelInfo;
import minerful.gui.common.ValidationEngine;
import minerful.gui.service.loginfo.EventFilter;
import minerful.gui.service.loginfo.LogInfo;

public class FitnessCheckerTabController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(FitnessCheckerTabController.class);
	
	private final ObservableList<FitnessCheckInfo> fitnessCheckInfoTable =
	        FXCollections.observableArrayList();
	
	@FXML
	TableView<FitnessCheckInfo> fitnessCheckTable;
	
	@FXML
	TableColumn<FitnessCheckInfo, String> templateColumn;
	
	@FXML
	TableColumn<FitnessCheckInfo, String> constraintColumn;
	
	@FXML
	TableColumn<FitnessCheckInfo, Double> fitnessColumn;
	
	@FXML
	TableColumn<FitnessCheckInfo, Integer> fullSatisfactionsColumn;
	
	@FXML
	TableColumn<FitnessCheckInfo, Integer> vacuousSatisfactionsColumn;
	
	@FXML
	TableColumn<FitnessCheckInfo, Integer> violationsColumn;
	
	private ModelFitnessEvaluation mfe;
	
	private ModelInfo modelInfo;
	
	private LogInfo eventLogInfo;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		templateColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, String>("template"));
		
		constraintColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, String>("constraint"));
		
		fitnessColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, Double>("fitness"));
		
		fullSatisfactionsColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, Integer>("fullSatisfactions"));
		
		vacuousSatisfactionsColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, Integer>("vacuousSatisfactions"));
		
		violationsColumn.setCellValueFactory(
                new PropertyValueFactory<FitnessCheckInfo, Integer>("violations"));
		
		fitnessCheckTable.setItems(fitnessCheckInfoTable);
	}
	
	public void updateFitnessResults() {
		fitnessCheckInfoTable.clear();
		fitnessCheckInfoTable.addAll(MinerfulGuiUtil.deriveFitnessCheckInfo(mfe));
	
	}
	
	public ModelFitnessEvaluation getMfe() {
		return mfe;
	}

	public void setMfe(ModelFitnessEvaluation mfe) {
		this.mfe = mfe;
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public LogInfo getEventLogInfo() {
		return eventLogInfo;
	}

	public void setEventLogInfo(LogInfo eventLogInfo) {
		this.eventLogInfo = eventLogInfo;
	}
}

package minerful.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import minerful.concept.constraint.Constraint;
import minerful.gui.common.ModelInfo;
import minerful.gui.service.loginfo.EventFilter;

public class ModelGeneratorTabController extends AbstractController implements Initializable {
	
	Logger logger = Logger.getLogger(ModelGeneratorTabController.class);
	
	private ModelInfo modelInfo;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//handle graphical representation of graph
		//handle bag and taskchar/activities
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

}

package minerful.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

public class TutorialController extends AbstractController implements Initializable {
	
	@FXML
	private GridPane infoDiscover;
	
	@FXML
	private GridPane infoSimulate;
	
	@FXML
	private GridPane infoSimplify;
	
	@FXML
	private GridPane infoGenerateAutomata;
	
	@FXML
	private GridPane infoPerformCheck;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		infoDiscover.setOnMouseClicked(e -> ((StartPageController) this.getMainController()).openDiscovery());
		infoSimulate.setOnMouseClicked(e -> ((StartPageController) this.getMainController()).openEventLogGenerator());
		infoSimplify.setOnMouseClicked(e -> ((StartPageController) this.getMainController()).openSimplifier());
		infoGenerateAutomata.setOnMouseClicked(e -> ((StartPageController) this.getMainController()).openAutomataGenerator());
		infoPerformCheck.setOnMouseClicked(e -> ((StartPageController) this.getMainController()).openFitnessChecker());

	}
	
	

}

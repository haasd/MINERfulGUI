package minerful.gui.controller;

import javafx.stage.Stage;

public abstract class AbstractController {
	
	private StartPageController mainController;
	
	private Stage stage;
	
	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setMainController(StartPageController mainController) {
        this.mainController = mainController;
    }

	public StartPageController getMainController() {
		return mainController;
	}

}

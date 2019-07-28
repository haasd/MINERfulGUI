package minerful.gui.controller;

public abstract class AbstractController {
	
	private StartPageController mainController;
	
	public void setMainController(StartPageController mainController) {
        this.mainController = mainController;
    }

	public StartPageController getMainController() {
		return mainController;
	}

}

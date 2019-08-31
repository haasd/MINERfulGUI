package minerful.gui.common;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/*
 * Simple PopUp-Window that displays progress of a task
 */
public class ProgressForm {
	
	private Stage dialogStage;
	private ProgressIndicator progressIndicator;
	private String progressText;
	
	public ProgressForm(String progressText) {
		this.progressText = progressText;
		dialogStage = new Stage();
		dialogStage.setResizable(false);
		dialogStage.setHeight(100);
		dialogStage.setWidth(250);
		progressIndicator = new ProgressIndicator();
		progressIndicator.setProgress(0);
        progressIndicator.progressProperty().unbind();
        progressIndicator.setVisible(true);
		
		final HBox hBox = new HBox();
	    hBox.setSpacing(5);
	    hBox.setAlignment(Pos.CENTER);
	    hBox.getChildren().addAll(new Label(progressText), progressIndicator);

	    Scene scene = new Scene(hBox);
	    dialogStage.setScene(scene);
	    dialogStage.show();
	    
	}
	
	public void closeProgressForm() {
		if (dialogStage != null) {
            dialogStage.hide();
        }
	}
	
	public void activateProgress(Task<?> task)  {
        progressIndicator.progressProperty().bind(task.progressProperty());
    }      

}

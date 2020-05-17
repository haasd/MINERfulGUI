package minerfulgui.common;

import com.sun.glass.ui.Screen;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
		dialogStage.initStyle(StageStyle.UNDECORATED);
		dialogStage.setX(Screen.getMainScreen().getWidth() - 300);
		dialogStage.setY(100);
		progressIndicator = new ProgressIndicator();
		progressIndicator.setProgress(0);
        progressIndicator.progressProperty().unbind();
        progressIndicator.setVisible(true);
		
		final HBox hBox = new HBox();
	    hBox.setSpacing(5);
	    hBox.setAlignment(Pos.CENTER);
	    hBox.getChildren().addAll(new Label(progressText), progressIndicator);
	    hBox.getStyleClass().add("loading");

	    Scene scene = new Scene(hBox);
	    scene.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
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

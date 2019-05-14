package minerful.gui.common;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/*
 * Simple PopUp-Window that displays progress of a task
 */
public class ProgressForm {
	
	private Stage dialogStage;
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	
	public ProgressForm() {
		dialogStage = new Stage();
		dialogStage.setResizable(false);
		dialogStage.setHeight(100);
		dialogStage.setWidth(250);
		progressBar = new ProgressBar(0);
		progressIndicator = new ProgressIndicator(0);
		
		final HBox hBox = new HBox();
	    hBox.setSpacing(5);
	    hBox.setAlignment(Pos.CENTER);
	    progressBar.prefWidthProperty().bind(hBox.widthProperty().subtract(50));
	    hBox.getChildren().addAll(progressBar, progressIndicator);

	    Scene scene = new Scene(hBox);
	    dialogStage.setScene(scene);
	    
	}
	
	public void activateProgressBar(final Task<?> task)  {
        progressBar.progressProperty().bind(task.progressProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());
        dialogStage.show();
        
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                if (dialogStage != null) {
                    dialogStage.hide();
                }
            }
        });
    }

}

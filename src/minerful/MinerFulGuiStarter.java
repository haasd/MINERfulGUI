package minerful;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * MINERful GUI Starter
 */
@SpringBootApplication
public class MinerFulGuiStarter extends Application {
	
	Logger logger = Logger.getLogger(MinerFulGuiStarter.class);

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	logger.info("Load Application!");
        scene = new Scene(loadFXML("pages/Startpage"));
        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
                logger.info("Close Application!");
            }
        });
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MinerFulGuiStarter.class.getClassLoader().getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    public static void main(String[] args) {
        launch();
    }

}

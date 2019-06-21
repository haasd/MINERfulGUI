package minerful;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

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

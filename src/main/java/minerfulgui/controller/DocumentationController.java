package minerfulgui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class DocumentationController implements Initializable  {
	
	@FXML
	private WebView webView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		webView.getEngine().load((getClass().getClassLoader().getResource("documentation/index.html")).toString());
	}
	
}

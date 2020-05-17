package minerfulgui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class DocumentationController {
	
	@FXML
	private ScrollPane scrollPane;
	
	@FXML
	private VBox box;
	
	@FXML
	private void scrollToElement(ActionEvent event) {
		 Hyperlink link = (Hyperlink) event.getSource();

		 Bounds bounds = scrollPane.getViewportBounds();
		 for(Node node : box.getChildrenUnmodifiable()) {
			 if( node instanceof Label && ((Label) node).getText().equals(link.getText())) {
				 scrollPane.setVvalue(node.getLayoutY() * 
                         (1/(box.getHeight()-bounds.getHeight())));
			 }
		 }
	}

}

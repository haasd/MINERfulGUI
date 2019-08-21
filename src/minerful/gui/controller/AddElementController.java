package minerful.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import minerful.concept.constraint.Constraint;

public class AddElementController extends AbstractController implements Initializable {
	
	@FXML
	VBox vbox;
	
	@FXML
	VBox constraints;
	
	@FXML
	TextField activityname;
	
	private RadioButton typeAtMostOne;
	private RadioButton typeEnd;
	private RadioButton typeExactlyOne;
	private RadioButton typeExistenceConstraint;
	private RadioButton typeInit;
	private RadioButton typeParticipation;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// AtMostOne
		typeAtMostOne = new RadioButton("AtMostOne");
		typeAtMostOne.setSelected(false);
		
		// End
		typeEnd = new RadioButton("End");
		typeEnd.setSelected(false);
		
		// ExactlyOne
		typeExactlyOne = new RadioButton("ExactlyOne");
		typeExactlyOne.setSelected(false);
		
		// ExistenceConstraint
		typeExistenceConstraint = new RadioButton("ExistenceConstraint");
		typeExistenceConstraint.setSelected(false);
		
		// Init
		typeInit = new RadioButton("Init");
		typeInit.setSelected(false);
		
		// Participation
		typeParticipation = new RadioButton("Participation");
		typeParticipation.setSelected(false);
		
		constraints.getChildren().addAll(typeAtMostOne,typeEnd,typeExactlyOne,typeExistenceConstraint,typeInit,typeParticipation);
		
	}
	
	@FXML
	public void addActivity(ActionEvent event){
		if(activityname.getText() != null && !activityname.getText().isEmpty()) {
			closeStage(event);
		}
		
	}
	
	@FXML
	public void cancel(ActionEvent event){
		closeStage(event);
	}
	
	public String getActivityName() {
		return activityname.getText();
	}
	
	public List<Constraint> getAllSelectedConstraints(){
		List<Constraint> constraintList = new ArrayList<>();
		
		return constraintList;
	}
	
	private void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

}

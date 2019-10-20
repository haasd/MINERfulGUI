package minerful.gui.model;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import minerful.gui.controller.ModelGeneratorTabController;

public class EditActivityPane extends ScrollPane {

	private ModelGeneratorTabController processTab;
	private ActivityNode editedActivityNode;

	private VBox nameEditVBox = new VBox();
	private VBox structureEditVBox = new VBox();
	private VBox cardinalityEditVBox = new VBox();
	private VBox addDeleteExistenceConstraintBox = new VBox();

	private Label nameL;
	private TextArea activityNameTF;
	private Label structureL;

	private Button addExistenceConstraintB;
	private Button deleteExistenceConstraintB;

	private CheckBox initCB;
	private CheckBox endCB;

	private GridPane cardinalityGridPane;
	private Label cardinalityLabel;
	private RadioButton cardMin0RB, cardMin1RB;
	private RadioButton cardMax1RB, cardMaxAnyRB;
	private TextField cardMinMoreTF, cardMaxMoreTF;
	private ToggleGroup minGroup;
	private ToggleGroup maxGroup;
	private Label cardErrorLabel;

	public EditActivityPane(ModelGeneratorTabController processTab) {
		super();

		setFitToHeight(true);
		setFitToWidth(true);

		this.processTab = processTab;

		// STRUCTURE OF PANE TO EDIT ACTIVIY
		VBox contentOfActivityPane = new VBox(nameEditVBox, addDeleteExistenceConstraintBox, structureEditVBox,
				cardinalityEditVBox);
		setContent(contentOfActivityPane);
		contentOfActivityPane.setSpacing(10);

		nameL = new Label("Identifier:");
		nameL.getStyleClass().add("editpane-header");
		activityNameTF = new TextArea();
		nameEditVBox.getChildren().addAll(nameL, activityNameTF);
		nameEditVBox.getStyleClass().add("edit-name");
		nameEditVBox.setSpacing(10);
		activityNameTF.setWrapText(true);
		activityNameTF.setPrefWidth(180);
		activityNameTF.setPrefHeight(40);

		addExistenceConstraintB = new Button("Existence Constraint");
		deleteExistenceConstraintB = new Button("Existence Constraint");
		addDeleteExistenceConstraintBox.getChildren().addAll(addExistenceConstraintB);
		addDeleteExistenceConstraintBox.getStyleClass().add("existence-constraint-box");
		addDeleteExistenceConstraintBox.setSpacing(10);

		structureL = new Label("Structure:");
		structureL.getStyleClass().add("editpane-header");
		initCB = new CheckBox("INIT");
		endCB = new CheckBox("END");
		structureEditVBox.getChildren().addAll(structureL, initCB, endCB);
		structureEditVBox.getStyleClass().add("constraint-content");

		cardinalityGridPane = new GridPane();
		cardinalityGridPane.setAlignment(Pos.CENTER);
		ColumnConstraints columnConstraint = new ColumnConstraints();
		columnConstraint.setPercentWidth(25);
		cardinalityGridPane.getColumnConstraints().add(columnConstraint);
		
		columnConstraint = new ColumnConstraints();
		columnConstraint.setPercentWidth(18);
		cardinalityGridPane.getColumnConstraints().add(columnConstraint);
		
		columnConstraint = new ColumnConstraints();
		columnConstraint.setPercentWidth(18);
		cardinalityGridPane.getColumnConstraints().add(columnConstraint);
		
		columnConstraint = new ColumnConstraints();
		columnConstraint.setPercentWidth(21);
		cardinalityGridPane.getColumnConstraints().add(columnConstraint);
		
		columnConstraint = new ColumnConstraints();
		columnConstraint.setPercentWidth(18);
		cardinalityGridPane.getColumnConstraints().add(columnConstraint);
		
		cardinalityLabel = new Label("Cardinality:");
		cardinalityLabel.getStyleClass().add("editpane-header");

		cardinalityEditVBox.getChildren().add(cardinalityLabel);
		cardinalityEditVBox.getChildren().add(cardinalityGridPane);
		Label card0L, card1L, cardMoreL, cardAnyL, cardMinL, cardMaxL;
		card0L = new Label("0");
		card1L = new Label("1");
		cardMoreL = new Label("more");
		cardAnyL = new Label("*");
		cardMinL = new Label("Minimum:");
		cardMaxL = new Label("Maximum:");
		cardMinL.setMinWidth(40);
		cardMaxL.setMinWidth(40);
		cardMin0RB = new RadioButton();
		cardMin1RB = new RadioButton();
		cardMax1RB = new RadioButton();
		cardMaxAnyRB = new RadioButton();
		cardMinMoreTF = new TextField("");
		cardMaxMoreTF = new TextField("");

		cardErrorLabel = new Label("");
		cardErrorLabel.getStyleClass().add("errorMessageLabel");
		cardErrorLabel.setWrapText(true);
		cardErrorLabel.setPrefHeight(50);

		cardinalityGridPane.add(card0L, 1, 0);
		cardinalityGridPane.add(card1L, 2, 0);
		cardinalityGridPane.add(cardMoreL, 3, 0);
		cardinalityGridPane.add(cardAnyL, 4, 0);
		cardinalityGridPane.add(cardMinL, 0, 1);
		cardinalityGridPane.add(cardMaxL, 0, 2);
		cardinalityGridPane.add(cardMin0RB, 1, 1);
		cardinalityGridPane.add(cardMin1RB, 2, 1);
		cardinalityGridPane.add(cardMinMoreTF, 3, 1);
		cardinalityGridPane.add(cardMax1RB, 2, 2);
		cardinalityGridPane.add(cardMaxMoreTF, 3, 2);
		cardinalityGridPane.add(cardMaxAnyRB, 4, 2);
		cardinalityEditVBox.getChildren().add(cardErrorLabel);

		GridPane.setHalignment(card0L, HPos.CENTER);
		GridPane.setHalignment(card1L, HPos.CENTER);
		GridPane.setHalignment(cardMoreL, HPos.CENTER);
		GridPane.setHalignment(cardAnyL, HPos.CENTER);
		GridPane.setHalignment(cardMin0RB, HPos.CENTER);
		GridPane.setHalignment(cardMin1RB, HPos.CENTER);
		GridPane.setHalignment(cardMinMoreTF, HPos.CENTER);
		GridPane.setHalignment(cardMax1RB, HPos.CENTER);
		GridPane.setHalignment(cardMaxMoreTF, HPos.CENTER);
		GridPane.setHalignment(cardMaxAnyRB, HPos.CENTER);
		cardMinMoreTF.setPrefWidth(30);
		cardMaxMoreTF.setPrefWidth(30);

		Insets radioButtonInsets = new Insets(5, 0, 5, 5);
		cardMin0RB.setPadding(radioButtonInsets);
		cardMin1RB.setPadding(radioButtonInsets);
		cardMax1RB.setPadding(radioButtonInsets);
		cardMaxAnyRB.setPadding(radioButtonInsets);

		// Behavior MinGorup
		minGroup = new ToggleGroup();
		cardMin0RB.setToggleGroup(minGroup);
		cardMin1RB.setToggleGroup(minGroup);

		cardMin0RB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				if (cardMinMoreTF.getText() != "") {
					cardMinMoreTF.clear();
				}
				updateCardinalityOnNode();
			}
		});
		cardMin1RB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				if (cardMinMoreTF.getText() != "") {
					cardMinMoreTF.clear();
				}
				updateCardinalityOnNode();
			}
		});

		// only allow int values
		cardMinMoreTF.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				cardMinMoreTF.setText(newValue.replaceAll("[^\\d]", ""));
			} else {
				if (newValue.length() > 5) {
					cardMinMoreTF.setText(newValue.substring(0, 5));

				} else {
					if (newValue.isEmpty()) {
						System.out.println("no text!");
					} else {
						cardMin0RB.setSelected(false);
						cardMin1RB.setSelected(false);
					}
				}
				updateCardinalityOnNode();
			}
		});

		// Behavior MaxGroup

		maxGroup = new ToggleGroup();
		cardMax1RB.setToggleGroup(maxGroup);
		cardMaxAnyRB.setToggleGroup(maxGroup);

		cardMax1RB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				if (cardMaxMoreTF.getText() != "") {
					cardMaxMoreTF.clear();
				}
				updateCardinalityOnNode();
			}
		});
		cardMaxAnyRB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				if (cardMaxMoreTF.getText() != "") {
					cardMaxMoreTF.clear();
				}
				updateCardinalityOnNode();
			}
		});

		// only allow int values
		cardMaxMoreTF.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				cardMaxMoreTF.setText(newValue.replaceAll("[^\\d]", ""));
			} else {
				if (newValue.length() > 5) {
					cardMaxMoreTF.setText(newValue.substring(0, 5));

				} else {
					if (newValue.isEmpty()) {
						System.out.println("no text!");
					} else {
						cardMax1RB.setSelected(false);
						cardMaxAnyRB.setSelected(false);
					}
				}
				updateCardinalityOnNode();
			}
		});

		// Styling
		FontIcon icon = new FontIcon("fa-plus");
		icon.setIconColor(Paint.valueOf("white"));
		icon.setIconSize(20);
		addExistenceConstraintB.setGraphic(icon);
		addExistenceConstraintB.setGraphicTextGap(10);
		addExistenceConstraintB.getStyleClass().add("add-button-with-graphic");

		icon = new FontIcon("fa-minus");
		icon.setIconColor(Paint.valueOf("white"));
		icon.setIconSize(20);
		deleteExistenceConstraintB.setGraphic(icon);
		deleteExistenceConstraintB.setGraphicTextGap(10);
		deleteExistenceConstraintB.getStyleClass().add("delete-button-with-graphic");

		// Set EventListeners for Activity Changes
		activityNameTF.textProperty().addListener((obs, oldText, newText) -> {
			if (editedActivityNode != null) {
				editedActivityNode.getActivityElement().setIdentifier(newText);
				editedActivityNode.updateNode();
			}
		});

		initCB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				editedActivityNode.getExistenceConstraint().getInitConstraint().setActive(true);
			} else {
				editedActivityNode.getExistenceConstraint().getInitConstraint().setActive(false);
			}
			editedActivityNode.updateNode();
		});

		endCB.selectedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				editedActivityNode.getExistenceConstraint().getEndConstraint().setActive(true);
			} else {
				editedActivityNode.getExistenceConstraint().getEndConstraint().setActive(false);
			}
			editedActivityNode.updateNode();
		});

		addExistenceConstraintB.setOnAction((event) -> {
			addExistenceConstraint(editedActivityNode);
		});

		deleteExistenceConstraintB.setOnAction((event) -> {
			deleteExistenceConstraint(editedActivityNode);
		});

		cardinalityEditVBox.setVisible(false);
		cardinalityEditVBox.setManaged(false);
		structureEditVBox.setVisible(false);
		structureEditVBox.setManaged(false);

		cardMin0RB.setSelected(true);
		cardMaxAnyRB.setSelected(true);
		updateCardinalityOnNode();
		// initially start without showing Existence Constraints and disabling
		// the tab
		setDisable(true);

		// processTab.sendMouseReleaseEvent(processTab.getBackgroundPane());
	}

	/**
	 * saves the Cardinality based on the current Selection in the Existence
	 * Constraint of the ActivityElement calls update on ActivityNode to display
	 * changes
	 */
	private void updateCardinalityOnNode() {
		if (validateCardinalitySelection() && editedActivityNode != null) {
			if (editedActivityNode.getActivityElement().getExistenceConstraint() != null) {
				if (cardMin0RB.isSelected() && cardMaxAnyRB.isSelected()) {
					editedActivityNode.getActivityElement().getExistenceConstraint().setCard(null);
				} else {
					String min = "0";
					String max = "*";
					if (cardMin0RB.isSelected()) {
						min = "0";
					}
					if (cardMin1RB.isSelected()) {
						min = "1";
					}
					if (!cardMinMoreTF.getText().isEmpty()) {
						min = cardMinMoreTF.getText();
					}

					if (cardMax1RB.isSelected()) {
						max = "1";
					}
					if (!cardMaxMoreTF.getText().isEmpty()) {
						max = cardMaxMoreTF.getText();
					}
					if (cardMaxAnyRB.isSelected()) {
						max = "*";
					}
					editedActivityNode.getActivityElement().getExistenceConstraint().setCard(new Card(min, max));
				}
				editedActivityNode.updateNode();
			}
		}
	}

	/**
	 * Validates the input in UI for Cardinalities of Existence Constraints
	 * checks if min <= max, only allows numbers from 2 to 99999 shows error
	 * message if it is not valid
	 * 
	 * @return
	 */
	private boolean validateCardinalitySelection() {

		String minGTMax = "Invalid Cardinality!\nMinimum > Maximum";
		String oneIsInvalid = "Invalid Cardinality!\nFor \"1\" select button.";
		String zeroIsInvalid = "nvalid Cardinality!\nFor \"0\" select button.";
		boolean isValid = true;
		System.out.println("Validating Cardinality Selction....");

		if (cardMin0RB.isSelected() || cardMin1RB.isSelected()) { // Min 0 and
																	// Min 1 is
																	// always
																	// valid
			cardErrorLabel.setText("");
			isValid = true;
		} else {

			if (!cardMinMoreTF.getText().isEmpty()) { // More TextField has
														// valid Value
				if (cardMinMoreTF.getText().equals("0")) { // 0 and 1 are not
															// allowed in "more"
															// TextField
					cardErrorLabel.setText(zeroIsInvalid);
					isValid = false;
				} else {
					if (cardMinMoreTF.getText().equals("1")) {
						cardErrorLabel.setText(oneIsInvalid);
						isValid = false;
					} else {
						if (cardMax1RB.isSelected()) { // x..1 not valid
							cardErrorLabel.setText(minGTMax);
							isValid = false;
						}
						if (!cardMaxMoreTF.getText().isEmpty()) {
							if (Integer.parseInt(cardMinMoreTF.getText()) > Integer.parseInt(cardMaxMoreTF.getText())) {
								cardErrorLabel.setText(minGTMax);
								isValid = false;
							}
						}
					}
				}
			} 
		}
		if (!cardMaxMoreTF.getText().isEmpty()) {
			if (cardMaxMoreTF.getText().equals("0")) { // 0 and 1 are not
														// allowed in "more"
														// TextField
				cardErrorLabel.setText(zeroIsInvalid);
				isValid = false;
			} else {
				if (cardMaxMoreTF.getText().equals("1")) {
					cardErrorLabel.setText(oneIsInvalid);
					isValid = false;
				}
			}
			if (!cardMinMoreTF.getText().isEmpty()) {
				if (Integer.parseInt(cardMinMoreTF.getText()) > Integer.parseInt(cardMaxMoreTF.getText())) {
					cardErrorLabel.setText(minGTMax);
					isValid = false;
				}
			}
		}
		if (isValid) {
			cardErrorLabel.setText("");
			System.out.println("Selection is valid");
		} else {
			System.out.println("Selection is not valid: " + cardErrorLabel.getText());
		}
		return isValid;
	}

	public void setActivity(ActivityNode aNode) {
		editedActivityNode = aNode;
		updatePane();

	}

	private void updatePane() {
		this.activityNameTF.setText(editedActivityNode.getActivityElement().getIdentifier());
		updateExistenceConstraintPane();
	}

	private void updateExistenceConstraintPane() {
		boolean existenceConstraintIsSet = (editedActivityNode.getExistenceConstraint() != null);
		structureEditVBox.setVisible(existenceConstraintIsSet);
		structureEditVBox.setManaged(existenceConstraintIsSet);
		cardinalityEditVBox.setVisible(existenceConstraintIsSet);
		cardinalityEditVBox.setManaged(existenceConstraintIsSet);
		cardinalityLabel.setVisible(existenceConstraintIsSet);
		addDeleteExistenceConstraintBox.getChildren().clear();

		if (existenceConstraintIsSet) {
			addDeleteExistenceConstraintBox.getChildren().add(deleteExistenceConstraintB);
		} else {
			addDeleteExistenceConstraintBox.getChildren().add(addExistenceConstraintB);
		}

		if (existenceConstraintIsSet) {
			if(editedActivityNode.getExistenceConstraint().getInitConstraint() != null) {
				initCB.setSelected(editedActivityNode.getExistenceConstraint().getInitConstraint().isActive());
			}
			
			if(editedActivityNode.getExistenceConstraint().getEndConstraint() != null) {
				endCB.setSelected(editedActivityNode.getExistenceConstraint().getEndConstraint().isActive());
			}

			if (editedActivityNode.getExistenceConstraint().getCard() == null) {
				cardMaxAnyRB.setSelected(true);
				cardMin0RB.setSelected(true);
			}

			else {
				updateCardinalityPane();
			}
		}
	}

	/**
	 * updates Selection Elements on UI to represent current State of
	 * Cardinality Precondition:
	 * editedActivityNode.getExistenceConstraint().getCard() != null
	 */
	private void updateCardinalityPane() {
		String min = editedActivityNode.getExistenceConstraint().getCard().getMin();
		String max = editedActivityNode.getExistenceConstraint().getCard().getMax();
		if (min.equals("0")) {
			cardMin0RB.setSelected(true);
		} else {
			if (min.equals("1")) {
				cardMin1RB.setSelected(true);
			} else {
				cardMinMoreTF.setText(min);
			}

		}
		if (max.equals("1")) {
			cardMax1RB.setSelected(true);
		} else {
			if (max.equals("*")) {
				cardMaxAnyRB.setSelected(true);
			} else {
				cardMaxMoreTF.setText(max);
			}
		}
	}

	public TextArea getActivityNameTF() {
		return activityNameTF;
	}

	private void addExistenceConstraint(ActivityNode aNode) {
		processTab.getCurrentProcessElement().addExistenceConstraint(aNode.getActivityElement());
		updatePane();
	}

	private void deleteExistenceConstraint(ActivityNode aNode) {
		editedActivityNode.setExistenceConstraint(null);
		aNode.updateNode();
		updatePane();
	}
}

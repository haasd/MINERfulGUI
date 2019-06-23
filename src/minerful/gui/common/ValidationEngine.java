package minerful.gui.common;

import java.util.function.UnaryOperator;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.converter.IntegerStringConverter;
import minerful.concept.constraint.Constraint;

public class ValidationEngine {
	
	// only allow numeric input
	public static TextFormatter<Integer> getNumericFilter() {
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();

		    if (text.matches("([1-9][0-9]*)?")) {
		        return change;
		    }

		    return null;
		};
		return new TextFormatter<Integer>(new IntegerStringConverter(), 0, filter);
	}
	
	/**
	 * Set format for threshold-parameters
	 * 
	 * @return TableCell<Constraint, Double>
	 */
	public static TableCell<Constraint,Double> cellConstraintFormat() {
		return new TableCell<Constraint, Double>() {
			
			@Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                this.setAlignment(Pos.CENTER);
                
                if(empty) {
                    setText(null);
                }
                else {
                    if(item != null) {
                        setText(String.format("%.3f", item));
                    }
                }
            }
		};
	}
    

}

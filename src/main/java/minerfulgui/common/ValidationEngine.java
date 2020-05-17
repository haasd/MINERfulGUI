package minerfulgui.common;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import minerful.concept.constraint.Constraint;

public class ValidationEngine {
	
	// only allow numeric input
	public static TextFormatter<Integer> getNumericFilter() {
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();

		    if (text.matches("([0-9][0-9]*)?")) {
		        return change;
		    }

		    return null;
		};
		return new TextFormatter<Integer>(new IntegerStringConverter(), 0, filter);
	}
	
	// only allow numeric input
	public static TextFormatter<Double> getDoubleFilter(double init) {

		Pattern validDoubleText = Pattern.compile("(([0-1]{0,1})|(\\d+\\.\\d{0,3}))");
		
		return new TextFormatter<Double>(new DoubleStringConverter(), init, 
	            change -> {
	                String newText = change.getControlNewText() ;
	                if (validDoubleText.matcher(newText).matches()) {
	                    return change;
	                } else return null ;
	            });
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

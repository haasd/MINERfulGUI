package minerful.gui.common;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.converter.IntegerStringConverter;

public class ValidationEngine {
	
	/**
	 * 
	 */
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

}

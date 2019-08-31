package minerful.gui.service;

import java.util.Collection;

import javafx.scene.chart.XYChart;
import minerful.concept.constraint.Constraint;

public class DiscoverUtil {
	
	
	public static XYChart.Series<Double,Integer> countConstraintForThresholdValue(Collection<Constraint> constraints, String parameter) {

		XYChart.Series<Double,Integer> series = new XYChart.Series<>();
		
		for(int n=0; n <= 100; n+=5) {
			double i = n * 0.01;
			
			Integer numberOfConstraints = 0;
		
			for(Constraint constraint : constraints) {
				Double value = 0.0;
				switch(parameter) {
					case "support": 
						value = constraint.getSupport();
						break;
					case "confidence": 
						value = constraint.getConfidence();
						break;
					case "interest": 
						value = constraint.getInterestFactor();
						break;
				}
				
				if(value >= i) {
					numberOfConstraints++;
				}
				
			}
			series.getData().add(new XYChart.Data<Double,Integer>(i,numberOfConstraints));
		}
		
		return series;
	}

}

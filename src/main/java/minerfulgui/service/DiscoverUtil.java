package minerfulgui.service;

import java.util.Collection;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.StackPane;
import minerful.concept.constraint.Constraint;

public class DiscoverUtil {
	
	
	public static XYChart.Series<Double,Integer> countConstraintForThresholdValue(Collection<Constraint> constraints, String parameter) {

		XYChart.Series<Double,Integer> series = new XYChart.Series<>();
		
		// TODO ADD LOGIC 		
		
		for(int n=0; n <= 100; n+=1) {
			double i = n * 0.01;
			
			Integer numberOfConstraints = 0;
		
			for(Constraint constraint : constraints) {
				
				if(isAboveThreshold(parameter, constraint, i)) {
					numberOfConstraints++;
				}
			}
			
			series.getData().add(new XYChart.Data<Double,Integer>(i,numberOfConstraints));
		}
		
		return series;
	}
	
	public static XYChart.Series<Double,Integer> countConstraintForThresholdValueFixed(Collection<Constraint> constraints, String parameter, String fixedParameter, Double fixedThreshold) {

		XYChart.Series<Double,Integer> series = new XYChart.Series<>();
		
		// TODO ADD LOGIC 		
		
		for(int n=0; n <= 100; n+=1) {
			double i = n * 0.01;
			
			Integer numberOfConstraints = 0;
		
			for(Constraint constraint : constraints) {
				
				if(isAboveThreshold(fixedParameter, constraint, fixedThreshold)) {
					
					if(isAboveThreshold(parameter, constraint, i)) {
						numberOfConstraints++;
					}
				}
			}
			series.getData().add(new XYChart.Data<Double,Integer>(i,numberOfConstraints));
		}
		
		return series;
	}
	
	

	private static boolean isAboveThreshold(String parameter, Constraint constraint, double  fixedThreshold) {
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
		
		
		if(value < fixedThreshold) {
			return false;
		}
		
		return true;
	}
}

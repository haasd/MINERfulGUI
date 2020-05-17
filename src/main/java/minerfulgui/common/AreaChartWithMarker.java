package minerfulgui.common;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

public class AreaChartWithMarker<X, Y> extends AreaChart<X, Y> {
	
	 private ObservableList<Data<X, Y>> verticalMarkers;
	 private List<Line> lineList = new ArrayList<>();

	public AreaChartWithMarker(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
		super(xAxis, yAxis);
		
		verticalMarkers = FXCollections.observableArrayList(d -> new Observable[] {d.YValueProperty()});
        // listen to list changes and re-plot
		verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
	}
	
	public void addVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line );
        getPlotChildren().add(line);
        removeLines();
        verticalMarkers.add(marker);
    }
	
	private void removeLines() {
		for(Line line : lineList) {
			getPlotChildren().remove(line);
		}
		lineList.clear();
		verticalMarkers.clear();
	}
    
    /**
     * Overridden to layout the value markers.
     */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Data<X, Y> verticalMarker : verticalMarkers) {
            double lower = ((ValueAxis) getYAxis()).getLowerBound();
            Y lowerY = getYAxis().toRealValue(lower);
            double upper = ((ValueAxis) getYAxis()).getUpperBound();
            Y upperY = getYAxis().toRealValue(upper);
            Line line = (Line) verticalMarker.getNode();
            line.setStartY(getYAxis().getDisplayPosition(lowerY));
            line.setEndY(getYAxis().getDisplayPosition(upperY));
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()));
            line.setEndX(line.getStartX());
            line.setStroke(Paint.valueOf("#393939"));
            
            lineList.add(line);
        }
    }
}

package minerfulgui.service;

import minerfulgui.model.ProcessElement;

public abstract class LayoutAlgorithm {
	
	private double areaWidth;
	private double areaHeight;
	private ProcessElement processElement;
	private ProcessElementInterface processElementInterface;
	
	LayoutAlgorithm(double areaWidth, double areaHeight, ProcessElement processElement,
			ProcessElementInterface processElementInterface){
		this.areaWidth = areaWidth;
		this.areaHeight = areaHeight;
		this.processElement = processElement;
		this.processElementInterface = processElementInterface;
	}
	
	public abstract void optimizeLayout();

	public double getAreaWidth() {
		return areaWidth;
	}

	public void setAreaWidth(double areaWidth) {
		this.areaWidth = areaWidth;
	}

	public double getAreaHeight() {
		return areaHeight;
	}

	public void setAreaHeight(double areaHeight) {
		this.areaHeight = areaHeight;
	}

	public ProcessElement getProcessElement() {
		return processElement;
	}

	public void setProcessElement(ProcessElement processElement) {
		this.processElement = processElement;
	}

	public ProcessElementInterface getProcessElementInterface() {
		return processElementInterface;
	}

	public void setProcessElementInterface(ProcessElementInterface processElementInterface) {
		this.processElementInterface = processElementInterface;
	}
	
	

}

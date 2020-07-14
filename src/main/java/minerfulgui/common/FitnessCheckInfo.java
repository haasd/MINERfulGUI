package minerfulgui.common;

import java.util.ArrayList;
import java.util.List;

public class FitnessCheckInfo {
	
	private String template; 
	private String constraint;
	private String constraintSource;
	private String constraintTarget;
	private Double fitness;
	private Integer fullSatisfactions;
	private Integer vacuousSatisfactions;
	private Integer violations;
	private List<String> fullSatisfyingTraces = new ArrayList<>(); 
	private List<String> vacuousSatisfyingTraces = new ArrayList<>(); ; 
	private List<String> violatingSatisfyingTraces = new ArrayList<>(); ; 
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getConstraint() {
		return constraint;
	}
	
	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
	
	public Double getFitness() {
		return fitness;
	}
	
	public void setFitness(Double fitness) {
		this.fitness = fitness;
	}
	
	public Integer getFullSatisfactions() {
		return fullSatisfactions;
	}
	
	public void setFullSatisfactions(Integer fullSatisfactions) {
		this.fullSatisfactions = fullSatisfactions;
	}
	
	public Integer getVacuousSatisfactions() {
		return vacuousSatisfactions;
	}
	
	public void setVacuousSatisfactions(Integer vacuousSatisfactions) {
		this.vacuousSatisfactions = vacuousSatisfactions;
	}
	
	public Integer getViolations() {
		return violations;
	}
	
	public void setViolations(Integer violations) {
		this.violations = violations;
	}

	public String getConstraintSource() {
		return constraintSource;
	}

	public void setConstraintSource(String constraintSource) {
		this.constraintSource = constraintSource;
	}

	public String getConstraintTarget() {
		return constraintTarget;
	}

	public void setConstraintTarget(String constraintTarget) {
		this.constraintTarget = constraintTarget;
	}

	public List<String> getFullSatisfyingTraces() {
		return fullSatisfyingTraces;
	}

	public void setFullSatisfyingTraces(List<String> fullSatisfyingTraces) {
		this.fullSatisfyingTraces = fullSatisfyingTraces;
	}

	public List<String> getVacuousSatisfyingTraces() {
		return vacuousSatisfyingTraces;
	}

	public void setVacuousSatisfyingTraces(List<String> vacuousSatisfyingTraces) {
		this.vacuousSatisfyingTraces = vacuousSatisfyingTraces;
	}

	public List<String> getViolatingSatisfyingTraces() {
		return violatingSatisfyingTraces;
	}

	public void setViolatingSatisfyingTraces(List<String> violatingSatisfyingTraces) {
		this.violatingSatisfyingTraces = violatingSatisfyingTraces;
	}

}

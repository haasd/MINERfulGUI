package minerful.gui.model;

public enum RelationConstraintEnum {
	RESPONDED_EXISTENCE("RespondedExistence",true, new Template("RespondedExistence", true, true, false, false, false, false, false, false) ),
	RESPONSE("Response", true, new Template("Response", false, true, false, false, false, false, false, false)),
	ALTERNATE_RESPONSE("AlternateResponse", true, new Template("AlternateResponse", false, true, false, false, true, false, false, false)),
	CHAIN_RESPONSE("ChainResponse", true, new Template("ChainResponse", false, true, false, false, false, false, true, false)),
	PRECEDENCE("Precedence", true, new Template("Precedence", false, false, true, false, false, false, false, false)),
	ALTERNATE_PRECEDENCE("AlternatePrecedence", true, new Template("AlternatePrecedence", false, false, true, false, false, true, false, false)),
	CHAIN_PRECEDENCE("ChainPrecedence", true, new Template("ChainPrecedence", false, false, true, false, false, false, true, false)),
	CO_EXISTENCE("CoExistence", true, new Template("CoExistence", true, true, true, true, false, false, false, false)),
	SUCCESSION("Succession", true, new Template("Succession", false, true, true, false, false, false, false, false)),
	ALTERNATE_SUCCESSION("AlternateSuccession", true, new Template("AlternateSuccession", false, true, true, false, true, true, false, false)),
	CHAIN_SUCCESSION("ChainSuccession", true, new Template("ChainSuccession", false, true, true, false, false, false, true, false)),
	NOT_CHAIN_SUCCESSION("NotChainSuccession", false, new Template("NotChainSuccession", false, true, true, false, false, false, true, true)),
	NOT_SUCCESSION("NotSuccession", false, new Template("NotSuccession", false, true, true, false, false, false, false, true)),
	NOT_CO_EXISTENCE("NotCoExistence", false, new Template("NotCoExistence", true, true, true, true, false, false, false, true));
	
	private String templateLabel;
	private Template template;
	private Boolean positiveConstraint;
	
	RelationConstraintEnum(String templateLabel,Boolean positiveConstraint, Template template){
		this.templateLabel = templateLabel;
		this.template = template;
		this.positiveConstraint = positiveConstraint;
	}
	
	public String getTemplateLabel() {
		return templateLabel;
	}
	
	public Template getTemplate() {
		return template;
	}
	
	public static Boolean isPositiveConstraint(String templateLabel) {
		for(RelationConstraintEnum rce : RelationConstraintEnum.values()) {
			if(rce.getTemplateLabel().toLowerCase().equals(templateLabel.toLowerCase())) {
				return rce.positiveConstraint;
			}
		}
		
		return false;
	}
	
	public static Template findTemplateByTemplateLabel(String templateLabel) {
		for(RelationConstraintEnum rce : RelationConstraintEnum.values()) {
			if(rce.getTemplateLabel().toLowerCase().equals(templateLabel.toLowerCase())) {
				return rce.getTemplate();
			}
		}
		return null;
	}
}
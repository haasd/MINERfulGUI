package minerful.gui.model;

public enum RelationConstraintEnum {
	RESPONDED_EXISTENCE("RespondedExistence", new Template("respondedExistence", true, true, false, false, false, false, false, false) ),
	RESPONSE("Response", new Template("response", false, true, false, false, false, false, false, false)),
	ALTERNATE_RESPONSE("AlternateResponse", new Template("alternateResponse", false, true, false, false, true, false, false, false)),
	CHAIN_RESPONSE("ChainResponse", new Template("chainResponse", false, true, false, false, false, false, true, false)),
	PRECEDENCE("Precedence", new Template("precedence", false, false, true, false, false, false, false, false)),
	ALTERNATE_PRECEDENCE("AlternatePrecedence", new Template("alternatePrecedence", false, false, true, false, false, true, false, false)),
	CHAIN_PRECEDENCE("ChainPrecedence", new Template("chainPrecedence", false, false, true, false, false, false, true, false)),
	CO_EXISTENCE("CoExistence", new Template("coExistence", true, true, true, true, false, false, false, false)),
	SUCCESSION("Succession", new Template("succession", false, true, true, false, false, false, false, false)),
	ALTERNATE_SUCCESSION("AlternateSuccession", new Template("alternateSuccession", false, true, true, false, true, true, false, false)),
	CHAIN_SUCCESSION("ChainSuccession", new Template("chainSuccession", false, true, true, false, false, false, true, false)),
	NOT_CHAIN_SUCCESSION("NotChainSuccession", new Template("notChainSuccession", false, true, true, false, false, false, true, true)),
	NOT_SUCCESSION("NotSuccession", new Template("notSuccession", false, true, true, false, false, false, false, true)),
	NOT_CO_EXISTENCE("NotCoExistence", new Template("notCoExistence", true, true, true, true, false, false, false, true));
	
	private String templateLabel;
	private Template template;
	
	RelationConstraintEnum(String templateLabel, Template template){
		this.templateLabel = templateLabel;
		this.template = template;
	}
	
	public String getTemplateLabel() {
		return templateLabel;
	}
	
	public Template getTemplate() {
		return template;
	}
	
	public static Template findTemplateByTemplateLabel(String templateLabel) {
		for(RelationConstraintEnum rce : RelationConstraintEnum.values()) {
			if(rce.getTemplateLabel().equals(templateLabel)) {
				return rce.getTemplate();
			}
		}
		return null;
	}
}
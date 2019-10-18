package minerful.gui.model.io;


import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import minerful.gui.model.ActivityElement;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;

public class XmlModelWriter {
	
	Logger logger = Logger.getLogger(XmlModelWriter.class);
	
	private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    Document positionDoc;
    Document activityDoc;
    Document constraintDoc;
    
    ProcessElement processElement;
	
	public XmlModelWriter(ProcessElement processElement) {
		this.processElement = processElement;
	}	
	
	public void writeXmlsFromProcessModel() {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			positionDoc = docBuilder.newDocument();
			activityDoc = docBuilder.newDocument();
			constraintDoc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error during creation of DocumentBuilder instance");
		}

		createPositionDoc();
		createActivityDoc();
		createConstraintDoc();
	}
	
	private void createPositionDoc() {
		
		logger.info("Start Creation of Position-Document!");
		
		Element rootElement = positionDoc.createElement("Positions");
		positionDoc.appendChild(rootElement);
		
		Element activityRootElement = positionDoc.createElement("ActivityPositions");
		rootElement.appendChild(activityRootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			activityRootElement.appendChild(createActivityPositionElement(aElement));
		}
		
		Element relationRootElement = positionDoc.createElement("RelationPositions");
		rootElement.appendChild(relationRootElement);
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			relationRootElement.appendChild(createRelationPositionElement(rcElement));
		}
		
		createXMLFile(positionDoc, "Positions");
		logger.info("Finished Creation of Position-Document!");
	}
	
	private Element createActivityPositionElement(ActivityElement aElement) {
		Element activityPosition = positionDoc.createElement("ActivityPosition");
		activityPosition.setAttribute("identifier", aElement.getId().toString());
		activityPosition.setAttribute("positionX", aElement.getPosX().toString());
		activityPosition.setAttribute("positionY", aElement.getPosY().toString());
		
		return activityPosition;
	}
	
	private Element createRelationPositionElement(RelationConstraintElement rcElement) {
		Element relationPosition = positionDoc.createElement("RelationPosition");
		relationPosition.setAttribute("identifier", rcElement.getId().toString());
		relationPosition.setAttribute("positionX", rcElement.getPosX().toString());
		relationPosition.setAttribute("positionY", rcElement.getPosY().toString());
		
		return relationPosition;
	}
	
	private void createXMLFile(Document document, String fileName) {
		// Write the content into XML file
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(String.format("%s.xml", fileName)));
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        
		try {
			transformer = transformerFactory.newTransformer();
			// Beautify the format of the resulted XML
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void createActivityDoc() {
		logger.info("Start Creation of Activity-Document!");
		
		Element rootElement = activityDoc.createElement("Activities");
		activityDoc.appendChild(rootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			rootElement.appendChild(createActivityElement(aElement));
		}
		
		createXMLFile(activityDoc, "Activities");
		
		logger.info("Finished Creation of Activity-Document!");
	}
	
	private Element createActivityElement(ActivityElement aElement) {
		Element activity = activityDoc.createElement("Activity");
		activity.setAttribute("identifier", aElement.getId().toString());
		activity.setAttribute("name", aElement.getIdentifier());
		
		return activity;
	}
	
	private void createConstraintDoc() {
		logger.info("Start Creation of Constraints-Document!");
		
		Element rootElement = constraintDoc.createElement("Constraints");
		constraintDoc.appendChild(rootElement);
		
		Element existenceConstraintRootElement = constraintDoc.createElement("ExistenceConstraints");
		rootElement.appendChild(existenceConstraintRootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			existenceConstraintRootElement.appendChild(createExistenceConstraintElement(aElement));
		}
		
		Element relationConstraintRootElement = constraintDoc.createElement("RelationConstraints");
		rootElement.appendChild(relationConstraintRootElement);
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			relationConstraintRootElement.appendChild(createRelationConstraintElement(rcElement));
		}
		
		createXMLFile(constraintDoc, "Constraints");
		
		logger.info("Finished Creation of Constraints-Document!");
	}
	
	private Element createExistenceConstraintElement(ActivityElement aElement) {
		Element existenceConstraint = constraintDoc.createElement("ExistenceConstraint");
		existenceConstraint.setAttribute("identifier", aElement.getId().toString());
		if(aElement.getExistenceConstraint() != null && aElement.getExistenceConstraint().getStruct() != null) {
			Element positionConstraint = constraintDoc.createElement("Position");
			positionConstraint.setTextContent(aElement.getExistenceConstraint().getStruct().name());
			existenceConstraint.appendChild(positionConstraint);
		}
		
		if(aElement.getExistenceConstraint() != null && aElement.getExistenceConstraint().getCard() != null) {
			Element cardinalityConstraint = constraintDoc.createElement("Cardinality");
			cardinalityConstraint.setAttribute("min", (aElement.getExistenceConstraint().getCard().getMin()));
			cardinalityConstraint.setAttribute("max", (aElement.getExistenceConstraint().getCard().getMax()));
			existenceConstraint.appendChild(cardinalityConstraint);
		}
		
		existenceConstraint.setAttribute("positionY", aElement.getPosY().toString());
		
		return existenceConstraint;
	}
	
	private Element createRelationConstraintElement(RelationConstraintElement rcElement) {
		Element relationConstraint = constraintDoc.createElement("RelationPosition");
		relationConstraint.setAttribute("identifier", rcElement.getId().toString());
		relationConstraint.setAttribute("template", rcElement.getTemplate().getName());
		
		return relationConstraint;
	}
	
	private void exportXmls() {
		
	}

}

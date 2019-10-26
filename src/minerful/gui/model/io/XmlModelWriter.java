package minerful.gui.model.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import minerful.gui.model.ActivityElement;
import minerful.gui.model.ConstraintElement;
import minerful.gui.model.ExistenceConstraintEnum;
import minerful.gui.model.LineElement;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;

public class XmlModelWriter {
	
	Logger logger = Logger.getLogger(XmlModelWriter.class);
	
	private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    Document positionDoc;
    Document activityDoc;
    Document constraintDoc;
    List<File> tmpFiles;
    
    ProcessElement processElement;
	
	public XmlModelWriter(ProcessElement processElement) {
		this.processElement = processElement;
	}	
	
	public void writeXmlsFromProcessModel(String path) {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			positionDoc = docBuilder.newDocument();
			activityDoc = docBuilder.newDocument();
			constraintDoc = docBuilder.newDocument();
			tmpFiles = new ArrayList<>();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error during creation of DocumentBuilder instance");
		}
		
		createPositionDoc();
		createActivityDoc();
		createConstraintDoc();

		try {
			zipDocuments(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error during zip of documents!");
		}
	}
	
	private void createPositionDoc() {
		
		logger.info("Start Creation of Position-Document!");
		
		Element rootElement = positionDoc.createElement("positions");
		positionDoc.appendChild(rootElement);
		
		Element activityRootElement = positionDoc.createElement("activityPositions");
		rootElement.appendChild(activityRootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			activityRootElement.appendChild(createActivityPositionElement(aElement));
		}
		
		Element relationRootElement = positionDoc.createElement("relationPositions");
		rootElement.appendChild(relationRootElement);
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			relationRootElement.appendChild(createRelationPositionElement(rcElement));
		}
		
		createXMLFile(positionDoc, "Positions");
		
		logger.info("Finished Creation of Position-Document!");
	}
	
	private Element createActivityPositionElement(ActivityElement aElement) {
		Element activityPosition = positionDoc.createElement("activityPosition");
		activityPosition.setAttribute("identifier", aElement.getTaskCharIdentifier());
		
		Element positionX = positionDoc.createElement("positionX");
		positionX.setTextContent(aElement.getPosX().toString());
		activityPosition.appendChild(positionX);
		
		Element positionY = positionDoc.createElement("positionY");
		positionY.setTextContent(aElement.getPosY().toString());
		activityPosition.appendChild(positionY);
		
		return activityPosition;
	}
	
	private Element createRelationPositionElement(RelationConstraintElement rcElement) {
		Element relationPosition = positionDoc.createElement("relationPosition");
		relationPosition.setAttribute("identifier", "C" + rcElement.getId().toString());
		
		Element positionX = positionDoc.createElement("positionX");
		positionX.setTextContent(rcElement.getPosX().toString());
		relationPosition.appendChild(positionX);
		
		Element positionY = positionDoc.createElement("positionY");
		positionY.setTextContent(rcElement.getPosY().toString());
		relationPosition.appendChild(positionY);
		
		return relationPosition;
	}
	
	private void createXMLFile(Document document, String fileName) {
		// Write the content into XML file
        DOMSource source = new DOMSource(document);
        try {
			File tmpFile = new File(String.format("%s.xml", fileName));
			tmpFiles.add(tmpFile);
			StreamResult result = new StreamResult(tmpFile);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer;
	        
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
		
		Element rootElement = activityDoc.createElement("activities");
		activityDoc.appendChild(rootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			rootElement.appendChild(createActivityElement(aElement));
		}
		
		createXMLFile(activityDoc, "Activities");
		
		logger.info("Finished Creation of Activity-Document!");
	}
	
	private Element createActivityElement(ActivityElement aElement) {
		Element activity = activityDoc.createElement("activity");
		activity.setAttribute("identifier", aElement.getTaskCharIdentifier());
		
		Element label = activityDoc.createElement("label");
		label.setTextContent(aElement.getIdentifier());
		activity.appendChild(label);
		
		return activity;
	}
	
	private void createConstraintDoc() {
		logger.info("Start Creation of Constraints-Document!");
		
		Element rootElement = constraintDoc.createElement("constraints");
		constraintDoc.appendChild(rootElement);
		
		Element existenceConstraintRootElement = constraintDoc.createElement("existenceConstraints");
		rootElement.appendChild(existenceConstraintRootElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			createExistenceConstraintElement(existenceConstraintRootElement,aElement);
		}
		
		Element relationConstraintRootElement = constraintDoc.createElement("relationConstraints");
		rootElement.appendChild(relationConstraintRootElement);
		
		List<LineElement> alreadyCreatedLineElements = new ArrayList<>();
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			createRelationConstraintElement(relationConstraintRootElement, rcElement , alreadyCreatedLineElements);
		}
		
		createXMLFile(constraintDoc, "Constraints");
		
		logger.info("Finished Creation of Constraints-Document!");
	}
	
	private void createExistenceConstraintElement(Element rootElement, ActivityElement aElement) {
		
		// handle position constraints
		if(aElement.getExistenceConstraint() != null) {
			
			if(aElement.getExistenceConstraint().getInitConstraint() != null && aElement.getExistenceConstraint().getInitConstraint().isActive()) {
				Element existenceConstraint = constraintDoc.createElement("existenceConstraint");
				Element activity = constraintDoc.createElement("activity");
				activity.setTextContent(aElement.getTaskCharIdentifier());
				existenceConstraint.appendChild(activity);
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.INIT.getTemplateLabel());
				addParameterElements(existenceConstraint,aElement.getExistenceConstraint().getInitConstraint());
				rootElement.appendChild(existenceConstraint);
			}
			
			if(aElement.getExistenceConstraint().getEndConstraint() != null && aElement.getExistenceConstraint().getEndConstraint().isActive()) {
				Element existenceConstraint = constraintDoc.createElement("existenceConstraint");
				Element activity = constraintDoc.createElement("activity");
				activity.setTextContent(aElement.getTaskCharIdentifier());
				existenceConstraint.appendChild(activity);
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.END.getTemplateLabel());
				addParameterElements(existenceConstraint,aElement.getExistenceConstraint().getEndConstraint());
				rootElement.appendChild(existenceConstraint);
			}
			
		}
		
		// handle cardinality constraints
		if(aElement.getExistenceConstraint() != null && aElement.getExistenceConstraint().getCard() != null) {
			Element existenceConstraint = constraintDoc.createElement("existenceConstraint");
			Element activity = constraintDoc.createElement("activity");
			activity.setTextContent(aElement.getTaskCharIdentifier());
			existenceConstraint.appendChild(activity);
			
			if( "0".equals(aElement.getExistenceConstraint().getCard().getMin()) && "1".equals(aElement.getExistenceConstraint().getCard().getMax())) {
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel());
			} else {
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel());
			}
			
			Element minValue = constraintDoc.createElement("min");
			minValue.setTextContent(aElement.getExistenceConstraint().getCard().getMin());
			existenceConstraint.appendChild(minValue);
			
			Element maxValue = constraintDoc.createElement("max");
			maxValue.setTextContent(aElement.getExistenceConstraint().getCard().getMax());
			existenceConstraint.appendChild(maxValue);
			
			addParameterElements(existenceConstraint,aElement.getExistenceConstraint().getCard());
			rootElement.appendChild(existenceConstraint);
		}

	}
	
	private void createRelationConstraintElement(Element rootElement, RelationConstraintElement rcElement , List<LineElement> alreadyCreatedLineElements) {
		
		for(LineElement lineElement: rcElement.getLineElements()) {
			
			Element relationConstraint = constraintDoc.createElement("relationConstraint");
			relationConstraint.setAttribute("template", rcElement.getTemplate().getName());
			relationConstraint.setAttribute("identifier", "C" + rcElement.getId().toString());
			
			// add activities and parameters
			Element source = constraintDoc.createElement("sourceActivity");
			source.setTextContent(lineElement.getSourceElement().getTaskCharIdentifier());
			relationConstraint.appendChild(source);
			Element target = constraintDoc.createElement("targetActivity");
			target.setTextContent(lineElement.getTargetElement().getTaskCharIdentifier());
			relationConstraint.appendChild(target);
			addParameterElements(relationConstraint, lineElement);
			
			rootElement.appendChild(relationConstraint);
		}

	}
	
	private void zipDocuments(String path) throws IOException {
		
        FileOutputStream fos = new FileOutputStream(path);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File fileToZip : tmpFiles) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
 
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            
            fileToZip.delete();
        }
        zipOut.close();
        fos.close();
        
        FileUtils.cleanDirectory(new File("/tmp"));
	}

	private void addParameterElements(Element element, ConstraintElement constraintElement) {
		Element support = constraintDoc.createElement("support");
		support.setTextContent(String.valueOf(constraintElement.getSupport()));
		element.appendChild(support);
		
		Element confidence = constraintDoc.createElement("confidence");
		confidence.setTextContent(String.valueOf(constraintElement.getConfidence()));
		element.appendChild(confidence);
		
		Element interest = constraintDoc.createElement("interest");
		interest.setTextContent(String.valueOf(constraintElement.getInterest()));
		element.appendChild(interest);
	}
	
}
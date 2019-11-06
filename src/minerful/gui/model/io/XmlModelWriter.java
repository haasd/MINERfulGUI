package minerful.gui.model.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
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

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.ExistenceConstraint;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ConstraintElement;
import minerful.gui.model.ExistenceConstraintEnum;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;

public class XmlModelWriter {
	
	Logger logger = Logger.getLogger(XmlModelWriter.class);
	
	private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    private Document positionDoc;
    private Document processSpecificationDoc;
    private Document templateDoc;
    private List<File> tmpFiles;
    
    private ProcessElement processElement;
    
    private Integer constraintsCounter = 0;
	
	public XmlModelWriter(ProcessElement processElement) {
		this.processElement = processElement;
	}	
	
	public void writeXmlsFromProcessModel(String path) {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			positionDoc = docBuilder.newDocument();
			processSpecificationDoc = docBuilder.newDocument();
			templateDoc = docBuilder.newDocument();
			tmpFiles = new ArrayList<>();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error during creation of DocumentBuilder instance");
		}
		
		createPositionDoc();
		createprocessSpecificationDoc();
		createTemplateDoc();

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
		relationPosition.setAttribute("identifier", createConstraintIdentifier(rcElement.getId()));
		
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
	
	private void createprocessSpecificationDoc() {
		logger.info("Start Creation of ProcessSpecification-Document!");
		
		Element rootElement = processSpecificationDoc.createElement("processSpecification");
		processSpecificationDoc.appendChild(rootElement);
		
		
		// create activities element
		Element activitiesElement = processSpecificationDoc.createElement("activities");
		rootElement.appendChild(activitiesElement);
		for(ActivityElement aElement : processElement.getActivityEList()) {
			activitiesElement.appendChild(createActivityElement(aElement));
		}
		
		// create constraints element
		Element constraintsElement = processSpecificationDoc.createElement("constraints");
		rootElement.appendChild(constraintsElement);
		
		for(ActivityElement aElement : processElement.getActivityEList()) {
			createExistenceConstraintElement(constraintsElement,aElement);
		}
		
		for(RelationConstraintElement rcElement : processElement.getConstraintEList()) {
			createRelationConstraintElement(constraintsElement, rcElement);
		}
		
		createXMLFile(processSpecificationDoc, "ProcessSpecification");
		
		logger.info("Finished Creation of ProcessSpecification-Document!");
	}
	
	private Element createActivityElement(ActivityElement aElement) {
		Element activity = processSpecificationDoc.createElement("activity");
		activity.setAttribute("identifier", aElement.getTaskCharIdentifier());
		
		Element label = processSpecificationDoc.createElement("label");
		label.setTextContent(aElement.getIdentifier());
		activity.appendChild(label);
		
		return activity;
	}
	
	private void createTemplateParameters(Element rootElement) {
		
	}
	
	private void createTemplateDoc() {
		logger.info("Start Creation of Template-Document!");
		Collection<Class<? extends Constraint>> templates = MetaConstraintUtils.getAllConstraintTemplates();
		
		// create activities element
		Element rootElement = processSpecificationDoc.createElement("templates");

		for(Class<? extends Constraint> template : templates) {
			Element templateElement = processSpecificationDoc.createElement("template");
			
//			try {
//
//				Constraint constraint = (Constraint) template.getConstructors()[0].newInstance();
//				templateElement.setAttribute("name", constraint.getTemplateName());
//				Element regExpElement = processSpecificationDoc.createElement("regexp");
//				regExpElement.setTextContent(constraint.getRegularExpression());
//				templateElement.appendChild(regExpElement);
//				
//				Element description = processSpecificationDoc.createElement("description");
//				description.setTextContent(constraint.getDescription());
//				templateElement.appendChild(description);
//				
//				Element parameters = processSpecificationDoc.createElement("parameters");
//				templateElement.appendChild(parameters);
//				createTemplateParameters(parameters);
//				
//			} catch (InstantiationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			

			rootElement.appendChild(templateElement);
		}

		createXMLFile(templateDoc, "Templates");
		
		logger.info("Finished Creation of Template-Document!");
	}
	
	private void addParameterElements(Element parameters, List<String> activities) {
		Element parameter = processSpecificationDoc.createElement("parameter");
		parameters.appendChild(parameter);
		
		// add all activities to the parameter element
		for(String activityId : activities) {
			Element activity = processSpecificationDoc.createElement("activity");
			activity.setTextContent(activityId);
			parameter.appendChild(activity);
		}
	}
	
	private String createConstraintIdentifier(Integer id) {
		constraintsCounter++;
		
		if(id != null) {
			return "C"+id;
		}
		
		return "C" + constraintsCounter;
	}
	
	private void createExistenceConstraintElement(Element rootElement, ActivityElement aElement) {
		
		// handle position constraints
		if(aElement.getExistenceConstraint() != null) {
			
			if(aElement.getExistenceConstraint().getInitConstraint() != null && aElement.getExistenceConstraint().getInitConstraint().isActive()) {
				Element existenceConstraint = processSpecificationDoc.createElement("constraint");
				
				existenceConstraint.setAttribute("identifier", createConstraintIdentifier(null));
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.INIT.getTemplateLabel());
				
				Element parameters = processSpecificationDoc.createElement("parameters");
				existenceConstraint.appendChild(parameters);
				addParameterElements(parameters, new ArrayList<>(List.of(aElement.getTaskCharIdentifier())));
				addMeasuresElement(existenceConstraint,aElement.getExistenceConstraint().getInitConstraint());
				rootElement.appendChild(existenceConstraint);
			}
			
			if(aElement.getExistenceConstraint().getEndConstraint() != null && aElement.getExistenceConstraint().getEndConstraint().isActive()) {
				Element existenceConstraint = processSpecificationDoc.createElement("constraint");
				
				existenceConstraint.setAttribute("identifier", createConstraintIdentifier(null));
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.END.getTemplateLabel());
				
				Element parameters = processSpecificationDoc.createElement("parameters");
				existenceConstraint.appendChild(parameters);
				addParameterElements(parameters, new ArrayList<>(List.of(aElement.getTaskCharIdentifier())));
				addMeasuresElement(existenceConstraint,aElement.getExistenceConstraint().getEndConstraint());
				rootElement.appendChild(existenceConstraint);
			}
			
		}
		
		// handle cardinality constraints
		if(aElement.getExistenceConstraint() != null && aElement.getExistenceConstraint().getCard() != null) {
			
			if( "1".equals(aElement.getExistenceConstraint().getCard().getMin().getBorder())) {
				Element existenceConstraint = processSpecificationDoc.createElement("constraint");
				existenceConstraint.setAttribute("identifier", createConstraintIdentifier(null));
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel());
				Element parameters = processSpecificationDoc.createElement("parameters");
				existenceConstraint.appendChild(parameters);
				addParameterElements(parameters, new ArrayList<>(List.of(aElement.getTaskCharIdentifier())));
				addMeasuresElement(existenceConstraint,aElement.getExistenceConstraint().getCard().getMin());
				rootElement.appendChild(existenceConstraint);
			}
			
			if( "1".equals(aElement.getExistenceConstraint().getCard().getMax().getBorder())) {
				Element existenceConstraint = processSpecificationDoc.createElement("constraint");
				existenceConstraint.setAttribute("identifier", createConstraintIdentifier(null));
				existenceConstraint.setAttribute("template", ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel());
				Element parameters = processSpecificationDoc.createElement("parameters");
				existenceConstraint.appendChild(parameters);
				addParameterElements(parameters, new ArrayList<>(List.of(aElement.getTaskCharIdentifier())));
				addMeasuresElement(existenceConstraint,aElement.getExistenceConstraint().getCard().getMax());
				rootElement.appendChild(existenceConstraint);
			}
		}
	}
	
	private void createRelationConstraintElement(Element rootElement, RelationConstraintElement rcElement) {
			
			Element relationConstraint = processSpecificationDoc.createElement("constraint");
			relationConstraint.setAttribute("template", rcElement.getTemplate().getName());
			relationConstraint.setAttribute("identifier", createConstraintIdentifier(rcElement.getId()));
			
			// add activities and parameters
			Element parameters = processSpecificationDoc.createElement("parameters");
			relationConstraint.appendChild(parameters);
			addParameterElements(parameters, rcElement.getParameter1ElementsIds());
			addParameterElements(parameters, rcElement.getParameter2ElementsIds());
			addMeasuresElement(relationConstraint, rcElement);
			
			rootElement.appendChild(relationConstraint);

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

	private void addMeasuresElement(Element element, ConstraintElement constraintElement) {
		Element measures = processSpecificationDoc.createElement("measures");
		element.appendChild(measures);
		
		Element support = processSpecificationDoc.createElement("support");
		support.setTextContent(String.valueOf(constraintElement.getSupport()));
		measures.appendChild(support);
		
		Element confidence = processSpecificationDoc.createElement("confidence");
		confidence.setTextContent(String.valueOf(constraintElement.getConfidence()));
		measures.appendChild(confidence);
		
		Element interest = processSpecificationDoc.createElement("interest");
		interest.setTextContent(String.valueOf(constraintElement.getInterest()));
		measures.appendChild(interest);
	}
	
}

package minerful.gui.model.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.geometry.Point2D;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.Card;
import minerful.gui.model.ExistenceConstraintEnum;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.RelationConstraintEnum;
import minerful.gui.model.StructureElement;
import minerful.gui.model.Template;
import minerful.gui.model.xml.XMLExistenceConstraint;

public class XmlModelReader {
	
	Logger logger = Logger.getLogger(XmlModelReader.class);
	String path;
	Map<String, Point2D> positions = new HashMap<>();
	
	public XmlModelReader(String path) {
		this.path = path;
	}
	
	public ProcessElement importXmlsAsProcessModel() {
		
		ProcessElement processElement = new ProcessElement();
		List<File> extractedFiles = unzipFolder();
		Map<String,ActivityElement> activities = new HashMap<>();
		List<ConstraintElement> constraintList = new ArrayList<>();
		
		//process unzipped files
		for(File file: extractedFiles) {
			Element rootElement = getRootElement(file);
			
			if("Activities.xml".equals(file.getName())) {
				activities = extractActivitiesFromFile(rootElement);
			} else if ("Positions.xml".equals(file.getName())) {
				positions = extractPositionsFromFile(rootElement);
			} else if ("Constraints.xml".equals(file.getName())) {
				constraintList = extractConstraintsFromFile(rootElement);
			} else {
				logger.error("Folder content doesn't fit requirements");
			}
			
			if(!file.delete()) {
				logger.error("Error during deletion of temp File: " + file.getName());
			}
		}
		
		Map<String, RelationConstraintElement> constraints = processConstraintList(constraintList, activities);
		
		setPositionsOfElements(activities, constraints);
		
		// add extracted Activities to Model
		for(ActivityElement aElement : activities.values()) {
			processElement.addActivity(aElement);
		}
		
		// add extracted RelationConstraints to Model
		for(RelationConstraintElement rcElement : constraints.values()) {
			processElement.addRelationConstraint(rcElement);
		}
		
		return processElement;
	}

	private void setPositionsOfElements(Map<String, ActivityElement> activities,
			Map<String, RelationConstraintElement> constraints) {
		
		// Map position to activities and constraints
		for(Map.Entry<String, Point2D> entry : positions.entrySet()) {
			if(activities.get(entry.getKey()) != null) {
				ActivityElement aElement = activities.get(entry.getKey());
				aElement.setPosition(entry.getValue().getX(), entry.getValue().getY());
			} else if(constraints.get(entry.getKey()) != null) {
				RelationConstraintElement rcElement = constraints.get(entry.getKey());
				rcElement.setPosition(entry.getValue().getX(), entry.getValue().getY());
			} else {
				logger.error("Could not map position to activity or constraint!");
			}
		}
		
	}

	private Map<String, RelationConstraintElement> processConstraintList(List<ConstraintElement> constraintList,
			Map<String, ActivityElement> activities) {
		
		Map<String, RelationConstraintElement> rcElements = new HashMap<>();
		
		int i = 0;
		for(ConstraintElement cElement : constraintList) {
			
			if(cElement.getTargetIdentifier() == null) {
				
				// determine corresponding ActivityElement
				ActivityElement aElement = activities.get(cElement.getSourceIdentifier());
				
				if(aElement.getExistenceConstraint() == null) {
					aElement.setExistenceConstraint(new XMLExistenceConstraint());
				}
				
				// set Existence Constraints
				if(ExistenceConstraintEnum.INIT.getTemplateLabel().equals(cElement.getTemplate())) {
					aElement.getExistenceConstraint().setInitConstraint(new StructureElement(true,cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
				} else if(ExistenceConstraintEnum.END.getTemplateLabel().equals(cElement.getTemplate())) {
					aElement.getExistenceConstraint().setEndConstraint(new StructureElement(true,cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
				} else if(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel().equals(cElement.getTemplate())) {
					if(aElement.getExistenceConstraint().getCard() == null) {
						aElement.getExistenceConstraint().setCard(new Card(cElement.getMin(),cElement.getMax(),cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
					}
				} else if(ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel().equals(cElement.getTemplate())) {
					if(aElement.getExistenceConstraint().getCard() == null) {
						aElement.getExistenceConstraint().setCard(new Card(cElement.getMin(),cElement.getMax(), cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
					}
				} 
			} else {
				i++;
				Template template = RelationConstraintEnum.findTemplateByTemplateLabel(cElement.getTemplate());
				RelationConstraintElement rcElement = new RelationConstraintElement(i, template);
				rcElement.addActivityElement(activities.get(cElement.getSourceIdentifier()), 1, cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());
				rcElement.addActivityElement(activities.get(cElement.getTargetIdentifier()), 2, cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());
				rcElements.put(cElement.getIdentifier(),rcElement);
			}
		}
		
		return rcElements;
		
	}

	private List<File> unzipFolder() {
		List<File> extractedFiles = new ArrayList<>();
        try {
	        File destDir = new File(".");
	        byte[] buffer = new byte[1024];
	        ZipInputStream zis = new ZipInputStream(new FileInputStream(path));
	        ZipEntry zipEntry = zis.getNextEntry();
	        while (zipEntry != null) {
	            File newFile = newFile(destDir, zipEntry);
	            extractedFiles.add(newFile);
	            
	            FileOutputStream fos = new FileOutputStream(newFile);
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
	            fos.close();
	            zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
	        zis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return extractedFiles;
	}
	
	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
         
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
         
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
         
        return destFile;
    }
	
	
	/**
	 * Read activities from Activities-File and return an ActivityMap
	 * @return activities as HashMap (identifier, ActivityElement)
	 */
	private Map<String,ActivityElement> extractActivitiesFromFile(Element rootElement) {

		Map<String,ActivityElement> activityMap = new HashMap<>();
		
		NodeList activities = rootElement.getElementsByTagName("activity");
		
		for(int i=0; i < activities.getLength(); i++) {
			
			Node node = activities.item(i);
			Element activity = (Element) node;
			String identifier = activity.getAttribute("identifier");
			NodeList nl = activity.getElementsByTagName("label");
			Element label = (Element) nl.item(0);
			
			ActivityElement aElement = new ActivityElement(i, label.getTextContent());
			activityMap.put(identifier, aElement);
		}
		
		return activityMap;
	}
	
	/**
	 * Read Positions from Positions-File and return Map
	 * @return
	 */
	private Map<String, Point2D> extractPositionsFromFile(Element rootElement) {
		Map<String, Point2D> positionMap = new HashMap<>();
		
		// extract Positions of Activities
		NodeList activityPositions = rootElement.getElementsByTagName("activityPosition");
		for(int i=0; i < activityPositions.getLength(); i++) {
			
			Node node = activityPositions.item(i);
			Element activityPosition = (Element) node;
			Element positionX = (Element) activityPosition.getElementsByTagName("positionX").item(0);
			Element positionY = (Element) activityPosition.getElementsByTagName("positionY").item(0);
			
			String identifier = activityPosition.getAttribute("identifier");
			
			positionMap.put(identifier, new Point2D(Double.parseDouble(positionX.getTextContent()), Double.parseDouble(positionY.getTextContent())));
			logger.debug("Found Activity Position of " + identifier + " X: " + positionX + " Y: "+ positionY);
		}
		
		// extract Positions of RelationConstraints
		NodeList relationPositions = rootElement.getElementsByTagName("relationPosition");
		for(int i=0; i < relationPositions.getLength(); i++) {
			
			Node node = relationPositions.item(i);
			Element relationPosition = (Element) node;
			Element positionX = (Element) relationPosition.getElementsByTagName("positionX").item(0);
			Element positionY = (Element) relationPosition.getElementsByTagName("positionY").item(0);
			
			String identifier = relationPosition.getAttribute("identifier");
			
			positionMap.put(identifier, new Point2D(Double.parseDouble(positionX.getTextContent()), Double.parseDouble(positionY.getTextContent())));
			logger.debug("Found Relation Position of " + identifier + " X: " + positionX.getTextContent() + " Y: "+ positionY.getTextContent());
		}
		
		return positionMap;
	}
	
	/**
	 * Read Constraints from Constraints-File and return them
	 * @return
	 */
	private List<ConstraintElement> extractConstraintsFromFile(Element rootElement) {
		List<ConstraintElement> constraintMap = new ArrayList<>();
		
		// extract RelationConstraints
		NodeList existenceConstraints = rootElement.getElementsByTagName("existenceConstraint");
		for(int i=0; i < existenceConstraints.getLength(); i++) {
			Node node = existenceConstraints.item(i);
			Element existenceConstraint = (Element) node;
			String template = existenceConstraint.getAttribute("template");
			Element support = (Element) existenceConstraint.getElementsByTagName("support").item(0);
			Element confidence = (Element) existenceConstraint.getElementsByTagName("confidence").item(0);
			Element interest = (Element) existenceConstraint.getElementsByTagName("interest").item(0);
			Element activity = (Element) existenceConstraint.getElementsByTagName("activity").item(0);
			Element minElement = (Element) existenceConstraint.getElementsByTagName("min").item(0);
			Element maxElement = (Element) existenceConstraint.getElementsByTagName("max").item(0);
			
			String min = minElement != null ? minElement.getTextContent() : null;
			String max = minElement != null ? maxElement.getTextContent() : null;
			constraintMap.add(new ConstraintElement(activity.getTextContent(), template, activity.getTextContent(), null, Double.parseDouble(support.getTextContent()), Double.parseDouble(confidence.getTextContent()), Double.parseDouble(interest.getTextContent()), true, min, max));
			logger.debug("Found ExistenceConstraint of " + activity.getTextContent() + " Template " + template +  " Support: " + support.getTextContent() + " Confidence: " + confidence.getTextContent() + " Interest: " + interest.getTextContent());

		}
		
		// extract RelationConstraints
		NodeList relationConstraints = rootElement.getElementsByTagName("relationConstraint");
		for(int i=0; i < relationConstraints.getLength(); i++) {
			Node node = relationConstraints.item(i);
			Element relationConstraint = (Element) node;
			String identifier = relationConstraint.getAttribute("identifier");
			String template = relationConstraint.getAttribute("template");
			Element support = (Element) relationConstraint.getElementsByTagName("support").item(0);
			Element confidence = (Element) relationConstraint.getElementsByTagName("confidence").item(0);
			Element interest = (Element) relationConstraint.getElementsByTagName("interest").item(0);
			Element sourceActivity = (Element) relationConstraint.getElementsByTagName("sourceActivity").item(0);
			Element targetActivity = (Element) relationConstraint.getElementsByTagName("targetActivity").item(0);
			constraintMap.add(new ConstraintElement(identifier, template, sourceActivity.getTextContent(), targetActivity.getTextContent(), Double.parseDouble(support.getTextContent()), Double.parseDouble(confidence.getTextContent()), Double.parseDouble(interest.getTextContent()), false, null, null));
			logger.debug("Found RelConstraint " + identifier + " Template " + template + " Support: " + support.getTextContent() + " Confidence: " + confidence.getTextContent() + " Interest: " + interest.getTextContent() + " Source: " + sourceActivity.getTextContent() + " Target: " + targetActivity.getTextContent());

		}
		
		return constraintMap;
		
	}
	
	private Element getRootElement(File file) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			
			if (file.exists()) {
				Document doc = db.parse(file);
				return doc.getDocumentElement();
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	// Helper-Class to temp store Constraints-Info
	class ConstraintElement {
		
		String identifier;
		String template;
		String sourceIdentifier;
		String targetIdentifier;
		double support;
		double confidence;
		double interest;
		boolean existenceConstraint;
		String min;
		String max;

		public ConstraintElement(String identifier, String template, String sourceIdentifier, String targetIdentifier, double support,
				double confidence, double interest, boolean existenceConstraint, String min, String max) {
			super();
			this.identifier = identifier;
			this.template = template;
			this.sourceIdentifier = sourceIdentifier;
			this.targetIdentifier = targetIdentifier;
			this.support = support;
			this.confidence = confidence;
			this.interest = interest;
			this.existenceConstraint = existenceConstraint;
			this.min = min;
			this.max = max;
		}
		
		public String getMin() {
			return min;
		}

		public void setMin(String min) {
			this.min = min;
		}

		public String getMax() {
			return max;
		}

		public void setMax(String max) {
			this.max = max;
		}

		public String getIdentifier() {
			return identifier;
		}

		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		public String getSourceIdentifier() {
			return sourceIdentifier;
		}

		public void setSourceIdentifier(String sourceIdentifier) {
			this.sourceIdentifier = sourceIdentifier;
		}

		public String getTargetIdentifier() {
			return targetIdentifier;
		}

		public void setTargetIdentifier(String targetIdentifier) {
			this.targetIdentifier = targetIdentifier;
		}

		public double getSupport() {
			return support;
		}

		public void setSupport(double support) {
			this.support = support;
		}

		public double getConfidence() {
			return confidence;
		}

		public void setConfidence(double confidence) {
			this.confidence = confidence;
		}

		public double getInterest() {
			return interest;
		}

		public void setInterest(double interest) {
			this.interest = interest;
		}

		public boolean isExistenceConstraint() {
			return existenceConstraint;
		}

		public void setExistenceConstraint(boolean existenceConstraint) {
			this.existenceConstraint = existenceConstraint;
		}
		
		
	}

}

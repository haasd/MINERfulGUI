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
import minerful.gui.model.CardinalityElement;
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
			
			if("ProcessSpecification.xml".equals(file.getName())) {
				activities = extractActivitiesFromFile(rootElement);
				constraintList = extractConstraintsFromFile(rootElement);
			} else if ("Positions.xml".equals(file.getName())) {
				positions = extractPositionsFromFile(rootElement);
			} else if ("Templates.xml".equals(file.getName())) {
				//TODO: must be implemented
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
			
			if(cElement.getParams().size() == 1) {
				
				// determine corresponding ActivityElement
				ActivityElement aElement = activities.get(cElement.getParams().get(0).get(0));
				
				if(aElement.getExistenceConstraint() == null) {
					aElement.setExistenceConstraint(new XMLExistenceConstraint());
				}
				
				// set Existence Constraints
				if(ExistenceConstraintEnum.INIT.getTemplateLabel().equals(cElement.getTemplate())) {
					aElement.getExistenceConstraint().setInitConstraint(new StructureElement(true,cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
				} else if(ExistenceConstraintEnum.END.getTemplateLabel().equals(cElement.getTemplate())) {
					aElement.getExistenceConstraint().setEndConstraint(new StructureElement(true,cElement.getSupport(), cElement.getConfidence(), cElement.getInterest()));
				} else if(ExistenceConstraintEnum.AT_MOST_ONE.getTemplateLabel().equals(cElement.getTemplate())) {
					CardinalityElement cardElement = new CardinalityElement("1", cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());
					if(aElement.getExistenceConstraint().getCard() == null) {
						Card card = new Card();
						card.setMax(cardElement);
						aElement.getExistenceConstraint().setCard(card);
					} else {
						aElement.getExistenceConstraint().getCard().setMax(cardElement);
					}
				} else if(ExistenceConstraintEnum.PARTICIPATION.getTemplateLabel().equals(cElement.getTemplate())) {
					CardinalityElement cardElement = new CardinalityElement("1", cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());
					if(aElement.getExistenceConstraint().getCard() == null) {
						Card card = new Card();
						card.setMin(cardElement);
						aElement.getExistenceConstraint().setCard(card);
					} else {
						aElement.getExistenceConstraint().getCard().setMin(cardElement);
					}
				} 
			} else {
				i++;
				Template template = RelationConstraintEnum.findTemplateByTemplateLabel(cElement.getTemplate());
				RelationConstraintElement rcElement = new RelationConstraintElement(i, template);
				for(String param1 : cElement.getParams().get(0)) {
					rcElement.addActivityElement(activities.get(param1), 1, cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());
				}
				
				for(String param2 : cElement.getParams().get(1)) {
					rcElement.addActivityElement(activities.get(param2), 2, cElement.getSupport(), cElement.getConfidence(), cElement.getInterest());

				}
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
		
		NodeList activities = rootElement.getElementsByTagName("activities");
		NodeList activityList = ((Element) activities.item(0)).getElementsByTagName("activity");
		
		for(int i=0; i < activityList.getLength(); i++) {
			
			Node node = activityList.item(i);
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
	
	private List<String> determineActivityList(Element parameters) {
		List<String> activityList = new ArrayList<>();
		
		NodeList activities = parameters.getElementsByTagName("activity");
		
		for(int i=0; i < activities.getLength(); i++) {
			activityList.add(activities.item(i).getTextContent());
		}
		
		return activityList;
	}
	
	/**
	 * Read Constraints from Constraints-File and return them
	 * @return
	 */
	private List<ConstraintElement> extractConstraintsFromFile(Element rootElement) {
		List<ConstraintElement> constraintMap = new ArrayList<>();
		
		// extract constraints
		NodeList constraints = rootElement.getElementsByTagName("constraint");
		for(int i=0; i < constraints.getLength(); i++) {
			Node node = constraints.item(i);
			Element constraint = (Element) node;
			String template = constraint.getAttribute("template");
			String identifier = constraint.getAttribute("identifier");
			Element support = (Element) constraint.getElementsByTagName("support").item(0);
			Element confidence = (Element) constraint.getElementsByTagName("confidence").item(0);
			Element interest = (Element) constraint.getElementsByTagName("interest").item(0);
			Element minElement = (Element) constraint.getElementsByTagName("min").item(0);
			Element maxElement = (Element) constraint.getElementsByTagName("max").item(0);
			
			NodeList parameters = constraint.getElementsByTagName("parameter");
			List<List<String>> params = new ArrayList<>();
			
			for(int j=0; j < parameters.getLength(); j++) {
				params.add(determineActivityList((Element) parameters.item(j)));
			}
			
			String min = minElement != null ? minElement.getTextContent() : null;
			String max = minElement != null ? maxElement.getTextContent() : null;
			constraintMap.add(new ConstraintElement(identifier, template, params, Double.parseDouble(support.getTextContent()), Double.parseDouble(confidence.getTextContent()), Double.parseDouble(interest.getTextContent()), true, min, max));
			logger.debug("Found Constraint " + identifier + " Template " + template +  " Support: " + support.getTextContent() + " Confidence: " + confidence.getTextContent() + " Interest: " + interest.getTextContent());

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
		List<List<String>> params;
		List<String> targetIdentifier;
		double support;
		double confidence;
		double interest;
		boolean existenceConstraint;
		String min;
		String max;

		public ConstraintElement(String identifier, String template, List<List<String>> params, double support,
				double confidence, double interest, boolean existenceConstraint, String min, String max) {
			super();
			this.identifier = identifier;
			this.template = template;
			this.params = params;
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
		
		public List<List<String>> getParams() {
			return params;
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

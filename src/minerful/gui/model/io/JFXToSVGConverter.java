package minerful.gui.model.io;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

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

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfxconverter.JFXConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode.Cursor;
import minerful.gui.model.RelationConstraintElement;
import minerful.gui.model.Template;
import minerful.gui.service.ProcessElementInterface;
import minerful.gui.util.Config;

public class JFXToSVGConverter {
	
	Config config = new Config("config");
	
	
	private final double activityRadius = config.getDouble("activity.radius");
	private final double constraintRadius = config.getDouble("constraint.radius");
	
	private final double cursorWidth = config.getDouble("arrow.width");
	private final double cursorHeight = config.getDouble("arrow.height");
	
	private ProcessElementInterface processTab;


	private SVGGraphics2D g2D;

	private Color lightBlueColor = new Color(196f/255f, 232f/255f, 247f/255f);
	private Color darkBlueColor = new Color(61f/255f, 136f/255f, 195f/255f);

	private JFXConverter converter;

	public JFXToSVGConverter(ProcessElementInterface processTab) {
		
		this.processTab = processTab;
		
	}

   /**
    * Creates the Document corresponding to the Node, and write it to the output.
    *
    * @param node the Node
    */
   public void createDocument(File outputFile) {
	         
		 if (outputFile != null) {
		
			try {
				outputFile.createNewFile();
				Writer writer = new BufferedWriter(new FileWriter(outputFile));
				createSVGStream();
				g2D.stream(writer, true);
		          
		        writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}  
   
   
   private void createSVGStream() {
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.getDOMImplementation().createDocument("http://www.w3.org/2000/svg", "svg", null);
		
		   //Bounds bounds = node.getBoundsInLocal();
		   Rectangle2D rec = new Rectangle2D.Double(0,0,processTab.getBackgroundPane().getWidth(),processTab.getBackgroundPane().getHeight());//(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
		
		   g2D = new SVGGraphics2D(doc);
		   converter = new JFXConverter();
		   
		   // convert everything automatically (bug with rotation)
		   //converter.convert(g2D, node);
		   
		   // convert manually
		   //lines
		   for(RelationConstraintElement cElem : processTab.getCurrentProcessElement().getConstraintEList()){
		
			   g2D.setPaint(darkBlueColor); 
			   if(cElem.getTemplate().getNegation()){
				   //set the stroke of the copy, not the original 
		           Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
				   g2D.setStroke(dashed);
			   } else {
				   g2D.setStroke(new BasicStroke(1));
			   }
			   
			   for (ActivityElement aElem : cElem.getParameter1Elements()){
				   Line2D.Double line2D = new Line2D.Double(cElem.getPosX() + constraintRadius, cElem.getPosY() + constraintRadius,
						   aElem.getPosX() + activityRadius, aElem.getPosY() + activityRadius);
				   g2D.draw(line2D);
			   }
			  
			   for (ActivityElement aElem : cElem.getParameter2Elements()){
				   Line2D.Double line2D = new Line2D.Double(cElem.getPosX() + constraintRadius, cElem.getPosY() + constraintRadius,
						   aElem.getPosX() + activityRadius, aElem.getPosY() + activityRadius);
				   g2D.draw(line2D);
			   }
		   }   
		   
		   //
		   g2D.setStroke(new BasicStroke(1));
		   
		   // relationConstraints
		   for(RelationConstraintElement cElem : processTab.getCurrentProcessElement().getConstraintEList()){
			   processTab.determineRelationConstraintNode(cElem).setEditable(false);
			   converter.convert(g2D, processTab.determineRelationConstraintNode(cElem));
		   }
		         
		   
		   // Activities
		   for(ActivityElement aElem : processTab.getCurrentProcessElement().getActivityEList()){
			   processTab.determineActivityNode(aElem).setEditable(false);
			   processTab.determineActivityNode(aElem).setCursorsInvisibleForExport(true);
			   converter.convert(g2D, processTab.determineActivityNode(aElem));
			   processTab.determineActivityNode(aElem).setCursorsInvisibleForExport(false);
		   }
		
		   // Cursors
		  for(RelationConstraintElement cElem : processTab.getCurrentProcessElement().getConstraintEList()){
			  for(ActivityElement aElem : cElem.getParameter1Elements()){
				  drawCursorsAndAlternateLabel(aElem, cElem);
			  }
			  
			  for(ActivityElement aElem : cElem.getParameter2Elements()){
				  drawCursorsAndAlternateLabel(aElem, cElem);  
			  }
		  } 
		  
		  // get the root element and add size
		  Double width = rec.getWidth()- 250d + 2 * config.getDouble("activity.radius");
		  Double height = rec.getHeight() - 250 +  + 2 * config.getDouble("activity.radius");
		    
		      g2D.setSVGCanvasSize(new Dimension(width.intValue(), height.intValue()));
		      
		 } catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
    }

	private void drawCursorsAndAlternateLabel( ActivityElement aElem, RelationConstraintElement cElem){
		
		   Cursor cursor = processTab.determineActivityNode(aElem).getCursorByConstraint(processTab.determineRelationConstraintNode(cElem));
		   if(cursor != null){
			   double angle = cursor.getInsideRotate().getAngle();
			   double adaptedRadius = activityRadius - 1;
			   double centerX = aElem.getPosX() + activityRadius + adaptedRadius* Math.cos(Math.toRadians(angle));
			   double centerY = aElem.getPosY() + activityRadius + adaptedRadius * Math.sin(Math.toRadians(angle));
			   Double posX1, posY1, posX2, posY2, posX3, posY3;
			   
			   Template template = cElem.getTemplate();
			   
			   boolean createDiamond = processTab.determineActivityNode(aElem).getChildren().contains(cursor.getInsideCursor()) && processTab.determineActivityNode(aElem).getChildren().contains(cursor.getOutsideCursor());
			   
			   //Graphics2D g2dTemp = (Graphics2D) g2D.create();
			   if (processTab.determineActivityNode(aElem).getChildren().contains(cursor.getOutsideCursor())){
				   posX1 = centerX + Math.cos(Math.toRadians(angle + 90)) * cursorWidth / 2;  
				   posY1 = centerY + Math.sin(Math.toRadians(angle + 90)) * cursorWidth / 2;
				   posX2 = centerX + Math.cos(Math.toRadians(angle)) * cursorHeight;  
				   posY2 = centerY + Math.sin(Math.toRadians(angle)) * cursorHeight;
				   posX3 = centerX + Math.cos(Math.toRadians(angle - 90)) * cursorWidth / 2;  
				   posY3 = centerY + Math.sin(Math.toRadians(angle - 90)) * cursorWidth / 2;
				   GeneralPath gPath = new GeneralPath();
				   gPath.moveTo(posX1, posY1);
		           gPath.lineTo(posX2, posY2);
		           gPath.lineTo(posX3, posY3);		         
		           if(!createDiamond) gPath.closePath();
		          
		           if(template.getNegation()){
		        	   g2D.setPaint(Color.WHITE);
		           } else {
		        	   g2D.setPaint(darkBlueColor);	
		           }
		           
		           g2D.fill(gPath);
		           g2D.setColor(darkBlueColor);
	
		           g2D.draw(gPath);
			   }
			   
			   if (processTab.determineActivityNode(aElem).getChildren().contains(cursor.getInsideCursor())){
				   posX1 = centerX + Math.cos(Math.toRadians(angle + 90)) * (cursorWidth / 2 - 1);  
				   posY1 = centerY + Math.sin(Math.toRadians(angle + 90)) * (cursorWidth / 2 - 1);
				   posX2 = centerX - Math.cos(Math.toRadians(angle)) * cursorHeight;  
				   posY2 = centerY - Math.sin(Math.toRadians(angle)) * cursorHeight;
				   posX3 = centerX + Math.cos(Math.toRadians(angle - 90)) * (cursorWidth / 2 - 1);  
				   posY3 = centerY + Math.sin(Math.toRadians(angle - 90)) * (cursorWidth / 2 - 1);
				   GeneralPath gPath = new GeneralPath();
				   gPath.moveTo(posX1, posY1);
		           gPath.lineTo(posX2, posY2);
		           gPath.lineTo(posX3, posY3);		         
		           if(!createDiamond) gPath.closePath();
		           
		           if(template.getNegation()){
		        	   g2D.setPaint(Color.WHITE);
		           } else {
		        	   g2D.setPaint(darkBlueColor);	
		           }
		           g2D.fill(gPath);
		           g2D.setColor(darkBlueColor);
	
		           g2D.draw(gPath);
			   }
			   
			   g2D.setFont(new Font("Serif", Font.PLAIN, 16));
			   double fontHeight = 12;
			   if(template.getP1Alternation() && cElem.getParameter1Elements().contains(aElem)){
				   Double AlternatePosX = centerX - Math.cos(Math.toRadians(angle + 90)) * (cursorWidth / 2) + Math.cos(Math.toRadians(angle)) * cursorHeight;
				   Double AlternatePosY = centerY - Math.sin(Math.toRadians(angle + 90)) * (cursorWidth / 2) + Math.sin(Math.toRadians(angle)) * cursorHeight;
				   
				   AffineTransform originalTranform = g2D.getTransform();
				   g2D.rotate(Math.toRadians(angle), AlternatePosX, AlternatePosY);
				   g2D.drawString("I", AlternatePosX.floatValue(), AlternatePosY.floatValue());
				   g2D.setTransform(originalTranform);
			   }
			   
			   if(template.getP2Alternation() && cElem.getParameter2Elements().contains(aElem)){ 
				   Double AlternatePosX = centerX + Math.cos(Math.toRadians(angle + 90)) * (cursorWidth / 2 + fontHeight ) + Math.cos(Math.toRadians(angle)) * cursorHeight;
				   Double AlternatePosY = centerY + Math.sin(Math.toRadians(angle + 90)) * (cursorWidth / 2 + fontHeight ) + Math.sin(Math.toRadians(angle)) * cursorHeight;
				   
				   AffineTransform originalTranform = g2D.getTransform();
				   g2D.rotate(Math.toRadians(angle), AlternatePosX, AlternatePosY);
				   g2D.drawString("I", AlternatePosX.floatValue(), AlternatePosY.floatValue());
				   g2D.setTransform(originalTranform);
			   }
			   
		   }
	}
	
	public SVGGraphics2D getG2D() {
		return g2D;
	}

	public void setG2D(SVGGraphics2D g2d) {
		g2D = g2d;
	}

}

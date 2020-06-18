package minerfulgui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import minerful.automaton.concept.weight.WeightedAutomaton;
import minerful.automaton.encdec.WeightedAutomatonFactory;
import minerful.concept.ProcessModel;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerfulgui.common.ModelInfo;

public class AutomataGeneratorTabController extends AbstractController implements Initializable {

	Logger logger = Logger.getLogger(AutomataGeneratorTabController.class);

	private ModelInfo modelInfo;

	@FXML
	WebView webView;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public void displayAutomaton() throws FileNotFoundException, TransformerException, JAXBException {

		// Create Automaton XML
		
		String weightedXml = printWeightedXmlAutomaton(modelInfo.getLogParser(), false);
		if(weightedXml != null) {
			File automatonXml = new File("help.xml");
			PrintWriter outWriter = new PrintWriter(automatonXml);
			outWriter.print(weightedXml);
			outWriter.flush();
			outWriter.close();
			transitionCluster();
			transitionFurtherCluster();
			weightedAutomatonDotter();
			
			try {
				File myObj = new File("help.clustered.withnot.xml.dot");
				Scanner myReader = new Scanner(myObj);
				StringBuilder sb = new StringBuilder();

				while (myReader.hasNextLine()) {
					sb.append(myReader.nextLine());
				}
				myReader.close();
				
				deleteAllFiles();
				
				String content = sb.toString().replace("&gt;", ">");
				content = content.replace("'", "\\'");
			    String automaton = content;

				webView.getEngine().load((getClass().getClassLoader().getResource("javascript/test.html")).toString());

				webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

					@Override
					public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
						if (newValue == State.SUCCEEDED) {
							webView.getEngine().executeScript("setModel('" + automaton + "')");
						}
					}
				});
				
				
				webView.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
				    if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.EQUALS
				            || e.getCode() == KeyCode.PLUS) {
				        webView.setZoom(webView.getZoom() * 1.1);
				    }
				    else if(e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS ){
				        webView.setZoom(webView.getZoom() / 1.1);
				    }
				}); 
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}

	}

	private void deleteAllFiles() {
		logger.info("help.xml was deleted " + new File("help.xml").delete());
		logger.info("help.clustered.xml was deleted " + new File("help.clustered.xml").delete());
		logger.info("help.clustered.withnot.xml was deleted " + new File("help.clustered.withnot.xml").delete());
		logger.info("help.clustered.withnot.xml.dot was deleted " + new File("help.clustered.withnot.xml.dot").delete());
	}

	public void transitionCluster() throws FileNotFoundException, TransformerException {
		TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource(
				(getClass().getClassLoader().getResource("dot-xsl/transitionCluster.xsl")).toString()));
		transformer.transform(new StreamSource("help.xml"),
				new StreamResult(new FileOutputStream("help.clustered.xml")));
	}

	public void transitionFurtherCluster() throws FileNotFoundException, TransformerException {
		TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource(
				(getClass().getClassLoader().getResource("dot-xsl/transitionFurtherCluster.xsl")).toString()));
		transformer.transform(new StreamSource("help.clustered.xml"),
				new StreamResult(new FileOutputStream("help.clustered.withnot.xml")));
	}

	public void weightedAutomatonDotter() throws FileNotFoundException, TransformerException {
		TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource(
				(getClass().getClassLoader().getResource("dot-xsl/weightedAutomatonDotter.xsl")).toString()));
		transformer.setParameter("DO_WEIGH_LINES", "true()");
		transformer.setParameter("DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED", "true()");
		transformer.transform(new StreamSource("help.clustered.withnot.xml"),
				new StreamResult(new FileOutputStream("help.clustered.withnot.xml.dot")));
	}

	public String printWeightedXmlAutomaton(LogParser logParser, boolean skimIt) throws JAXBException {
		Automaton processAutomaton = modelInfo.getProcessModel().buildAutomaton();

		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(
				TaskCharEncoderDecoder.getTranslationMap(modelInfo.getProcessModel().bag));
		WeightedAutomaton wAut = wAF.augmentByReplay(processAutomaton, logParser, skimIt);

		if (wAut == null)
			return null;

		JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(wAut, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();

		// OINK
		strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
				strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
				" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");

		return strixWriter.toString();
	}

}

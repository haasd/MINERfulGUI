package minerful.gui.graph.util;

import java.util.EnumSet;

import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;

public class ModelGeneratorGraphMouseManager implements MouseManager {
	/**
	 * The view this manager operates upon.
	 */
	protected View view;

	/**
	 * The graph to modify according to the view actions.
	 */
	protected GraphicGraph graph;
	
	private ProcessModel processModel;
	
	private Stage stage = new Stage();
	
	private StackPane stackPane = new StackPane();

	final private EnumSet<InteractiveElement> types;

	public ModelGeneratorGraphMouseManager(EnumSet<InteractiveElement> types, ProcessModel processModel, Stage stage) {
		this.types = types;
		this.processModel = processModel;
		
		this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.initModality(Modality.NONE);
        this.stage.initOwner(stage);
        
        stackPane.getStylesheets().add(getClass().getClassLoader().getResource("css/main.css").toExternalForm());
        stackPane.getStyleClass().add("lightbox");
        
        Scene scene = new Scene(stackPane, 300.0, 400.0);
        scene.setFill(Color.TRANSPARENT);
        this.stage.setScene(scene);
	}

	public void init(GraphicGraph graph, View view) {
		this.view = view;
		this.graph = graph;
		view.addListener(MouseEvent.MOUSE_PRESSED, mousePressed);
		view.addListener(MouseEvent.MOUSE_DRAGGED, mouseDragged);
		view.addListener(MouseEvent.MOUSE_RELEASED, mouseRelease);
		view.addListener(ScrollEvent.SCROLL, mouseScroll);
	}
	
	// Command
	protected void mouseButtonPress(MouseEvent event) {
		view.requireFocus();
		
		if (event.getButton() == MouseButton.SECONDARY) {
			VBox vbox = new VBox();
			Label controlLabel = new Label("Control");
			controlLabel.getStyleClass().add("section-header2");
			vbox.getChildren().add(controlLabel);
			
			
			stackPane.getChildren().clear();
			stackPane.getChildren().add(vbox);
			
			stage.setX(event.getSceneX()- 300);
			stage.setY(event.getSceneY());
			stage.show();
			stage.toFront();
		} else {
			unselectAllElements();
			stage.close();
		}
		
	}
	
	protected void mouseButtonRelease(MouseEvent event,
									  Iterable<GraphicElement> elementsInArea) {
		for (GraphicElement element : elementsInArea) {
			
		}
	}
	
	protected void mouseButtonPressOnElement(GraphicElement element,
											 MouseEvent event) {
		view.freezeElement(element, true);
		unselectAllElements();
		if (event.getButton() == MouseButton.SECONDARY) {
			element.setAttribute("ui.selected");
			
			GraphicNode node = (GraphicNode) element;
			
			VBox vbox = new VBox();
			stackPane.getChildren().clear();
			stackPane.getChildren().add(vbox);
			
			if(node.getDegree() != 0) {
				node.edges().forEach(edge -> {edge.setAttribute("ui.selected");});
			}
			
			stage.setX(event.getSceneX()- 600);
			stage.setY(event.getSceneY());
			stage.show();
			stage.toFront();
			
		} else {
			element.setAttribute("ui.selected");
			if(element.getClass() == GraphicNode.class) {
				GraphicNode node = (GraphicNode) element;
				if(node.getDegree() != 0) {
					node.edges().forEach(edge -> {edge.setAttribute("ui.selected");});
				}		
			}
			
		}
	}
	
	protected void elementMoving(GraphicElement element, MouseEvent event) {
		view.moveElementAtPx(element, event.getX(), event.getY());
	}
	
	protected void mouseButtonReleaseOffElement(GraphicElement element,
												MouseEvent event) {
		view.freezeElement(element, false);
		if (event.getButton() != MouseButton.SECONDARY) {
			element.removeAttribute("ui.clicked");
		}
	}
	
	// Mouse Listener

	protected GraphicElement curElement;

	protected double x1, y1;
	
	EventHandler<MouseEvent> mousePressed = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {			
			curElement = view.findGraphicElementAt(types, e.getX(), e.getY());

			if (curElement != null) {
				mouseButtonPressOnElement(curElement, e);
			} else {
				x1 = e.getX();
				y1 = e.getY();
				mouseButtonPress(e);
				view.beginSelectionAt(x1, y1);
			}
		}
	};
	
	EventHandler<MouseEvent> mouseDragged = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if (curElement != null) {
				elementMoving(curElement, event);
			} else {
				view.selectionGrowsAt(event.getX(), event.getY());
			}
		}
	};	
	
	EventHandler<ScrollEvent> mouseScroll = new EventHandler<ScrollEvent>() {
		@Override
		public void handle(ScrollEvent event) {
			if(event.getDeltaY() < 0) {
				view.getCamera().setViewPercent(view.getCamera().getViewPercent() - 0.1);
			} else {
				view.getCamera().setViewPercent(view.getCamera().getViewPercent() + 0.1);
			}
			
		}
	};	
	
	EventHandler<MouseEvent> mouseRelease = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if (curElement != null) {
				mouseButtonReleaseOffElement(curElement, event);
				curElement = null;
			} else {
				double x2 = event.getX();
				double y2 = event.getY();
				double t;

				if (x1 > x2) {
					t = x1;
					x1 = x2;
					x2 = t;
				}
				if (y1 > y2) {
					t = y1;
					y1 = y2;
					y2 = t;
				}

				mouseButtonRelease(event, view.allGraphicElementsIn(types,x1, y1, x2, y2));
				view.endSelectionAt(x2, y2);
			}
		}
	};
	
	@Override
	public void release() {
		view.removeListener(MouseEvent.MOUSE_PRESSED, mousePressed);
		view.removeListener(MouseEvent.MOUSE_DRAGGED, mouseDragged);
		view.removeListener(MouseEvent.MOUSE_RELEASED, mouseRelease);
	}

	@Override
	public EnumSet<InteractiveElement> getManagedTypes() {
		// TODO Auto-generated method stub
		return types;
	}

	private void unselectAllElements() {
		graph.nodes().filter(n -> n.hasAttribute("ui.selected")).forEach(n -> n.removeAttribute("ui.selected"));
		graph.edges().filter(n -> n.hasAttribute("ui.selected")).forEach(n -> n.removeAttribute("ui.selected"));
		graph.sprites().filter(s -> s.hasAttribute("ui.selected")).forEach(s -> s.removeAttribute("ui.selected"));
	}
}

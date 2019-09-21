package minerful.gui.model;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import minerful.gui.controller.ModelGeneratorTabController;
import minerful.gui.util.Config;

public class EventHandlerManager {
	private ModelGeneratorTabController processTab;
	private Config config = new Config("config");
	
	private int parameterNumber;
	
	
	public EventHandlerManager(ModelGeneratorTabController processTab){
		this.processTab = processTab;
	}

	/*
	 * EVENTHANDLER for Dragging elements
	 */
	private double activityX, activityY;
	private double activityTranslateX, activityTranslateY;
	
	/**
	 * Updates the activity or constraint of the dragged and released Node
	 * Updates the saved processPosition element
	 */
	private EventHandler<MouseEvent> activityOnMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
		@Override
        public void handle(MouseEvent t) {
           ActivityNode aNode = (ActivityNode)(t.getSource());
           //System.out.println("ActivityNode was selected: ID: " + aNode.getActivityElement().getId());
           //Update activityElement
           aNode.updateActivityElement();
       
           aNode.getActivityElement().updateAllLineNodePositions();
           for (RelationConstraintElement cElem : aNode.getActivityElement().getConstraintList()){
           	cElem.getConstraintNode().updatePosition();
           }
       
           if(processTab != null) {
        	   processTab.getConstraintPane().setDisable(true);
               processTab.getActivityPane().setDisable(false);
               processTab.getEditTabPane().setExpandedPane(processTab.getActivityPane());
               EditActivityPane aPane = (EditActivityPane) processTab.getActivityPane().getContent();
               aPane.getActivityNameTF().requestFocus();
               aPane.getActivityNameTF().selectAll();
               
               //Update selected element
               if (processTab.getSelectedElement() != null){
            	   processTab.getSelectedElement().setEditable(false);        	   
               }
               
               processTab.setSelectedElement(aNode);
               aNode.setEditable(true);
           }
		}
		
	}; 
		
	private EventHandler<MouseEvent> activityOnMousePressedEventHandler = new EventHandler<MouseEvent>() {
		 
        @Override
        public void handle(MouseEvent t) {
            activityX = t.getSceneX();
            activityY = t.getSceneY();
            activityTranslateX = ((Node)((ActivityNode)(t.getSource()))).getTranslateX();
            activityTranslateY = ((Node)((ActivityNode)(t.getSource()))).getTranslateY();
            
            ActivityNode aNode = (ActivityNode)(t.getSource());
            aNode.toFront();
            
            if(processTab != null) {
            	//Update selected element
                if (processTab.getSelectedElement() != null){
             	   processTab.getSelectedElement().setEditable(false);        	   
                }
                processTab.setSelectedElement(aNode);
                aNode.setEditable(true);
            }
        }
    };
     
    private EventHandler<MouseEvent> activityOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            double offsetX = t.getSceneX() - activityX;
            double offsetY = t.getSceneY() - activityY;
            double newTranslateX = Math.max(config.getDouble("sheet.padding"), activityTranslateX + offsetX);
            double newTranslateY = Math.max(config.getDouble("sheet.padding"), activityTranslateY + offsetY);
            ActivityNode aNode = (ActivityNode)(t.getSource());
                  

            
             
            //Update ActivityNode Position on Screen
            ((Node)((ActivityNode)(t.getSource()))).setTranslateX(newTranslateX);
            ((Node)((ActivityNode)(t.getSource()))).setTranslateY(newTranslateY);
            
            for (RelationConstraintElement cElem : aNode.getActivityElement().getConstraintList()){
            	 if (!cElem.isPositionFixed()){
            		 cElem.getConstraintNode().moveConstraintBetweenActivities();
            	 }
            }
            
            aNode.getActivityElement().updateAllLineNodePositions();
            if(processTab != null) {
            	processTab.setMaxTranslate();
            }
        }
    };
    
	public void setEventHandler(ActivityNode a){
		a.setOnMousePressed(activityOnMousePressedEventHandler);
		a.setOnMouseDragged(activityOnMouseDraggedEventHandler);
		a.setOnMouseReleased(getActivityOnMouseReleasedEventHandler());
	}
	
	// ------------------------------- CONSTRAINT NODES -------------------------------------
	public void setEventHandler(RelationConstraintNode c){
		c.setOnMousePressed(constraintOnMousePressedEventHandler);
		c.setOnMouseDragged(constraintOnMouseDraggedEventHandler);
		c.setOnMouseReleased(constraintOnMouseReleasedEventHandler);
	}

    
	private double constraintX, constraintY;
	private double constraintTranslateX, constraintTranslateY;
	
	private EventHandler<MouseEvent> constraintOnMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
		@Override
        public void handle(MouseEvent t) {
           RelationConstraintNode cNode = (RelationConstraintNode)(t.getSource());
          
           //System.out.println("ConstraintNode was selected: ID: " + cNode.getConstraintElement().getId());
           
           //Update constraintElement
           cNode.updatePosition(); 
           cNode.getConstraintElement().setPositionFixed(true);
           
           if(processTab != null) {
	           if (processTab.getSelectedElement() != null){
	        	   processTab.getSelectedElement().setEditable(false);        	   
	           }
	           processTab.setSelectedElement(cNode);
	           cNode.setEditable(true);
	           
	           processTab.getConstraintPane().setDisable(false);
	           processTab.getActivityPane().setDisable(true);
	           processTab.getEditTabPane().setExpandedPane(processTab.getConstraintPane());
           }
		}
	}; 
		
	private EventHandler<MouseEvent> constraintOnMousePressedEventHandler = new EventHandler<MouseEvent>() {
		 
        @Override
        public void handle(MouseEvent t) {
        	
            constraintX = t.getSceneX();
            constraintY = t.getSceneY();
            constraintTranslateX = ((Node)(t.getSource())).getTranslateX();
            constraintTranslateY = ((Node)(t.getSource())).getTranslateY();
        }
    };
     
    private EventHandler<MouseEvent> constraintOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent t) {
            double offsetX = t.getSceneX() - constraintX;
            double offsetY = t.getSceneY() - constraintY;
            double newTranslateX = Math.max(config.getDouble("sheet.padding"), constraintTranslateX + offsetX);
            double newTranslateY = Math.max(config.getDouble("sheet.padding"), constraintTranslateY + offsetY);
            
            RelationConstraintNode cNode = (RelationConstraintNode)(t.getSource());
             
            //Update Constraint Position on Scene
            ((RelationConstraintNode)(t.getSource())).setTranslateX(newTranslateX);
            ((RelationConstraintNode)(t.getSource())).setTranslateY(newTranslateY);
            
            //Update Line Positions on Scene     
            
            for(LineNode line : cNode.getConstraintElement().getParameter1Lines()){
          	   line.updateLinePosition();
            }
            for(LineNode line : cNode.getConstraintElement().getParameter2Lines()){
           	   line.updateLinePosition();
            }

            if(processTab != null) {
	            //Update BackgroundTab (Sheet)
	            processTab.setMaxTranslate();
            }
        }
    };
	
	// --------------------------------BACKGROUND PANE ---------------------------------------

    /**
     * BACKGROUNDPANE
     */
    private EventHandler<MouseEvent> noElementReleasedEventHandler = new EventHandler<MouseEvent>() {
		@Override
        public void handle(MouseEvent t) {
			if(processTab.getSelectedElement() != null ) {
				processTab.getSelectedElement().setEditable(false);
				processTab.setSelectedElement(null);
				processTab.getConstraintPane().setDisable(true);
		        processTab.getActivityPane().setDisable(true);
			}
		}
    };
    
    public void setEventHandler(BorderPane backgroundPane){
    	backgroundPane.setOnMouseReleased(noElementReleasedEventHandler);
    }
	
    // ----------------------------- SELECTION EVENTS ----------------------------------------
    
    /**
     * EventHandler that is added to every Activity that can be selected in SelectionMode
     * SelectionMode is used to adjust constraints
     * 1. add new constraints
     * 2. change Parameter1 or Parameter2 of Constraint
     * 3. add additional activity to Parameter1 Side
     * 4. add additional activity to Parameter1 Side
     */

    
	
	private EventHandler<MouseEvent> selectAndIncludeActivityHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				
				if(processTab != null) {
					//System.out.println("A new Activity was selected.");
					ActivityNode aNode2 = (ActivityNode) t.getSource();
					if(processTab.isAddConstraintMode() == AddConstraintMode.NEW_CONSTRAINT){
						ActivityNode aNode1 = (ActivityNode) processTab.getSelectedElement();
						processTab.addNewRelationConstraint(aNode1.getActivityElement(), aNode2.getActivityElement());
						//reset modus to normal
						processTab.resetToNormalState();
						//processTab.setSelectionModeToAddConstraint(false);
					} else if(processTab.isAddConstraintMode() == AddConstraintMode.EXCHANGE_ACTIVITY) {
						processTab.adjustRelationConstraint(aNode2, parameterNumber);
						//reset modus to normal
						processTab.resetToNormalState();
						//processTab.setSelectionModeToChangeConstraint(false, null, null);
					} else if(processTab.isAddConstraintMode() == AddConstraintMode.ADD_TO_PARAMETER1 ) {
						processTab.addAdditionalActivity(aNode2.getActivityElement(), (RelationConstraintNode)processTab.getSelectedElement(), 1);
						processTab.resetToNormalState();
					} else if(processTab.isAddConstraintMode() == AddConstraintMode.ADD_TO_OPARAMETER2){
						processTab.addAdditionalActivity(aNode2.getActivityElement(), (RelationConstraintNode)processTab.getSelectedElement(), 2);
						
						processTab.resetToNormalState();					
					}
				}
				
			}
		};

	/**
	 * adds 
	 * @param node
	 */
	public void setActivityToSelectionMode(ActivityNode aNode, int parameterNumber) {
		this.parameterNumber = parameterNumber;
		aNode.setOnMousePressed(null);
		aNode.setOnMouseDragged(null);
		aNode.setOnMouseReleased(null);
		aNode.setOnMouseReleased(selectAndIncludeActivityHandler);
		
	}
    
	public EventHandler<MouseEvent> getActivityOnMouseReleasedEventHandler() {
		return activityOnMouseReleasedEventHandler;
	}
	
}


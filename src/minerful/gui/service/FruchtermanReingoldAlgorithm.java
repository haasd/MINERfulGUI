package minerful.gui.service;

import java.util.List;

import minerful.gui.model.ActivityElement;
import minerful.gui.model.ActivityNode;
import minerful.gui.model.ProcessElement;
import minerful.gui.model.RelationConstraintElement;

public class FruchtermanReingoldAlgorithm extends LayoutAlgorithm {
	
	private static final double SPEED_DIVISOR = 100;
	
	private double k;
	private List<ActivityElement> nodes;
	private List<RelationConstraintElement> edges;
	private int iterationSteps;
	
	private double gravity = 12;
	private double speed = 1;
	
	
	public FruchtermanReingoldAlgorithm(double areaWidth, double areaHeight, ProcessElement processElement,
			ProcessElementInterface processElementInterface, int iterationSteps) {
		super(areaWidth, areaHeight, processElement, processElementInterface);
		this.iterationSteps = iterationSteps;
	}

	@Override
	public void optimizeLayout() {
		nodes = this.getProcessElement().getActivityEList();
		edges = this.getProcessElement().getConstraintEList();
		
		this.k = Math.sqrt((this.getAreaWidth() * this.getAreaHeight())/ nodes.size());
		double maxDisplace = (double) (Math.sqrt(this.getAreaWidth() * this.getAreaHeight()));
		
		for(int i = 0; i < iterationSteps; i++) {
			
			//calculate repulsive forces
			for(ActivityElement node1 : nodes) {
				for(ActivityElement node2 : nodes) {
					if(node1 != node2) {
						double xDist = node1.getPosX() - node2.getPosX();
						double yDist = node1.getPosY() - node2.getPosY();
						
						double dist = Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
						
						if(dist > 0 ) {
							double repulsiveF = calcRepulsiveForce(dist);
							
							double newX = node1.getPosX() + xDist / dist * repulsiveF;
							double newY = node1.getPosY() + yDist / dist * repulsiveF;
							node1.setPosition(newX, newY);
						}
					}
				}
			}
			
			// calculate attractive forces
			for(RelationConstraintElement edge : edges) {
				for(ActivityElement sourceNode : edge.getParameter1Elements()) {
					for(ActivityElement targetNode : edge.getParameter2Elements()) {
						
						double xDist = sourceNode.getPosX() - targetNode.getPosX();
						double yDist = sourceNode.getPosY() - targetNode.getPosY();
						
						double dist = Math.min(100d, Math.sqrt(xDist * xDist + yDist * yDist));
						
						double attractiveF = calcAttractiveForce(dist);
						
						if(dist > 300) {
							
							double newSourceX = sourceNode.getPosX() - xDist / dist * attractiveF;
							double newSourceY = sourceNode.getPosY() - yDist / dist * attractiveF;
							double newTargetX = targetNode.getPosX() + xDist / dist * attractiveF;
							double newTargetY = targetNode.getPosY() + yDist / dist * attractiveF;
							
							sourceNode.setPosition(newSourceX, newSourceY);
							targetNode.setPosition(newTargetX, newTargetY);
						}
					}
				}
			}
			
			// gravity
			for(ActivityElement node : nodes) {
				double d = Math.sqrt(Math.pow(node.getPosX(),2) + Math.pow(node.getPosY(),2));
				double gf = 0.01d * k * gravity * d;
				
				double newX = node.getPosX() - (gf * node.getPosX() / d);
				double newY = node.getPosY() - (gf * node.getPosY() / d);
				
				node.setPosition(newX, newY);
			}
			
			// speed
			for(ActivityElement node : nodes) {
				
				double newX = node.getPosX() * (speed / SPEED_DIVISOR);
				double newY = node.getPosY() * (speed / SPEED_DIVISOR);
				
				node.setPosition(newX, newY);
			}
			
			for (ActivityElement node : nodes) {

                double xDist = node.getPosX();
                double yDist = node.getPosY();
                double dist = (double) Math.sqrt(node.getPosX() * node.getPosX() + node.getPosY() * node.getPosY());
                
                if (dist > 0) {
                	
                	double panelDist = maxDisplace * ((float) speed / SPEED_DIVISOR);
                    double limitedDist = Math.min(panelDist, dist);
                    
                    node.setPosition(node.getPosX() + xDist / dist * limitedDist, node.getPosY() + yDist / dist * limitedDist);
                }
            }
			
		}
		
		
		double minX = 0;
		double minY = 0;
		
		//determine minimumPosition
		for(ActivityElement node : nodes) {
			if (node.getPosX() < minX) {
				minX = node.getPosX();
			}
			if (node.getPosY() < minY){
				minY = node.getPosY();
			}
		}
		
		// add minimum Position to every node so every Node has positive Position
		for(ActivityElement node : nodes) {
			
			double posX = node.getPosX();
			double posY = node.getPosY();
			
			if(minX < 0) {
				posX += (minX * -1) + 25;
			}
			
			if(minY < 0) {
				posY += (minY * -1) + 25;
			}
			
			node.setPosition(posX, posY);
		}
		
		for(ActivityElement node : nodes) {
			ActivityNode aNode = this.getProcessElementInterface().determineActivityNode(node);
			aNode.updateNode();
			
			for (RelationConstraintElement cElem : aNode.getActivityElement().getConstraintList()){
	        	this.getProcessElementInterface().determineRelationConstraintNode(cElem).moveConstraintBetweenActivities(); 
	        }
			
			aNode.updateAllLineNodePositions();
	        
		}
		
	}
	
	private double calcAttractiveForce(double distance) {
		return (Math.pow(distance, 2) / k);
	}
	
	private double calcRepulsiveForce(double distance) {
		return (Math.pow(k, 2) / distance);
	}
	

}

package cz.cuni.mff.d3s.jdeeco.ua.map;

import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

public class LinkPosition {

	private Link link;
	
	private double distance;
	
	private double epsilon = 0.001; // 1 mm
	
	public LinkPosition(Link link){
		startFrom(link);
	}
	
	public void startFrom(Link link){
		this.link = link;
		distance = 0;
	}
	
	public Node atNode(){
		if(link == null) throw new IllegalStateException(
				"Requested atNode() but no Link is set.");
		if(isAtStart()){
			return link.getFrom();
		}
		if(isEndReached()){
			return link.getTo();
		}
		return null;
	}
	
	public double getDistance(){
		return distance;
	}
	
	public double getRemainingDistance(){
		return link.getLength() - distance;
	}
	
	public void move(double length)	{
		if(!Double.isFinite(length)) throw new IllegalArgumentException(
				"The length argument is not finite number.");
		if(length < 0) throw new IllegalArgumentException(
				"The length argument is a negative number.");
		
		distance += length;
		if(distance > link.getLength()){
			distance = link.getLength();
		}
	}
	
	public boolean isEndReached(){
		return Math.abs(distance - link.getLength()) < epsilon;
	}
	
	public boolean isAtStart(){
		return Math.abs(distance - 0) < epsilon;
	}
	
	public Link getLink(){
		return link;
	}
	
	@Override
	public String toString() {
		double x, y;
		// calculate covered distance fraction
		double fraction = distance / link.getLength();
		// Calculate x coverage
		double xDistance = link.getTo().getX() - link.getFrom().getX();
		x = link.getFrom().getX() + xDistance*fraction;
		// Calculate y coverage
		double yDistance = link.getTo().getY() - link.getFrom().getY();
		y = link.getFrom().getY() + yDistance*fraction;
		
		return String.format("Position: link: %d distance: %.3f [%.3f, %.3f]", 
				link.getId(), distance, x, y);
	}
}

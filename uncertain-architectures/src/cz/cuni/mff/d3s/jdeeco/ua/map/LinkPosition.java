package cz.cuni.mff.d3s.jdeeco.ua.map;

import java.io.IOException;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.EnteredLinkRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.LeftLinkRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.LinkRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

public class LinkPosition {

	private Link link;
	
	private double distance;
	
	private double epsilon = 0.001; // 1 mm
	
	private final RuntimeLogger runtimeLogger;
	
	private final String robotId;
	
	public LinkPosition(Link link, String robotId, RuntimeLogger runtimeLogger){
		this.link = null;
		this.runtimeLogger = runtimeLogger;
		this.robotId = robotId;
		startFrom(link);
	}
	
	public void startFrom(Link link){
		if(runtimeLogger != null){
			try {
				// Log left link event if the previous link is present
				if(this.link != null){
					LinkRecord record = new LeftLinkRecord(robotId);
					record.setLink(this.link);
					record.setPerson(robotId);
					runtimeLogger.log(record);
				}
				
				// Log the entered link event
				LinkRecord record = new EnteredLinkRecord(robotId);
				record.setLink(link);
				record.setPerson(robotId);
				runtimeLogger.log(record);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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

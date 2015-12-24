/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.EnteredLinkRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.LeftLinkRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.LinkRecord;

public class LinkPosition implements Serializable{

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8458304837790584432L;

	private Link link;
	
	private double distance;
	
	private boolean linkLeft;
	
	public final static double POSITION_EPSILON = 0.001; // 1 mm
	
	private final String robotId;
	
	public LinkPosition(Link link, String robotId, RuntimeLogger runtimeLogger){
		if(link == null) throw new IllegalArgumentException(String.format(
				"The argument %s cannot be null.", "link"));
		if(robotId == null || robotId.isEmpty())
			throw new IllegalArgumentException(String.format(
				"The argument %s cannot be null nor empty.", "robotId"));
				
		this.link = link;
		this.robotId = robotId;
		distance = 0;
		linkLeft = false;

		// Log the entered link event
		LinkRecord record = new EnteredLinkRecord(robotId);
		record.setLink(link);
		record.setPerson(robotId);
		try {
			runtimeLogger.log(record);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startFrom(Link link){
		if(link == null) throw new IllegalArgumentException(String.format(
				"The argument %s cannot be null.", "link"));

		this.link = link;
		distance = 0;
		linkLeft = false;
		
		// Log the entered link event
		LinkRecord record = new EnteredLinkRecord(robotId);
		record.setLink(link);
		record.setPerson(robotId);
		try {
			ProcessContext.getRuntimeLogger().log(record);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void leave() {
		if(!isEndReached()){
			Log.e(String.format("Robot %s is leaving link %d before the end is reached.",
					robotId, link.getId()));
		}
		
		linkLeft = true;

		// Log left link event if the previous link is present
		LinkRecord record = new LeftLinkRecord(robotId);
		record.setLink(this.link);
		record.setPerson(robotId);
		try {
			ProcessContext.getRuntimeLogger().log(record);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public Node atNode(){
		if(link == null) throw new IllegalStateException(
				"Requested atNode() but no Link is set.");
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
		return Math.abs(distance - link.getLength()) < POSITION_EPSILON;
	}
		
	public boolean isLinkLeft(){
		return linkLeft;
	}
	
	public Link getLink(){
		return link;
	}
	
	@Override
	public String toString() {
		/*double x, y;
		// calculate covered distance fraction
		double fraction = distance / link.getLength();
		// Calculate x coverage
		double xDistance = link.getTo().getX() - link.getFrom().getX();
		x = link.getFrom().getX() + xDistance*fraction;
		// Calculate y coverage
		double yDistance = link.getTo().getY() - link.getFrom().getY();
		y = link.getFrom().getY() + yDistance*fraction;*/
		
		// [x, y] notation
		/*return String.format("From [%.3f, %.3f] to [%.3f, %.3f] distance %.3f\n",
				link.getFrom().getX(), link.getFrom().getY(),
				link.getTo().getX(), link.getTo().getY(), distance);*/
		// nodeId notation
		return String.format(Locale.ENGLISH, "From: %d to %d via %d distance: %.3f",
				link.getFrom().getId(), link.getTo().getId(), link.getId(),
				distance);
	}
}

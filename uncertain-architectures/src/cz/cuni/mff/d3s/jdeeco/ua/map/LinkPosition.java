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
	
	private double epsilon = 0.001; // 1 mm
	
	private final String robotId;
	
	public LinkPosition(Link link, String robotId){
		this.link = null;
		this.robotId = robotId;
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
		
		if(distance == 0){
			// Log the entered link event
			LinkRecord record = new EnteredLinkRecord(robotId);
			record.setLink(link);
			record.setPerson(robotId);
			record.setVehicle(robotId);
			try {
				ProcessContext.getRuntimeLogger().log(record);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		
		distance += length;
		if(distance > link.getLength()){
			distance = link.getLength();
		}
		
		if(Math.abs(distance - link.getLength()) < epsilon){
			// Log left link event if the previous link is present
			LinkRecord record = new LeftLinkRecord(robotId);
			record.setLink(this.link);
			record.setPerson(robotId);
			record.setVehicle(robotId);
			try {
				ProcessContext.getRuntimeLogger().log(record);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
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
		
		return String.format("[%.3f, %.3f]", x, y);
		/*return String.format("Position: link: %d distance: %.3f [%.3f, %.3f]", 
				link.getId(), distance, x, y);*/
	}
}

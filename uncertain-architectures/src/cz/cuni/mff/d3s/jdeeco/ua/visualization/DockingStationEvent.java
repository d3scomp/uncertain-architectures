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
package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import cz.filipekt.jdcv.events.Event;

/**
 * Models an event of type "dockingStation", which appears in the JDEECo
 * event log (runtimeData.xml)
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 *
 */
public class DockingStationEvent implements Event {

	/**
	 * A point in time when a docking station is deployed on a node 
	 */
	private final double time;

	/**
	 * ID of node on which a docking station is deployed
	 */
	private String node;
	
	public static String DOCKING_STATION_EVENT_TYPE = "dockingStation";

	public DockingStationEvent(double time) {
		this.time = time;
	}

	@Override
	public String getType() {
		return DOCKING_STATION_EVENT_TYPE;
	}

	@Override
	public double getTime() {
		return time;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
	
	@Override
	public String toString(){
		return "Docking station deployed at node " + node + " at time " + time;
	}
}

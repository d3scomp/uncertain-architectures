/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.component;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_CHECK_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_FAILURE_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_FAILURE_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_TO_FAIL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.FaultyKnowledgeReportingRole;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.role.DockRole;
import cz.cuni.mff.d3s.jdeeco.ua.visualization.DockingStationRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
 

@Component
@PlaysRole(DockRole.class)
@PlaysRole(FaultyKnowledgeReportingRole.class)
public class Dock {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;
	
	public List<String> robotsInLine;
	
	public Node position;
	
	public Set<String> faultyKnowledge;
	

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Dock(final String id, Node position, RuntimeLogger runtimeLogger) {
		this.id = id;
		this.position = position;
		robotsInLine = new ArrayList<>();
		faultyKnowledge = new HashSet<>();
		
		// Register the dock position in the dirtiness map
		DirtinessMap.placeDockingStation(position);
		
		try {
			// Log the event that the dock has been placed
			DockingStationRecord record = new DockingStationRecord(id);
			record.setNode(position);
			runtimeLogger.log(record);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Process
	@PeriodicScheduling(period = DOCK_CHECK_PERIOD)
	public static void checkDockingStation(@In("id") String dockId,
			@In("position") Node dockPosition,
			@InOut("faultyKnowledge") ParamHolder<Set<String>> faultyKnowledge) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		if (DOCK_FAILURE_ON) {
			if (DOCK_TO_FAIL.equals(dockId) && currentTime >= DOCK_FAILURE_TIME) {
				faultyKnowledge.value.add("position");
				DirtinessMap.setDockWorking(dockPosition, false);
			}
		}
	}

}

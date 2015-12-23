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
package cz.cuni.mff.d3s.jdeeco.ua.ensemble;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.CoordinatorRole;
import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.MemberRole;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.demo.DockData;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.role.DockRole;
import cz.cuni.mff.d3s.jdeeco.ua.role.DockableRole;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Dijkstra;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
@Ensemble
@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
@CoordinatorRole(DockRole.class)
@MemberRole(DockableRole.class)
public class DockingEnsemble {
	@Membership
	public static boolean membership() {
		return true;
	}
	
	@KnowledgeExchange
	public static void knowledgeExchange(
			@In("coord.id") String cId,
			@InOut("coord.robotsInLine") ParamHolder<List<String>> robotsInLine,
			@In("coord.position") Node dockPosition,
			@InOut("member.availableDocks") ParamHolder<Map<String, DockData>> availableDocks,
			@In("member.id") String mId,
			@In("member.position") CorrelationMetadataWrapper<LinkPosition> mPosition) {
		
		DockData dockData = new DockData();
		dockData.position = dockPosition;
		dockData.timestanp = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		availableDocks.value.put(cId, dockData);
	}
}

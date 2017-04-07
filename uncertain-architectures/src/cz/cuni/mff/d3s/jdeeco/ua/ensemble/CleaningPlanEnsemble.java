/*******************************************************************************
 * Copyright 2016 Charles University in Prague
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

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEAN_PLAN_EXCHANGE_PERIOD;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
@Ensemble(enableLogging=false)
@PeriodicScheduling(period = CLEAN_PLAN_EXCHANGE_PERIOD)
public class CleaningPlanEnsemble {
	@Membership
	public static boolean membership(@In("member.id") String mId, @In("coord.id") String cId) {
		return !mId.equals(cId);
	}
	
	@KnowledgeExchange
	public static void knowledgeExchange(
			@InOut("coord.othersPlans") ParamHolder<Map<String, Node>> othersPlans,
			@In("member.id") String mId,
			@In("member.trajectory") List<Link> trajectory) {
		if(trajectory.size() > 0){
			Node target = trajectory.get(trajectory.size()-1).getTo();
			othersPlans.value.put(mId, target);
		} else {
			othersPlans.value.remove(mId);
		}
	}
}

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
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;

/**
 * TODO: specify all the fields for data correlation here. These has to be wrapped
 */
@Ensemble
@PeriodicScheduling(period = 1000)
public class RobotDataAggregation {

	@Membership
	public static boolean membership(@In("member.id") String memberId,
			@In("member.batteryLevel") CorrelationMetadataWrapper<Double> batteryLevel,
			@In("member.position") CorrelationMetadataWrapper<LinkPosition> position,
			@In("coord.knowledgeHistoryOfAllComponents")
				Map<String, Map<String, List<CorrelationMetadataWrapper<?>>>>
					knowledgeHistoryOfAllComponents) {
		return true;
	}

	@KnowledgeExchange
	public static void map(@In("member.id") String memberId,
			@In("member.batteryLevel") CorrelationMetadataWrapper<Double> batteryLevel,
			@In("member.position") CorrelationMetadataWrapper<LinkPosition> position,
			@InOut("coord.knowledgeHistoryOfAllComponents")
				ParamHolder<Map<String, Map<String, List<CorrelationMetadataWrapper<?>>>>>
					knowledgeHistoryOfAllComponents)
				throws KnowledgeNotFoundException {

		System.out.println("KnowledgeExchange for component " + memberId);

		Map<String, List<CorrelationMetadataWrapper<?>>> memberKnowledgeHistory = 
				knowledgeHistoryOfAllComponents.value.get(memberId);
		if (memberKnowledgeHistory == null) {
			memberKnowledgeHistory = new HashMap<>();
			knowledgeHistoryOfAllComponents.value.put(memberId, memberKnowledgeHistory);
		}

		addFieldToHistory(memberKnowledgeHistory, "batteryLevel", batteryLevel);
		addFieldToHistory(memberKnowledgeHistory, "position", position);
	}

	/**
	 * Adds field to component field history.
	 * 
	 * @param histories
	 *            component field histories
	 * @param name
	 *            field name
	 * @param value
	 *            field value
	 */
	static private void addFieldToHistory(final Map<String, List<CorrelationMetadataWrapper<?>>> histories, final String name,
			final CorrelationMetadataWrapper<?> value) {
		List<CorrelationMetadataWrapper<?>> fieldHistory = histories.get(name);
		if (fieldHistory == null) {
			fieldHistory = new ArrayList<>();
			histories.put(name, fieldHistory);
		}
		fieldHistory.add(value);
	}
}
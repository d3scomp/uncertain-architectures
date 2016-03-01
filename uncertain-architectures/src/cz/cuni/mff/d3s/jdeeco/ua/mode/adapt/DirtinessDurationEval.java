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
package cz.cuni.mff.d3s.jdeeco.ua.mode.adapt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchFitnessEval;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchFitness;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessDurationEval implements NonDetModeSwitchFitnessEval {

	private Map<Node, Long> dirtInitTimes = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getKnowledgeNames()
	 */
	@Override
	public String[] getKnowledgeNames() {
		return new String[]{"map"};
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getEnergy(long, java.lang.Object[])
	 */
	@Override
	public NonDetModeSwitchFitness getFitness(long currentTime, Object[] knowledgeValues) {
		@SuppressWarnings("unchecked")
		CorrelationMetadataWrapper<DirtinessMap> map = (CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0];
		
		// Add dirt init times for dirt that is not yet present
		Set<Node> dirtyTiles = map.getValue().getDirtiness().keySet();
		for(Node n : dirtyTiles){
			if(!dirtInitTimes.keySet().contains(n)){
				dirtInitTimes.put(n,  currentTime);
			}
		}

		// Take dirt duration times for dirt that is cleaned
		List<Long> dirtDurations = new ArrayList<>();
		Set<Node> removeNodes = new HashSet<>();
		for(Node n : dirtInitTimes.keySet()){
			if(!dirtyTiles.contains(n)){
				dirtDurations.add(currentTime - dirtInitTimes.get(n));
				removeNodes.add(n);
			}
		}
		for(Node n : removeNodes){
			dirtInitTimes.remove(n);
		}
		
		return new DirtinessDurationFitness(dirtDurations);
	}

}

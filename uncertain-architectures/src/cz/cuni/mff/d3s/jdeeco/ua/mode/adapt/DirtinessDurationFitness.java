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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationUtility;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
import cz.cuni.mff.d3s.metaadaptation.correlation.CorrelationMetadataWrapper;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessDurationFitness extends AdaptationUtility {

	/**
	 * Initial times of uncompleted dirtiness events.
	 * Initial time is when the dirt appears. The dirtiness event
	 * is completed when the dirt is cleaned.
	 */
	private Map<Node, Long> dirtInitTimes = new HashMap<>();

	/**
	 * Summary of time durations of completed dirtiness events.
	 */
	private long durationsSum = 0;
	
	/**
	 * Number of completed dirtiness events.
	 */
	private int durationsCnt = 0;
	
	private final SimulationTimer timer;
	
	
	public DirtinessDurationFitness(SimulationTimer timer) {
		if(timer == null){
			throw new IllegalArgumentException(String.format("The %s argument is null.", "timer"));
		}
		
		this.timer = timer;
	}
	
	
	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getKnowledgeNames()
	 */
	@Override
	public String[] getKnowledgeNames() {
		return new String[]{"map", "id"};
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getEnergy(long, java.lang.Object[])
	 */
	@Override
	public double getUtility(Object[] knowledgeValues) {
		@SuppressWarnings("unchecked")
		CorrelationMetadataWrapper<DirtinessMap> map = (CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0];
		long currentTime = timer.getCurrentMilliseconds();
		
		System.out.println();
		System.out.println(knowledgeValues[1]);
		System.out.println(currentTime);
		System.out.println("Init Times");
		for(Node n : dirtInitTimes.keySet()){
			System.out.println(dirtInitTimes.get(n));
		}
		System.out.println("Dirt level");
		for(Node n : map.getValue().getDirtiness().keySet()){
			System.out.println(map.getValue().getDirtiness().get(n));
		}
		
		// Add dirt init times for dirt that is not yet present
		final Set<Node> dirtyTiles = map.getValue().getDirtiness().keySet();
		for(Node n : dirtyTiles){
			if(!dirtInitTimes.keySet().contains(n)){
				dirtInitTimes.put(n, currentTime);
			}
		}
		
		// Uncompleted events
		long ucDurationSum = 0;
		int ucDurationCnt = 0;
		
		// Take dirt duration times for dirt that is cleaned
		Set<Node> removeNodes = new HashSet<>();
		for(Node n : dirtInitTimes.keySet()){
			if(!dirtyTiles.contains(n)){
				durationsSum += (currentTime - dirtInitTimes.get(n));
				durationsCnt++;
				removeNodes.add(n);
			} else {
				ucDurationSum += (currentTime - dirtInitTimes.get(n));
				ucDurationCnt++;
			}
		}
		for(Node n : removeNodes){
			dirtInitTimes.remove(n);
		}
		
		// Compute the fitness
		if(ucDurationCnt == 0 && durationsCnt == 0){
			return 1; // Default value
		}

		System.out.println("Duration Sum: " + durationsSum);
		System.out.println("ucDuration Sum: " + ucDurationSum);
		System.out.println("Duration Cnt: " + durationsCnt);
		System.out.println("ucDuration Cnt: " + ucDurationCnt);
		
		double avg = (double) (durationsSum + ucDurationSum)
				/ (double) (durationsCnt + ucDurationCnt);
		
		// Normalize
		return avg / Configuration.SIMULATION_DURATION;
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchFitnessEval#restart()
	 */
//	@Override
//	public void restart() {
//		dirtInitTimes.clear();
//		durationsSum = 0;
//		durationsCnt = 0;
//	}


	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationUtility#getUtilityThreshold()
	 */
	@Override
	public double getUtilityThreshold() {
		return 0; // TODO:
	}

}

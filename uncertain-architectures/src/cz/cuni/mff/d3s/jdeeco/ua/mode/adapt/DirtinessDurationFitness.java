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

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchFitness;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessDurationFitness implements NonDetModeSwitchFitness {

	private long durationsSum;
	private int durationsCnt;
	
	public DirtinessDurationFitness(List<Long> dirtDurations) {
		durationsSum = 0;
		for(long duration : dirtDurations){
			durationsSum += duration;
		}
		durationsCnt = dirtDurations.size();
	}
	
	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance#combineEnergies(cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance)
	 */
	@Override
	public NonDetModeSwitchFitness combineFitness(NonDetModeSwitchFitness other) {
		DirtinessDurationFitness otherDurationsFit = (DirtinessDurationFitness)other;
		durationsSum += otherDurationsFit.durationsSum;
		durationsCnt += otherDurationsFit.durationsCnt;
		
		return this;
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance#getEnergy()
	 */
	@Override
	public double getFitness() {
		
		if(durationsCnt == 0){
			return 1;
		}
		
		double avg = (double) durationsSum / (double) durationsCnt;
		
		// Normalize
		return avg / Configuration.SIMULATION_DURATION;
	}

}

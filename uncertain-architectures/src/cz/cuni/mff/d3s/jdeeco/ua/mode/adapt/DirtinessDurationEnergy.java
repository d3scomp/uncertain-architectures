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

import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessDurationEnergy implements NonDetModeSwitchPerformance {

	private List<Long> dirtDurations;
	
	public DirtinessDurationEnergy(List<Long> dirtDurations) {
		this.dirtDurations = dirtDurations;
	}
	
	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance#combineEnergies(cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance)
	 */
	@Override
	public NonDetModeSwitchPerformance combineEnergies(NonDetModeSwitchPerformance other) {
		
		dirtDurations.addAll(((DirtinessDurationEnergy)other).dirtDurations);
		return this;
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchPerformance#getEnergy()
	 */
	@Override
	public double getEnergy() {
		long sum = 0;
		for(long l : dirtDurations){
			sum += l;
		}
		
		double avg = (double )sum / (double) dirtDurations.size();
		
		// Normalize
		return avg / Configuration.SIMULATION_DURATION;
	}

}

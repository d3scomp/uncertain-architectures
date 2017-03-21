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

import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.TimeProgressImpl;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;
import cz.cuni.mff.d3s.metaadaptation.search.SearchParameters;
import cz.cuni.mff.d3s.metaadaptation.search.annealing.Annealing;
import cz.cuni.mff.d3s.metaadaptation.search.annealing.LinearTemperature;
import cz.cuni.mff.d3s.metaadaptation.search.annealing.SimpleAcceptance;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class AnnealingParams extends SearchParameters {

	public static SimulationTimer timer;
	
	public AnnealingParams(){
		super();
		parameters.put(Annealing.TEMPERATURE_PARAMETER, new LinearTemperature(
				Configuration.NON_DET_START_TIME, Configuration.NON_DET_END_TIME, new TimeProgressImpl(timer)));
		parameters.put(Annealing.PROBABILITY_PARAMETER, new SimpleAcceptance());
		// parameters.put(Annealing.SEED_PARAMETER, 12345);
	}
}

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
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_GENERATION_PERIOD;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */

@Component
public class Environment {

	/** Mandatory id field. */
	public String id;
	

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Environment(final String id) {
		this.id = id;
	}
	
	@Process
	@PeriodicScheduling(period = DIRT_GENERATION_PERIOD)
	public static void generateDirt() {
		DirtinessMap.generateDirt();		
	}
}

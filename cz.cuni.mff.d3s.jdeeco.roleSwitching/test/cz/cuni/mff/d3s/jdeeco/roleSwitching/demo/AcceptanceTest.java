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
package cz.cuni.mff.d3s.jdeeco.roleSwitching.demo;

import org.junit.Test;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;

/** 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
public class AcceptanceTest {
	
	@Test
	public void test() throws InstantiationException, IllegalAccessException, DEECoException, AnnotationProcessorException {
			
			/* create main application container */
			SimulationTimer simulationTimer = new DiscreteEventTimer();
			DEECoSimulation realm = new DEECoSimulation(simulationTimer);
			
			/* create one and only deeco node (centralized deployment) */
			DEECoNode deeco = realm.createNode(0);
			
			/* deploy components and ensembles */
			deeco.deployComponent(new Car("Seat Ibiza", 2));
			deeco.deployComponent(new Car("Mazda", 1));
			deeco.deployComponent(new Bus("142", 42));
			deeco.deployComponent(new Building("Valdstejnsky palac", 150));
			deeco.deployComponent(new Person("Chuck", "Norris", 1));
			deeco.deployComponent(new Person("Milos", "Zeman", 2)); // Milos Zeman requires two seats
			deeco.deployEnsemble(BoardingEnsemble.class);
			
			realm.start(999);
	}
	
}

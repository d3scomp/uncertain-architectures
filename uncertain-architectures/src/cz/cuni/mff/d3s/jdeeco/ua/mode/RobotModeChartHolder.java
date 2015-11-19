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
package cz.cuni.mff.d3s.jdeeco.ua.mode;

import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.modes.TrueGuard;
import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;

public class RobotModeChartHolder extends ModeChartHolder {
	
	public RobotModeChartHolder(){
		ModeGuard<MetadataWrapper<Double>> batteryDrainedGuard = new ModeGuard<MetadataWrapper<Double>>() {
			@Override
			public boolean isSatisfied(MetadataWrapper<Double> knowledgeValue) {
				return knowledgeValue.getValue() < 0.2;
			}
			
			@Override
			public String getKnowledgeName() {
				return "batteryLevel";
			}
		};
		ModeGuard<Boolean> dockReachedGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDock";
			}
		};
		ModeGuard<MetadataWrapper<Double>> batteryChargedGuard = new ModeGuard<MetadataWrapper<Double>>() {
			@Override
			public boolean isSatisfied(MetadataWrapper<Double> knowledgeValue) {
				return knowledgeValue.getValue() > 0.95;
			}
			
			@Override
			public String getKnowledgeName() {
				return "batteryLevel";
			}
		};
		ModeGuard<Boolean> CleanGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDirt";
			}
		};
		ModeGuard<Boolean> SearchGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return !knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDirt";
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		factory.withTransitionWithGuard(CleanMode.class, SearchMode.class, SearchGuard);
		factory.withTransition(SearchMode.class, CleanMode.class, CleanGuard, 1);
		factory.withTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1);
		//factory.withTransition(SearchMode.class, DockingMode.class, new TrueGuard(), 0.1);
		factory.withTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
		factory.withTransitionWithGuard(ChargingMode.class, SearchMode.class, batteryChargedGuard);
		factory.withInitialMode(SearchMode.class);
		//currentMode = SearchMode.class;
		
		modeChart = factory.create();
	}
	
	/*@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		currentMode =  modeChart.switchMode(component, currentMode);
		return currentMode;
	}*/

}

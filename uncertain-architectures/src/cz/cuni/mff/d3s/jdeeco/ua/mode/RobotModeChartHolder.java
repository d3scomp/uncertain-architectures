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

import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

public class RobotModeChartHolder extends ModeChartHolder {

	@SuppressWarnings("unchecked")
	public RobotModeChartHolder(){
		final ModeGuard deadBatteryGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return ((MetadataWrapper<Double>)knowledgeValue[0]).getValue() <= 0;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		final ModeGuard batteryDrainedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return !deadBatteryGuard.isSatisfied(knowledgeValue)
						&& ((MetadataWrapper<Double>)knowledgeValue[0]).getValue() < 0.2;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		final ModeGuard dockReachedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) knowledgeValues[0];
				LinkPosition position = (LinkPosition) knowledgeValues[1];
				Node positionNode = position.atNode();
				return (positionNode != null
						&& map.getDockingStations().contains(positionNode));
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position"};
			}
		};
		final ModeGuard batteryChargedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return ((MetadataWrapper<Double>)knowledgeValue[0]).getValue() > 0.95;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		final ModeGuard cleanGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) knowledgeValues[0];
				LinkPosition position = (LinkPosition) knowledgeValues[1];
				Node positionNode = position.atNode();
				return (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& !deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& positionNode != null
						&& map.getDirtiness().keySet().contains(positionNode)
						&& map.getDirtiness().get(positionNode) > 0);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};
		final ModeGuard searchGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) knowledgeValues[0];
				LinkPosition position = (LinkPosition) knowledgeValues[1];
				Node positionNode = position.atNode();
				return !(positionNode != null
						&& map.getDirtiness().keySet().contains(positionNode)
						&& map.getDirtiness().get(positionNode) > 0);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position"};
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		factory.withTransitionWithGuard(CleanMode.class, SearchMode.class, searchGuard);
		factory.withTransition(SearchMode.class, CleanMode.class, cleanGuard, 1);
		factory.withTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1);
		factory.withTransitionWithGuard(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard);
		//factory.withTransition(SearchMode.class, DockingMode.class, new TrueGuard(), 0.1);
		factory.withTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
		factory.withTransitionWithGuard(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard);
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

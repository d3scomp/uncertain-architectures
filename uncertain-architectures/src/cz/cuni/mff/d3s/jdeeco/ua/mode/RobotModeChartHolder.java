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

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.SEARCH_TO_CHARGE_PROBABILITY;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.modes.ModeTransitionListener;
import cz.cuni.mff.d3s.deeco.modes.TrueGuard;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.demo.DockData;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

public class RobotModeChartHolder extends ModeChartHolder {


	@SuppressWarnings("unchecked")
	public RobotModeChartHolder(){
		
		final ModeGuard deadBatteryGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() <= 0;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final ModeGuard batteryDrainedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				boolean b = !deadBatteryGuard.isSatisfied(knowledgeValue)
						&& ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() < 0.25;
				return b;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final ModeGuard dockReachedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[0]).getValue();
				Map<String, DockData> availableDocks = (Map<String, DockData>) knowledgeValues[1];
				Node positionNode = position.atNode();
				if(positionNode != null){
					for(String dock : availableDocks.keySet()){
						if(positionNode.equals(availableDocks.get(dock).position)){
							return true;
						}
					}
				}
				return false;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"position", "availableDocks"};
			}
		};
		
		final ModeGuard batteryChargedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() > 0.95;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final ModeGuard cleanGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				return (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						/*&& !deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[2]})*/
						&& positionNode != null
						&& map.getDirtiness().keySet().contains(positionNode));
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};

		final ModeGuard approachGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				boolean b = (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& !deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& positionNode != null
						&& map.getDirtiness().keySet().size() > 5);
				return b;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};
		
		final ModeGuard searchGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				return map.getDirtiness().keySet().size() == 0;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position"};
			}
		};

		final ModeGuard keepCleaningGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				
				return (!(positionNode != null
						&& map.getDirtiness().containsKey(positionNode)
						&& map.getDirtiness().get(positionNode) > 0))
						&& map.getDirtiness().keySet().size() > 0;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position"};
			}
		};
		
		final ModeTransitionListener clearPlanEventListener = new ModeTransitionListener() {
			@Override
			public void transitionTaken(ParamHolder<?>[] knowledgeValues) {
				List<Link> trajectory = (List<Link>)knowledgeValues[0].value;
				trajectory.clear();
			}
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"trajectory"};
			}
		};
		
		final ModeGuard startWaitGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				List<Link> trajectory = (List<Link>) knowledgeValues[1];
				String id = (String) knowledgeValues[2];
				
				if(trajectory.size() == 0){
					return false;
				}
				
				Node nextPosition = trajectory.get(0).getTo();
				for(LinkPosition otherPosition : map.getOthersPosition(id)){
					if(nextPosition.equals(otherPosition.atNode())){
						return true;
					}
				}
				
				return false;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "trajectory", "id"};
			}
		};

		final ModeGuard stopWaitGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				return !startWaitGuard.isSatisfied(knowledgeValues);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "trajectory", "id"};
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		
		factory.addTransitionWithGuard(CleanMode.class, SearchMode.class, searchGuard);
		factory.addTransitionListener(CleanMode.class, SearchMode.class, new ModeTransitionLogger(CleanMode.class, SearchMode.class));
		
		factory.addTransitionWithGuard(CleanMode.class, DirtApproachMode.class, keepCleaningGuard);
		factory.addTransitionListener(CleanMode.class, DirtApproachMode.class, new ModeTransitionLogger(CleanMode.class, DirtApproachMode.class));
		
		factory.addTransition(SearchMode.class, DirtApproachMode.class, approachGuard, 1-SEARCH_TO_CHARGE_PROBABILITY);
		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, new ModeTransitionLogger(SearchMode.class, DirtApproachMode.class));
		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, clearPlanEventListener);
		
		factory.addTransition(DirtApproachMode.class, CleanMode.class, cleanGuard, 1);
		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, new ModeTransitionLogger(DirtApproachMode.class, CleanMode.class));
		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, clearPlanEventListener);

		factory.addTransition(DirtApproachMode.class, SearchMode.class, searchGuard, 1);
		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, new ModeTransitionLogger(DirtApproachMode.class, SearchMode.class));
		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, clearPlanEventListener);
		
		factory.addTransition(DirtApproachMode.class, DockingMode.class, batteryDrainedGuard, 1);
		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, new ModeTransitionLogger(DirtApproachMode.class, DockingMode.class));
		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, clearPlanEventListener);
		
		factory.addTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1-SEARCH_TO_CHARGE_PROBABILITY);
		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));
		factory.addTransitionListener(SearchMode.class, DockingMode.class, clearPlanEventListener);
		
		factory.addTransition(SearchMode.class, DockingMode.class, new TrueGuard(), SEARCH_TO_CHARGE_PROBABILITY);
		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));

		factory.addTransition(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard, 1-SEARCH_TO_CHARGE_PROBABILITY);
		factory.addTransitionListener(SearchMode.class, DeadBatteryMode.class, new ModeTransitionLogger(SearchMode.class, DeadBatteryMode.class));
		
		factory.addTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
		factory.addTransitionListener(DockingMode.class, ChargingMode.class, new ModeTransitionLogger(DockingMode.class, ChargingMode.class));
		factory.addTransitionListener(DockingMode.class, ChargingMode.class, clearPlanEventListener);
				
		factory.addTransitionWithGuard(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard);
		factory.addTransitionListener(DockingMode.class, DeadBatteryMode.class, new ModeTransitionLogger(DockingMode.class, DeadBatteryMode.class));

		factory.addTransitionWithGuard(DockingMode.class, WaitingMode.class, startWaitGuard);
		factory.addTransitionListener(DockingMode.class, WaitingMode.class, new ModeTransitionLogger(DockingMode.class, WaitingMode.class));

		factory.addTransitionWithGuard(WaitingMode.class, DockingMode.class, stopWaitGuard);
		factory.addTransitionListener(WaitingMode.class, DockingMode.class, new ModeTransitionLogger(WaitingMode.class, DockingMode.class));
		
		factory.addTransitionWithGuard(ChargingMode.class, SearchMode.class, batteryChargedGuard);
		factory.addTransitionListener(ChargingMode.class, SearchMode.class, new ModeTransitionLogger(ChargingMode.class, SearchMode.class));
		
		factory.addInitialMode(SearchMode.class);
				
		modeChart = factory.create();
	}
	
	/*@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		currentMode =  modeChart.switchMode(component, currentMode);
		return currentMode;
	}*/

}

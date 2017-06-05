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

import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.modes.DEECoMode;
import cz.cuni.mff.d3s.deeco.modes.DEECoModeGuard;
import cz.cuni.mff.d3s.deeco.modes.DEECoTransitionListener;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.jdeeco.modes.Transition;
import cz.cuni.mff.d3s.jdeeco.modes.runtimelog.ModeTransitionLogger;
import cz.cuni.mff.d3s.jdeeco.ua.demo.DockData;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
import cz.cuni.mff.d3s.metaadaptation.correlation.CorrelationMetadataWrapper;

public class RobotModeChartHolder extends ModeChartHolder {


	@SuppressWarnings("unchecked")
	public RobotModeChartHolder(){
		
		// GUARDS #############################################################
		
		final DEECoModeGuard deadBatteryGuard = new DEECoModeGuard() {
			
			@Override
			protected void specifyParameters(){}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				return ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() <= 0;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final DEECoModeGuard batteryDrainedGuard = new DEECoModeGuard() {

			private static final String DRAINED_LEVEL = "DRAINED_LEVEL";
			private static final double INIT_DRAINED_LEVEL = 0.2;
			
			@Override
			protected void specifyParameters(){
				parameters.put(DRAINED_LEVEL, INIT_DRAINED_LEVEL);
			}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				boolean batteryDead = deadBatteryGuard.isSatisfied(knowledgeValue);
				boolean batteryLow = 
						((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue()
						< parameters.get(DRAINED_LEVEL);
				return !batteryDead && batteryLow;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final DEECoModeGuard dockReachedGuard = new DEECoModeGuard() {

			@Override
			protected void specifyParameters(){}
			
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
		
		final DEECoModeGuard batteryChargedGuard = new DEECoModeGuard() {

			private static final String CHARGED_LEVEL = "CHARGED_LEVEL";
			private static final double INIT_CHARGED_LEVEL = 0.95;
			
			@Override
			protected void specifyParameters(){
				parameters.put(CHARGED_LEVEL, INIT_CHARGED_LEVEL);
			}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				boolean batteryCharged = ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue()
						> parameters.get(CHARGED_LEVEL);
				return batteryCharged;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final DEECoModeGuard cleanGuard = new DEECoModeGuard() {
			
			@Override
			protected void specifyParameters(){}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				return (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& positionNode != null
						&& map.getDirtiness().keySet().contains(positionNode));
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};

		final DEECoModeGuard approachGuard = new DEECoModeGuard() {

			private static final String FOUND_ENOUGH = "FOUND_ENOUGH";
			private static final double FOUND_ENOUGH_VALUE = 5;
			
			@Override
			protected void specifyParameters(){
				// TODO: it is possible to configure the map dirtiness size parameter - but do id smart - how much percent of the local map is dirty
				parameters.put(FOUND_ENOUGH, FOUND_ENOUGH_VALUE);
			}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				boolean b = (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& !deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& positionNode != null
						&& map.getDirtiness().keySet().size() > parameters.get(FOUND_ENOUGH));
				return b;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};
		
		final DEECoModeGuard searchGuard = new DEECoModeGuard() {

			private static final String CLEANED_ENOUGH = "CLEANED_ENOUGH";
			private static final double CLEANED_ENOUGH_VALUE = 0;
			
			@Override
			protected void specifyParameters(){
				// TODO: it is possible to configure the map dirtiness size to start searching - do it as above
				parameters.put(CLEANED_ENOUGH, CLEANED_ENOUGH_VALUE);
			}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				
				boolean lowPower = batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[1]});
				boolean moreDirt = map.getDirtiness().keySet().size() > parameters.get(CLEANED_ENOUGH);
				return !lowPower && !moreDirt;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "batteryLevel"};
			}
		};

		final DEECoModeGuard keepCleaningGuard = new DEECoModeGuard() {

			private static final String CLEANED_ENOUGH = "CLEANED_ENOUGH";
			private static final double CLEANED_ENOUGH_VALUE = 0;
			
			@Override
			protected void specifyParameters(){
				// TODO: it is possible to configure the map dirtiness size to continue cleaning - do it as above
				parameters.put(CLEANED_ENOUGH, CLEANED_ENOUGH_VALUE);
			}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
				Node positionNode = position.atNode();
				
				boolean lowPower = batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]});
				boolean atDirt = positionNode != null
						&& map.getDirtiness().containsKey(positionNode)
						&& map.getDirtiness().get(positionNode) > 0;
				boolean moreDirt = map.getDirtiness().keySet().size() > parameters.get(CLEANED_ENOUGH);
				
				return !lowPower && !atDirt && moreDirt;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "position", "batteryLevel"};
			}
		};
		
		final DEECoTransitionListener clearPlanEventListener = new DEECoTransitionListener() {
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
		
		final DEECoModeGuard startWaitGuard = new DEECoModeGuard() {
			
			@Override
			protected void specifyParameters(){}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				if (deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[3]})) {
					return false;
				}
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
				return new String[] {"map", "trajectory", "id", "batteryLevel"};
			}
		};

		final DEECoModeGuard stopWaitGuard = new DEECoModeGuard() {

			@Override
			protected void specifyParameters(){}
			
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				return !startWaitGuard.isSatisfied(knowledgeValues);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "trajectory", "id", "batteryLevel"};
			}
		};
		
		// MODES ##############################################################
		
		DEECoMode cleanMode = new CleanMode();
		DEECoMode searchMode = new SearchMode();
		DEECoMode dirtApproachMode = new DirtApproachMode();
		DEECoMode dockingMode = new DockingMode();
		DEECoMode deadBatteryMode = new DeadBatteryMode();
		DEECoMode chargingMode = new ChargingMode();
		DEECoMode waitingMode = new WaitingMode();
		
		// TRANSITIONS ########################################################
		
		Transition transition;
		transition = addTransition(cleanMode, searchMode, searchGuard);
		addTransitionListener(transition, new ModeTransitionLogger(cleanMode, searchMode));
		
		transition = addTransition(cleanMode, dirtApproachMode, keepCleaningGuard);
		addTransitionListener(transition, new ModeTransitionLogger(cleanMode, dirtApproachMode));

		transition = addTransition(cleanMode, dockingMode, batteryDrainedGuard);
		addTransitionListener(transition, new ModeTransitionLogger(cleanMode, dockingMode));
		
		transition = addTransition(searchMode, dirtApproachMode, approachGuard);
		addTransitionListener(transition, new ModeTransitionLogger(searchMode, dirtApproachMode));
		addTransitionListener(transition, clearPlanEventListener);
		
		transition = addTransition(dirtApproachMode, cleanMode, cleanGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dirtApproachMode, cleanMode));
		addTransitionListener(transition, clearPlanEventListener);

		transition = addTransition(dirtApproachMode, searchMode, searchGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dirtApproachMode, searchMode));
		addTransitionListener(transition, clearPlanEventListener);
		
		transition = addTransition(dirtApproachMode, dockingMode, batteryDrainedGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dirtApproachMode, dockingMode));
		addTransitionListener(transition, clearPlanEventListener);
		
		transition = addTransition(searchMode, dockingMode, batteryDrainedGuard);
		addTransitionListener(transition, new ModeTransitionLogger(searchMode, dockingMode));
		addTransitionListener(transition, clearPlanEventListener);
		
		transition = addTransition(searchMode, deadBatteryMode, deadBatteryGuard);
		addTransitionListener(transition, new ModeTransitionLogger(searchMode, deadBatteryMode));
		
		transition = addTransition(dockingMode, chargingMode, dockReachedGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dockingMode, chargingMode));
		addTransitionListener(transition, clearPlanEventListener);
				
		transition = addTransition(dockingMode, deadBatteryMode, deadBatteryGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dockingMode, deadBatteryMode));

		transition = addTransition(dockingMode, waitingMode, startWaitGuard);
		addTransitionListener(transition, new ModeTransitionLogger(dockingMode, waitingMode));

		transition = addTransition(waitingMode, dockingMode, stopWaitGuard);
		addTransitionListener(transition, new ModeTransitionLogger(waitingMode, dockingMode));
		
		transition = addTransition(chargingMode, searchMode, batteryChargedGuard);
		addTransitionListener(transition, new ModeTransitionLogger(chargingMode, searchMode));	
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.modes.ModeChartHolder#getInitialMode()
	 */
	@Override
	public DEECoMode getInitialMode() {
		return new SearchMode();
	}

}

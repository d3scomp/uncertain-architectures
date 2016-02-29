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

import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.modes.ModeTransitionListener;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.jdeeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.jdeeco.modes.runtimelog.ModeTransitionLogger;
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
						&& ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() < 0.2;
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
		
		final ModeGuard batteryChargedOrChargingInUnavailableDockGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValue) {
				boolean batteryCharged = ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() > 0.95;
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValue[1]).getValue();
				Map<String, DockData> availableDocks = (Map<String, DockData>) knowledgeValue[2];
				boolean dockNotInAvailableOnes = true;
				for (DockData dockData : availableDocks.values()) {
					if ((dockData.position).equals(position.atNode())) {
						dockNotInAvailableOnes = false;
					}
				}
				return batteryCharged || dockNotInAvailableOnes;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel", "position", "availableDocks"};
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
				return (!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[1]})
						&& map.getDirtiness().keySet().size() == 0);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "batteryLevel"};
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

		final ModeGuard stopWaitGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				return !startWaitGuard.isSatisfied(knowledgeValues);
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"map", "trajectory", "id", "batteryLevel"};
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		
		/* ----------------------------------------- */
		/* ---       1st alternative          ------ */
		/* ----------------------------------------- */

//		factory.addTransitionWithGuard(CleanMode.class, SearchMode.class, searchGuard);
//		factory.addTransitionListener(CleanMode.class, SearchMode.class, new ModeTransitionLogger(CleanMode.class, SearchMode.class));
//		
//		factory.addTransitionWithGuard(CleanMode.class, DirtApproachMode.class, keepCleaningGuard);
//		factory.addTransitionListener(CleanMode.class, DirtApproachMode.class, new ModeTransitionLogger(CleanMode.class, DirtApproachMode.class));
//		
//		factory.addTransition(SearchMode.class, DirtApproachMode.class, approachGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, new ModeTransitionLogger(SearchMode.class, DirtApproachMode.class));
//		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, clearPlanEventListener);
//		
//		factory.addTransition(DirtApproachMode.class, CleanMode.class, cleanGuard, 1);
//		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, new ModeTransitionLogger(DirtApproachMode.class, CleanMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, clearPlanEventListener);
//
//		factory.addTransition(DirtApproachMode.class, SearchMode.class, searchGuard, 1);
//		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, new ModeTransitionLogger(DirtApproachMode.class, SearchMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, clearPlanEventListener);
//		
//		factory.addTransition(DirtApproachMode.class, DockingMode.class, batteryDrainedGuard, 1);
//		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, new ModeTransitionLogger(DirtApproachMode.class, DockingMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, clearPlanEventListener);
//		
//		factory.addTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, clearPlanEventListener);
//		
//		factory.addTransition(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DeadBatteryMode.class, new ModeTransitionLogger(SearchMode.class, DeadBatteryMode.class));
//		
//		factory.addTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
//		factory.addTransitionListener(DockingMode.class, ChargingMode.class, new ModeTransitionLogger(DockingMode.class, ChargingMode.class));
//		factory.addTransitionListener(DockingMode.class, ChargingMode.class, clearPlanEventListener);
//				
//		factory.addTransitionWithGuard(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard);
//		factory.addTransitionListener(DockingMode.class, DeadBatteryMode.class, new ModeTransitionLogger(DockingMode.class, DeadBatteryMode.class));
//
//		factory.addTransitionWithGuard(DockingMode.class, WaitingMode.class, startWaitGuard);
//		factory.addTransitionListener(DockingMode.class, WaitingMode.class, new ModeTransitionLogger(DockingMode.class, WaitingMode.class));
//
//		factory.addTransitionWithGuard(WaitingMode.class, DockingMode.class, stopWaitGuard);
//		factory.addTransitionListener(WaitingMode.class, DockingMode.class, new ModeTransitionLogger(WaitingMode.class, DockingMode.class));
//		
//		factory.addTransitionWithGuard(ChargingMode.class, SearchMode.class, batteryChargedOrChargingInUnavailableDockGuard);
//		factory.addTransitionListener(ChargingMode.class, SearchMode.class, new ModeTransitionLogger(ChargingMode.class, SearchMode.class));
//		
//		factory.addTransition(SearchMode.class, DockingMode.class, new TrueGuard(), PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));

		/* ----------------------------------------- */
		/* ---       2nd alternative          ------ */
		/* ----------------------------------------- */
		
//		factory.addTransition(CleanMode.class, SearchMode.class, searchGuard, 1-PROBABILITY);
//		factory.addTransitionListener(CleanMode.class, SearchMode.class, new ModeTransitionLogger(CleanMode.class, SearchMode.class));
//		
//		factory.addTransition(CleanMode.class, DirtApproachMode.class, keepCleaningGuard, 1-PROBABILITY);
//		factory.addTransitionListener(CleanMode.class, DirtApproachMode.class, new ModeTransitionLogger(CleanMode.class, DirtApproachMode.class));
//		
//		factory.addTransition(SearchMode.class, DirtApproachMode.class, approachGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, new ModeTransitionLogger(SearchMode.class, DirtApproachMode.class));
//		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, clearPlanEventListener);
//		
//		factory.addTransition(DirtApproachMode.class, CleanMode.class, cleanGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, new ModeTransitionLogger(DirtApproachMode.class, CleanMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, clearPlanEventListener);
//
//		factory.addTransition(DirtApproachMode.class, SearchMode.class, searchGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, new ModeTransitionLogger(DirtApproachMode.class, SearchMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, clearPlanEventListener);
//		
//		factory.addTransition(DirtApproachMode.class, DockingMode.class, batteryDrainedGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, new ModeTransitionLogger(DirtApproachMode.class, DockingMode.class));
//		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, clearPlanEventListener);
//		
//		factory.addTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, clearPlanEventListener);
//		
//		factory.addTransition(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard, 1-PROBABILITY);
//		factory.addTransitionListener(SearchMode.class, DeadBatteryMode.class, new ModeTransitionLogger(SearchMode.class, DeadBatteryMode.class));
//		
//		factory.addTransition(DockingMode.class, ChargingMode.class, dockReachedGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DockingMode.class, ChargingMode.class, new ModeTransitionLogger(DockingMode.class, ChargingMode.class));
//		factory.addTransitionListener(DockingMode.class, ChargingMode.class, clearPlanEventListener);
//				
//		factory.addTransition(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DockingMode.class, DeadBatteryMode.class, new ModeTransitionLogger(DockingMode.class, DeadBatteryMode.class));
//
//		factory.addTransition(DockingMode.class, WaitingMode.class, startWaitGuard, 1-PROBABILITY);
//		factory.addTransitionListener(DockingMode.class, WaitingMode.class, new ModeTransitionLogger(DockingMode.class, WaitingMode.class));
//
//		factory.addTransition(WaitingMode.class, DockingMode.class, stopWaitGuard, 1-PROBABILITY);
//		factory.addTransitionListener(WaitingMode.class, DockingMode.class, new ModeTransitionLogger(WaitingMode.class, DockingMode.class));
//		
//		factory.addTransition(ChargingMode.class, SearchMode.class, batteryChargedOrChargingInUnavailableDockGuard,1-PROBABILITY);
//		factory.addTransitionListener(ChargingMode.class, SearchMode.class, new ModeTransitionLogger(ChargingMode.class, SearchMode.class));
//		
//		/* ------------ SearchMode ------------------*/
//		factory.addTransition(SearchMode.class, DirtApproachMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, new ModeTransitionLogger(SearchMode.class, DirtApproachMode.class));
//
//		factory.addTransition(SearchMode.class, CleanMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(SearchMode.class, CleanMode.class, new ModeTransitionLogger(SearchMode.class, CleanMode.class));
//
//		factory.addTransition(SearchMode.class, DockingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));
//
//		factory.addTransition(SearchMode.class, ChargingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(SearchMode.class, ChargingMode.class, new ModeTransitionLogger(SearchMode.class, ChargingMode.class));
//
//		/* ------------ DirtApproachMode ------------------*/
//		factory.addTransition(DirtApproachMode.class, SearchMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DirtApproachMode.class, SearchMode.class, new ModeTransitionLogger(DirtApproachMode.class, SearchMode.class));
//		
//		factory.addTransition(DirtApproachMode.class, DockingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DirtApproachMode.class, DockingMode.class, new ModeTransitionLogger(DirtApproachMode.class, DockingMode.class));
//
//		factory.addTransition(DirtApproachMode.class, CleanMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, new ModeTransitionLogger(DirtApproachMode.class, CleanMode.class));
//
//		factory.addTransition(DirtApproachMode.class, ChargingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DirtApproachMode.class, ChargingMode.class, new ModeTransitionLogger(DirtApproachMode.class, ChargingMode.class));
//		
//		/* ------------ CleanMode ------------------*/
//		factory.addTransition(CleanMode.class, SearchMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(CleanMode.class, SearchMode.class, new ModeTransitionLogger(CleanMode.class, SearchMode.class));
//		
//		factory.addTransition(CleanMode.class, DockingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(CleanMode.class, DockingMode.class, new ModeTransitionLogger(CleanMode.class, DockingMode.class));
//
//		factory.addTransition(CleanMode.class, DirtApproachMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(CleanMode.class, DirtApproachMode.class, new ModeTransitionLogger(CleanMode.class, DirtApproachMode.class));
//
//		factory.addTransition(CleanMode.class, ChargingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(CleanMode.class, ChargingMode.class, new ModeTransitionLogger(CleanMode.class, ChargingMode.class));
//		
//		/* ------------ DockingMode ------------------*/
//		factory.addTransition(DockingMode.class, SearchMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DockingMode.class, SearchMode.class, new ModeTransitionLogger(DockingMode.class, SearchMode.class));
//		
//		factory.addTransition(DockingMode.class, DirtApproachMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DockingMode.class, DirtApproachMode.class, new ModeTransitionLogger(DockingMode.class, DirtApproachMode.class));
//
//		factory.addTransition(DockingMode.class, CleanMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DockingMode.class, CleanMode.class, new ModeTransitionLogger(DockingMode.class, CleanMode.class));
//
//		factory.addTransition(DockingMode.class, ChargingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(DockingMode.class, ChargingMode.class, new ModeTransitionLogger(DockingMode.class, ChargingMode.class));
//
//		/* ------------ ChargingMode ------------------*/
//		factory.addTransition(ChargingMode.class, SearchMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(ChargingMode.class, SearchMode.class, new ModeTransitionLogger(ChargingMode.class, SearchMode.class));
//		
//		factory.addTransition(ChargingMode.class, DockingMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(ChargingMode.class, DockingMode.class, new ModeTransitionLogger(ChargingMode.class, DockingMode.class));
//
//		factory.addTransition(ChargingMode.class, DirtApproachMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(ChargingMode.class, DirtApproachMode.class, new ModeTransitionLogger(ChargingMode.class, DirtApproachMode.class));
//
//		factory.addTransition(ChargingMode.class, CleanMode.class, new TrueGuard(), PROBABILITY/4);
//		factory.addTransitionListener(ChargingMode.class, CleanMode.class, new ModeTransitionLogger(ChargingMode.class, CleanMode.class));
//		
//		/* ------------ WaitingMode ------------------*/
//		factory.addTransition(WaitingMode.class, SearchMode.class, new TrueGuard(), PROBABILITY/5);
//		factory.addTransitionListener(WaitingMode.class, SearchMode.class, new ModeTransitionLogger(WaitingMode.class, SearchMode.class));
//		
//		factory.addTransition(WaitingMode.class, DockingMode.class, new TrueGuard(), PROBABILITY/5);
//		factory.addTransitionListener(WaitingMode.class, DockingMode.class, new ModeTransitionLogger(WaitingMode.class, DockingMode.class));
//
//		factory.addTransition(WaitingMode.class, DirtApproachMode.class, new TrueGuard(), PROBABILITY/5);
//		factory.addTransitionListener(WaitingMode.class, DirtApproachMode.class, new ModeTransitionLogger(WaitingMode.class, DirtApproachMode.class));
//
//		factory.addTransition(WaitingMode.class, CleanMode.class, new TrueGuard(), PROBABILITY/5);
//		factory.addTransitionListener(WaitingMode.class, CleanMode.class, new ModeTransitionLogger(WaitingMode.class, CleanMode.class));
//		
//		factory.addTransition(WaitingMode.class, ChargingMode.class, new TrueGuard(), PROBABILITY/5);
//		factory.addTransitionListener(WaitingMode.class, ChargingMode.class, new ModeTransitionLogger(WaitingMode.class, ChargingMode.class));
		
		/* ----------------------------------------- */
		/* ---       3rd alternative          ------ */
		/* ----------------------------------------- */
		
		factory.addTransition(CleanMode.class, SearchMode.class, searchGuard, 1);
		factory.addTransitionListener(CleanMode.class, SearchMode.class, new ModeTransitionLogger(CleanMode.class, SearchMode.class));
		
		factory.addTransition(CleanMode.class, DirtApproachMode.class, keepCleaningGuard, 1);
		factory.addTransitionListener(CleanMode.class, DirtApproachMode.class, new ModeTransitionLogger(CleanMode.class, DirtApproachMode.class));
		
		factory.addTransition(SearchMode.class, DirtApproachMode.class, approachGuard, 1);
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
		
		factory.addTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1);
		factory.addTransitionListener(SearchMode.class, DockingMode.class, new ModeTransitionLogger(SearchMode.class, DockingMode.class));
		factory.addTransitionListener(SearchMode.class, DockingMode.class, clearPlanEventListener);
		
		factory.addTransition(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard, 1);
		factory.addTransitionListener(SearchMode.class, DeadBatteryMode.class, new ModeTransitionLogger(SearchMode.class, DeadBatteryMode.class));
		
		factory.addTransition(DockingMode.class, ChargingMode.class, dockReachedGuard, 1);
		factory.addTransitionListener(DockingMode.class, ChargingMode.class, new ModeTransitionLogger(DockingMode.class, ChargingMode.class));
		factory.addTransitionListener(DockingMode.class, ChargingMode.class, clearPlanEventListener);
				
		factory.addTransition(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard, 1);
		factory.addTransitionListener(DockingMode.class, DeadBatteryMode.class, new ModeTransitionLogger(DockingMode.class, DeadBatteryMode.class));

		factory.addTransition(DockingMode.class, WaitingMode.class, startWaitGuard, 1);
		factory.addTransitionListener(DockingMode.class, WaitingMode.class, new ModeTransitionLogger(DockingMode.class, WaitingMode.class));

		factory.addTransition(WaitingMode.class, DockingMode.class, stopWaitGuard, 1);
		factory.addTransitionListener(WaitingMode.class, DockingMode.class, new ModeTransitionLogger(WaitingMode.class, DockingMode.class));
		
		factory.addTransition(ChargingMode.class, SearchMode.class, batteryChargedOrChargingInUnavailableDockGuard,1);
		factory.addTransitionListener(ChargingMode.class, SearchMode.class, new ModeTransitionLogger(ChargingMode.class, SearchMode.class));
				
		/* ----------------------------------------- */
		/* ----------------------------------------- */
		
		factory.addInitialMode(SearchMode.class);
				
		modeChart = factory.create();
	}
	
	/*@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		currentMode =  modeChart.switchMode(component, currentMode);
		return currentMode;
	}*/

}

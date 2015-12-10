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

import java.io.IOException;
import java.util.List;

import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.modes.ModeTransitionListener;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.EnteredVehicleRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.LeftVehicleRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.records.VehicleRecord;

public class RobotModeChartHolder extends ModeChartHolder {

	private final boolean enableTransitionActions = false;

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
				return !deadBatteryGuard.isSatisfied(knowledgeValue)
						&& ((CorrelationMetadataWrapper<Double>)knowledgeValue[0]).getValue() < 0.2;
			}
			
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"batteryLevel"};
			}
		};
		
		final ModeGuard dockReachedGuard = new ModeGuard() {
			@Override
			public boolean isSatisfied(Object[] knowledgeValues) {
				DirtinessMap map = (DirtinessMap) ((CorrelationMetadataWrapper<DirtinessMap>) knowledgeValues[0]).getValue();
				LinkPosition position = ((CorrelationMetadataWrapper<LinkPosition>) knowledgeValues[1]).getValue();
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
				return (/*!batteryDrainedGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& !deadBatteryGuard.isSatisfied(new Object[]{knowledgeValues[2]})
						&& */positionNode != null
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
		
		final ModeTransitionListener waitEventListener = new ModeTransitionListener() {
			@Override
			public void transitionTaken(ParamHolder<?>[] knowledgeValues) {
				String id = (String)knowledgeValues[0].value;
				
				VehicleRecord record = new LeftVehicleRecord(id);
				record.setVehicle(id);
				record.setPerson(id);
				try {
					ProcessContext.getRuntimeLogger().log(record);
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"id"};
			}
		};
		
		final ModeTransitionListener goEventListener = new ModeTransitionListener() {
			@Override
			public void transitionTaken(ParamHolder<?>[] knowledgeValues) {
				String id = (String)knowledgeValues[0].value;
				
				VehicleRecord record = new EnteredVehicleRecord(id);
				record.setVehicle(id);
				record.setPerson(id);
				try {
					ProcessContext.getRuntimeLogger().log(record);
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public String[] getKnowledgeNames() {
				return new String[] {"id"};
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		factory.addTransitionWithGuard(CleanMode.class, SearchMode.class, searchGuard);
		factory.addTransitionWithGuard(CleanMode.class, DirtApproachMode.class, keepCleaningGuard);
		
		factory.addTransition(SearchMode.class, DirtApproachMode.class, approachGuard, 1);
		factory.addTransitionListener(SearchMode.class, DirtApproachMode.class, clearPlanEventListener);
		
		factory.addTransition(DirtApproachMode.class, CleanMode.class, cleanGuard, 1);
		factory.addTransitionListener(DirtApproachMode.class, CleanMode.class, clearPlanEventListener);
		
		factory.addTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 1);
		factory.addTransitionListener(SearchMode.class, DockingMode.class, clearPlanEventListener);
		
		factory.addTransitionWithGuard(SearchMode.class, DeadBatteryMode.class, deadBatteryGuard);
		
		//factory.withTransition(SearchMode.class, DockingMode.class, new TrueGuard(), 0.1);
		
		factory.addTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
		factory.addTransitionListener(DockingMode.class, ChargingMode.class, clearPlanEventListener);
		
		factory.addTransitionWithGuard(DockingMode.class, DeadBatteryMode.class, deadBatteryGuard);
		
		factory.addTransitionWithGuard(ChargingMode.class, SearchMode.class, batteryChargedGuard);
		
		factory.addInitialMode(SearchMode.class);
		
		if(enableTransitionActions){
			factory.addTransitionListener(CleanMode.class, SearchMode.class, goEventListener);
			factory.addTransitionListener(SearchMode.class, CleanMode.class, waitEventListener);
			factory.addTransitionListener(SearchMode.class, DeadBatteryMode.class, waitEventListener);
			factory.addTransitionListener(DockingMode.class, ChargingMode.class, waitEventListener);
			factory.addTransitionListener(DockingMode.class, DeadBatteryMode.class, waitEventListener);
			factory.addTransitionListener(ChargingMode.class, SearchMode.class, goEventListener);
		}
		
		modeChart = factory.create();
	}
	
	/*@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		currentMode =  modeChart.switchMode(component, currentMode);
		return currentMode;
	}*/

}

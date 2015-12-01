/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.BATTERY_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CHARGING_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEANING_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVEMENT_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.STATUS_PROCESS_PERIOD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.ComponentModeChart;
import cz.cuni.mff.d3s.deeco.annotations.CorrelationData;
import cz.cuni.mff.d3s.deeco.annotations.ExcludeModes;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Mode;
import cz.cuni.mff.d3s.deeco.annotations.Modes;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionMetric;
import cz.cuni.mff.d3s.jdeeco.ua.mode.ChargingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.CleanMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.DockingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.RobotModeChartHolder;
import cz.cuni.mff.d3s.jdeeco.ua.mode.SearchMode;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
 

@Component
@ComponentModeChart(RobotModeChartHolder.class)
public class Robot {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;
	
	/** Battery level. */
	public CorrelationMetadataWrapper<Double> batteryLevel;
			
	@Local
	public final DirtinessMap map;
	
	@Local
	@CorrelationData(metric=PositionMetric.class,boundary=4,confidence=0.9)
	public CorrelationMetadataWrapper<LinkPosition> position;

	@Local
	public final List<Link> trajectory;

	@Local
	public SearchTrajectoryPlanner searchPlanner;
	
	@Local
	public NearestTrajectoryPlanner targetPlanner;
	
	@Local
	public TrajectoryExecutor mover;
	
	@Local
	public PositionFilter positionInaccuracy;
	
	@Local
	public DoubleFilter batteryInaccuracy;
	

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Robot(final String id) {
		this.id = id;
		map = new DirtinessMap(id);
		trajectory = new ArrayList<>();
	}

	///////////////////////////////////////////////////////////////////////////
	// Handle Battery /////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	@Process
	@Modes({SearchMode.class, DockingMode.class})
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void consumeBatterySearch(
			@InOut("batteryLevel") ParamHolder<CorrelationMetadataWrapper<Double>> batteryLevel
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		double delta = MOVEMENT_ENERGY_COST * (double) BATTERY_PROCESS_PERIOD / 1000;
		batteryLevel.value.setValue(Math.max(0, batteryLevel.value.getValue() - delta), currentTime);
		// TODO: use battery noise - use actual and believed battery value
	}
	
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void consumeBatteryClean(
			@InOut("batteryLevel") ParamHolder<CorrelationMetadataWrapper<Double>> batteryLevel
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		double delta = CLEANING_ENERGY_COST * (double) BATTERY_PROCESS_PERIOD / 1000;
		batteryLevel.value.setValue(Math.max(0, batteryLevel.value.getValue() - delta), currentTime);
		// TODO: use battery noise - use actual and believed battery value
	}

	@Process
	@Mode(ChargingMode.class)
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void consumeBatteryCharge(
			@InOut("batteryLevel") ParamHolder<CorrelationMetadataWrapper<Double>> batteryLevel
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		double delta = CHARGING_RATE * (double) BATTERY_PROCESS_PERIOD / 1000;
		batteryLevel.value.setValue(Math.min(1, batteryLevel.value.getValue() + delta), currentTime);
		// TODO: use battery noise - use actual and believed battery value
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	

	///////////////////////////////////////////////////////////////////////////
	// Handle Movement ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	@Process
	@ExcludeModes({ChargingMode.class, CleanMode.class})
	@PeriodicScheduling(period = MOVE_PROCESS_PERIOD)
	public static void move(@In("mover") TrajectoryExecutor mover,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@InOut("position") ParamHolder<CorrelationMetadataWrapper<LinkPosition>> position,
			@InOut("map") ParamHolder<DirtinessMap> map) {
		// Move
		mover.move(trajectory.value, position.value.getValue());
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		position.value.setValue(position.value.getValue(), currentTime);

		// Check the tile for dirt
		Node node = position.value.getValue().atNode();
		if(node != null){
			map.value.getVisitedNodes().put(node, currentTime);
			double intensity = map.value.checkDirtiness(node);
			if(intensity > 0){
				System.out.format("\nDirtiness %f detected at %d\n\n",
						intensity, node.getId());
			}
			/*
			System.out.format("%nAt node: %d%n", node.getId());
			System.out.format("At state: %s%n", ProcessContext.getCurrentProcess().getComponentInstance().getModeChart().getCurrentMode());
			System.out.format("At time: %s%n", ProcessContext.getTimeProvider().getCurrentMilliseconds());
			*/
		}
		
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	

	///////////////////////////////////////////////////////////////////////////
	// Handle Planning ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	@Process
	@Mode(SearchMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planSearch(@In("searchPlanner") SearchTrajectoryPlanner planner,
			@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position,
			@In("map") DirtinessMap map) {
		Map<Node, Double> dirtiness = map.getDirtiness(); 
		if(dirtiness.isEmpty()){
			planner.updateTrajectory(trajectory.value);
		} else {
			Node targetTile = trajectory.value.isEmpty()
					? position.getValue().getLink().getTo()
					: trajectory.value.get(trajectory.value.size()-1).getTo();
			if(!dirtiness.keySet().contains(targetTile)){
				trajectory.value.clear();
				targetPlanner.updateTrajectory(dirtiness.keySet(), trajectory.value);
			}
		}
	}
	
	@Process
	@Mode(DockingMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planDock(@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("map") DirtinessMap map) {
		Set<Node> docks = map.getDockingStations();
		Node targetTile = trajectory.value.isEmpty() ? 
				null
				 : trajectory.value.get(trajectory.value.size()-1).getTo();
		if(targetTile == null || !docks.contains(targetTile)){
			trajectory.value.clear();
			targetPlanner.updateTrajectory(docks, trajectory.value);
		}
		
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
		
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = CLEAN_PROCESS_PERIOD)
	public static void clean(@InOut("map") ParamHolder<DirtinessMap> map,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position) {
		Node node = position.getValue().atNode();
		if(node != null){
			Double intensity = map.value.getDirtiness().get(node);
			if(intensity != null && intensity > 0){
				map.value.cleanDirtiness(node, intensity);
				System.out.format("\nCleaned %d\n\n", node.getId());
			}
		}
	}

	@Process
	@PeriodicScheduling(period = STATUS_PROCESS_PERIOD)
	public static void printStatus(@In("id") String id,
			@In("batteryLevel") CorrelationMetadataWrapper<Double> batteryLevel,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position) {
		System.out.println("#########################################");
		System.out.println("TIME: " + ProcessContext.getTimeProvider().getCurrentMilliseconds());
		System.out.println("ID: " + id);
		System.out.println("MODE: " + ProcessContext.getCurrentProcess().getComponentInstance().getModeChart().getCurrentMode());
		System.out.println("batteryLevel = " + batteryLevel.getValue());
		System.out.println("position = " + position);
		System.out.println("#########################################");
	}
}

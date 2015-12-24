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
package cz.cuni.mff.d3s.jdeeco.ua.component;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.BATTERY_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CHARGING_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEANING_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEANING_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVEMENT_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.STATUS_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.AVAILABLE_DOCK_OBSOLETE_THRESHOLD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.DifferenceMetric;
import cz.cuni.mff.d3s.jdeeco.ua.demo.DockData;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.metric.DirtinessMapMetric;
import cz.cuni.mff.d3s.jdeeco.ua.metric.PositionMetric;
import cz.cuni.mff.d3s.jdeeco.ua.mode.ChargingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.CleanMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.DeadBatteryMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.DirtApproachMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.DockingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.RobotModeChartHolder;
import cz.cuni.mff.d3s.jdeeco.ua.mode.SearchMode;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.role.DockableRole;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
 

@Component
@ComponentModeChart(RobotModeChartHolder.class)
@PlaysRole(DockableRole.class)
public class Robot {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;
	
	/** Battery level. */
	@CorrelationData(metric=DifferenceMetric.class,boundary=0.02,confidence=0.9)
	public CorrelationMetadataWrapper<Double> batteryLevel;
			
	@CorrelationData(metric=DirtinessMapMetric.class,boundary=5,confidence=0.9)
	public final CorrelationMetadataWrapper<DirtinessMap> map;
	
	@CorrelationData(metric=PositionMetric.class,boundary=4,confidence=0.9)
	public CorrelationMetadataWrapper<LinkPosition> position;
		
	public Map<String, DockData> availableDocks;

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
		map = new CorrelationMetadataWrapper<>(new DirtinessMap(id), "map");
		trajectory = new ArrayList<>();
		availableDocks = new HashMap<>();
	}

	///////////////////////////////////////////////////////////////////////////
	// Handle Battery /////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	@Process
	@Modes({SearchMode.class, DockingMode.class, DirtApproachMode.class})
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
	@ExcludeModes({ChargingMode.class, CleanMode.class, DeadBatteryMode.class})
	@PeriodicScheduling(period = MOVE_PROCESS_PERIOD)
	public static void move(@In("id") String id,
			@In("mover") TrajectoryExecutor mover,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@InOut("position") ParamHolder<CorrelationMetadataWrapper<LinkPosition>> position,
			@InOut("map") ParamHolder<CorrelationMetadataWrapper<DirtinessMap>> map) {
		boolean waitOnCollision = DockingMode.class.equals(ProcessContext.getCurrentProcess().getComponentInstance().getModeChart().getCurrentMode());
		// Move
		mover.move(trajectory.value, position.value.getValue(), waitOnCollision);
		
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		//Log.i(String.format("%s %d %s\n", id, currentTime, position.value.getValue()));
		
		position.value.setValue(position.value.getValue(), currentTime);

		// Check the tile for dirt
		Node node = position.value.getValue().atNode();
		if(node != null){
			map.value.getValue().getVisitedNodes().put(node, currentTime);
			// Check the dirtiness
			map.value.getValue().checkDirtiness(node);
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
	public static void planSearch(@In("id") String id,
			@In("searchPlanner") SearchTrajectoryPlanner planner,
			@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position,
			@In("map") CorrelationMetadataWrapper<DirtinessMap> map) {
		// Plan to search
		Set<Node> target = new HashSet<>();
		target.add(map.getValue().getRandomNode());
		targetPlanner.updateTrajectory(target, trajectory.value);
		//planner.updateTrajectory(trajectory.value);
	}
	@Process
	@Mode(DirtApproachMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planClean(@In("id") String id,
			@In("searchPlanner") SearchTrajectoryPlanner planner,
			@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position,
			@In("map") CorrelationMetadataWrapper<DirtinessMap> map) {
		// Plan to clean
		Map<Node, Double> dirtiness = map.getValue().getDirtiness(); 
		targetPlanner.updateTrajectory(dirtiness.keySet(), trajectory.value);
	}
	
	@Process
	@Mode(DockingMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planDock(@In("id") String id,
			@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("availableDocks") Map<String, DockData> docks) {
		if(docks.isEmpty()){
			long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
			Log.e(String.format("%s at %d planning to dock, but no dock available.",
					id, currentTime));
		} else {
			Set<Node> target = new HashSet<>();
			for(String dId : docks.keySet()){
				target.add(docks.get(dId).position);
			}
			trajectory.value.clear();
			targetPlanner.updateTrajectory(target, trajectory.value);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
		
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = CLEAN_PROCESS_PERIOD)
	public static void clean(@In("id") String id,
			@InOut("map") ParamHolder<CorrelationMetadataWrapper<DirtinessMap>> map,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position) {
		Node node = position.getValue().atNode();
		if(node != null && map.value.getValue().getDirtiness().containsKey(node)){
			double intensity = map.value.getValue().getDirtiness().get(node);
			if(intensity > 0.0){
				map.value.getValue().cleanDirtiness(node, CLEANING_RATE);
			}
		}
	}
	
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void removeObsoleteDocks(@In("id") String id,
			@InOut("availableDocks") ParamHolder<Map<String, DockData>> docks) {
		for(String dock : docks.value.keySet()){
			long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
			long recordTime = docks.value.get(dock).timestanp;
			if(currentTime - recordTime > AVAILABLE_DOCK_OBSOLETE_THRESHOLD){
				docks.value.remove(dock);
			}
		}
	}

	@Process
	@PeriodicScheduling(period = STATUS_PROCESS_PERIOD)
	public static void printStatus(@In("id") String id,
			@In("batteryLevel") CorrelationMetadataWrapper<Double> batteryLevel,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position,
			@In("trajectory") List<Link> trajectory,
			@In("availableDocks") Map<String, DockData> availableDocks) {
		Log.i("#########################################");
		Log.i("TIME: " + ProcessContext.getTimeProvider().getCurrentMilliseconds());
		Log.i("ID: " + id);
		Log.i("MODE: " + ProcessContext.getCurrentProcess().getComponentInstance().getModeChart().getCurrentMode());
		Log.i("batteryLevel = " + batteryLevel.getValue());
		Log.i("position = " + position.getValue());
		StringBuilder builder = new StringBuilder();
		/*for(String dock : availableDocks.keySet()){
			builder.append("\tDock:")
					.append("\n\t\tID = ").append(dock)
					.append("\n\t\tPosition = ").append(availableDocks.get(dock).position)
					.append("\n\t\tTimestamp = ").append(availableDocks.get(dock).timestanp)
					.append("\n");
		}
		Log.i("docks:\n" + builder.toString());
		builder = new StringBuilder();*/
		for(Link l : trajectory){
			builder.append(" -> ");
			builder.append(l.getId());
		}
		Log.i("plan: " + builder.toString());
		Log.i("#########################################");
	}
}

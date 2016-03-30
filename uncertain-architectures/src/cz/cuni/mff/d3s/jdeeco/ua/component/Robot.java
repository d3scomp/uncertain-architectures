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

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.AVAILABLE_DOCK_OBSOLETE_THRESHOLD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.BATTERY_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CHARGING_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEANING_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEANING_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_DETECTION_FAILURE_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_DETECTION_FAILURE_ROBOT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_DETECTION_FAILURE_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.IDLE_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVEMENT_ENERGY_COST;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.STATUS_PROCESS_PERIOD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.CorrelationData;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.NonDeterministicModeSwitching;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.DifferenceMetric;
import cz.cuni.mff.d3s.jdeeco.adaptation.search.annealing.Annealing;
import cz.cuni.mff.d3s.jdeeco.annotations.ComponentModeChart;
import cz.cuni.mff.d3s.jdeeco.annotations.ExcludeModes;
import cz.cuni.mff.d3s.jdeeco.annotations.Mode;
import cz.cuni.mff.d3s.jdeeco.annotations.Modes;
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
import cz.cuni.mff.d3s.jdeeco.ua.mode.WaitingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.adapt.AnnealingParams;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.role.DockableRole;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;
 

@Component
@ComponentModeChart(RobotModeChartHolder.class)
@NonDeterministicModeSwitching(searchEngine = Annealing.class,
	searchParameters = AnnealingParams.class)
@PlaysRole(DockableRole.class)
public class Robot {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;
	
	/** Battery level. */
	@CorrelationData(metric=DifferenceMetric.class,boundary=0.005,confidence=0.95)
	public CorrelationMetadataWrapper<Double> batteryLevel;
			
	@CorrelationData(metric=DirtinessMapMetric.class,boundary=5,confidence=0.9)
	public final CorrelationMetadataWrapper<DirtinessMap> map;
	
	@CorrelationData(metric=PositionMetric.class,boundary=3,confidence=0.9)
	public CorrelationMetadataWrapper<LinkPosition> position;
		
	public Map<String, DockData> availableDocks;
	
	public Map<String, Node> othersPlans;

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
	
	@Local
	public Random random;

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Robot(final String id, boolean withSeed, long seed) {
		this.id = id;
		map = new CorrelationMetadataWrapper<>(new DirtinessMap(id), "map");
		trajectory = new ArrayList<>();
		availableDocks = new HashMap<>();
		othersPlans = new HashMap<>();
		if (withSeed) {
			random = new Random(seed);	
		} else {
			random = new Random();
		}
		
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
	}

	@Process
	@ExcludeModes({SearchMode.class, DockingMode.class, DirtApproachMode.class, CleanMode.class, ChargingMode.class})
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void consumeBatteryIdle(
			@InOut("batteryLevel") ParamHolder<CorrelationMetadataWrapper<Double>> batteryLevel
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		double delta = IDLE_ENERGY_COST * (double) BATTERY_PROCESS_PERIOD / 1000;
		batteryLevel.value.setValue(Math.max(0, batteryLevel.value.getValue() - delta), currentTime);
	}

	@Process
	@Mode(ChargingMode.class)
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void charge(
			@InOut("batteryLevel") ParamHolder<CorrelationMetadataWrapper<Double>> batteryLevel,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position
	) {
		Node positionNode = position.getValue().atNode();
		if(positionNode == null){
			Log.w("Trying to charge between two nodes.");
		} else if (DirtinessMap.isDockWorking(positionNode)){
			long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
			double delta = CHARGING_RATE * (double) BATTERY_PROCESS_PERIOD / 1000;
			batteryLevel.value.setValue(Math.min(1, batteryLevel.value.getValue() + delta), currentTime);
		} else {
			Log.w("Trying to charge in malfunctioned dock.");
		}
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	

	///////////////////////////////////////////////////////////////////////////
	// Handle Movement ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	@Process
	@ExcludeModes({ChargingMode.class, CleanMode.class, DeadBatteryMode.class, WaitingMode.class})
	@PeriodicScheduling(period = MOVE_PROCESS_PERIOD)
	public static void move(@In("id") String id,
			@In("mover") TrajectoryExecutor mover,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@InOut("position") ParamHolder<CorrelationMetadataWrapper<LinkPosition>> position,
			@InOut("map") ParamHolder<CorrelationMetadataWrapper<DirtinessMap>> map) {
		LinkPosition positionValue = position.value.getValue();
		boolean waitOnCollision = DockingMode.class.equals(ProcessContext.getCurrentProcess().getComponentInstance().getModeChart().getCurrentMode());
		// Move
		mover.move(trajectory.value, positionValue, waitOnCollision);
		
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		//Log.i(String.format("%s %d %s\n", id, currentTime, position.value.getValue()));
		
		position.value.setValue(positionValue, currentTime);

		if (DIRT_DETECTION_FAILURE_ON) {
			if (DIRT_DETECTION_FAILURE_ROBOT.equals(id) && currentTime >= DIRT_DETECTION_FAILURE_TIME) {
				// If the dirt detection sensor failed, don't check the dirtiness
				map.value.malfunction();
				return;
			}
		}
		
		// Check the tile for dirt
		Node node = positionValue.atNode();
		if(node != null){
			DirtinessMap mapValue = map.value.getValue();
			mapValue.getVisitedNodes().put(node, currentTime);
			// Check the dirtiness
			mapValue.checkDirtiness(node);
			// Update the timestamp when dirtiness updated
			map.value.setValue(mapValue, currentTime);
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
			@In("map") CorrelationMetadataWrapper<DirtinessMap> map,
			@In("random") Random random) {
		// Plan to search
		Set<Node> target = new HashSet<>();
		target.add(map.getValue().getRandomNode(random));
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
			@In("map") CorrelationMetadataWrapper<DirtinessMap> map,
			@In("othersPlans") Map<String, Node> othersPlans) {
		Map<Node, Double> dirtiness = new HashMap<>(map.getValue().getDirtiness());
		// Avoid tiles targeted by others
		for(Node tile : othersPlans.values()){
			if(dirtiness.containsKey(tile)){
				dirtiness.remove(tile);
			}
		}
		// Plan to clean
		targetPlanner.updateTrajectory(dirtiness.keySet(), trajectory.value);
	}
	
	@Process
	@Mode(DockingMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planDock(@In("id") String id,
			@In("targetPlanner") NearestTrajectoryPlanner targetPlanner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory,
			@In("availableDocks") Map<String, DockData> docks,
			@In("position") CorrelationMetadataWrapper<LinkPosition> position) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		if(docks.isEmpty()){
			Log.e(String.format("%s at %d planning to dock, but no dock available.",
					id, currentTime));
		} else if (position.getValue().isLinkLeft()){
			Set<Node> targets = new HashSet<>();
			for(String dId : docks.keySet()){
				targets.add(docks.get(dId).position);
			}
			trajectory.value.clear();
			targetPlanner.updateTrajectory(targets, trajectory.value);
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

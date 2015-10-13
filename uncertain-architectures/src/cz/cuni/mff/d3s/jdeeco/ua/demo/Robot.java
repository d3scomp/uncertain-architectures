package cz.cuni.mff.d3s.jdeeco.ua.demo;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.POSION_STATE_HISTORY;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.computeCurrentInaccuracy;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.currentTime;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.getInaccuracyHistory;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.resetBatteryStateIfNeeded;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestOldestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.ShortestTrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryPlanner;
 

@Component
public class Robot {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;
	
	/** Battery level. */
	public MetadataWrapper<Double> batteryLevel;

	/** Position in corridor coordinate system. */
	public MetadataWrapper<PositionKnowledge> position;

	@Local
	public final DirtinessMap map;

	@Local
	public final List<Position> trajectory;
	
	@Local
	public final TrajectoryPlanner planner;
	
	@Local
	public final TrajectoryExecutor mover;
	

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Robot(final String id) {
		this.id = id;
		map = new DirtinessMap();
		batteryLevel = new MetadataWrapper<>(Environment.getInitialBattery(id));
		position = new MetadataWrapper<>(Environment.getInitialPosition(id));
		trajectory = new ArrayList<>();
		
		//TODO: provide constructor in which there can be set specific planner and executor
		planner = new NearestOldestTrajectoryPlanner(map);
		mover = new ShortestTrajectoryExecutor(map);
	}

	@Process
	@PeriodicScheduling(period=1000)
	public static void determineBatteryLevel(
			@In("id") String id,
			@InOut("batteryLevel") ParamHolder<MetadataWrapper<Double>> batteryLevel
	) {

		if (batteryLevel.value.isOperational()) {
			batteryLevel.value.setValue(Environment.getBatteryLevel(id), currentTime());
		}
		resetBatteryStateIfNeeded(batteryLevel.value.getValue());
	}

	@Process
	@PeriodicScheduling(period = Environment.INITIAL_POSITION_PERIOD)
	public static void determinePosition(
		@In("id") String id,
		@InOut("position") ParamHolder<MetadataWrapper<PositionKnowledge>> position
	) {
		final double inacc = computeCurrentInaccuracy(position.value);
		final Deque<Double> history = getInaccuracyHistory();
		if (history.size() >= POSION_STATE_HISTORY) {
			history.removeFirst();
		}
		history.add(inacc);
		if (position.value.isOperational()) {
			position.value.setValue(Environment.getPosition(id), currentTime());
		}
	}

	@Process
	@PeriodicScheduling(period=MOVE_PROCESS_PERIOD)
	public static void move(@In("id") String id) {
		
	}
	
	@Process
	@PeriodicScheduling(period=PLAN_PROCESS_PERIOD)
	public static void plan(@In("planner") TrajectoryPlanner planner,
			@InOut("trajectory") ParamHolder<List<Position>> trajectory) {
		planner.updateTrajectory(trajectory.value);
	}

	@Process
	@PeriodicScheduling(period=1000) // TODO: get rid of these constants
	public static void printStatus(@In("id") String id,
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel,
			@In("position") MetadataWrapper<PositionKnowledge> position) {
		System.out.println("#########################################");
		System.out.println("TIME: " + ProcessContext.getTimeProvider().getCurrentMilliseconds());
		System.out.println("ID: " + id);
		System.out.println("batteryLevel = " + batteryLevel);
		System.out.println("position = " + position);
		System.out.println("#########################################");
	}
}

package cz.cuni.mff.d3s.jdeeco.ua.demo;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.BATTERY_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CLEAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.PLAN_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.STATUS_PROCESS_PERIOD;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.ComponentModeChart;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Mode;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.mode.CleanMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.DockingMode;
import cz.cuni.mff.d3s.jdeeco.ua.mode.RobotModeChartHolder;
import cz.cuni.mff.d3s.jdeeco.ua.mode.SearchMode;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryPlanner;
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
	public MetadataWrapper<Double> batteryLevel;
	
	public Boolean isOnDirt;
	
	public Boolean isOnDock;
	
	@Local
	public final DirtinessMap map;
	
	@Local
	public LinkPosition position;

	@Local
	public final List<Link> trajectory;

	@Local
	public TrajectoryPlanner searchPlanner;
	
	@Local
	public TrajectoryPlanner dockPlanner;
	
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
	public Robot(final String id, RuntimeLogger runtimeLogger) {
		this.id = id;
		map = new DirtinessMap(id, runtimeLogger);
		trajectory = new ArrayList<>();
	}

	@Process
	@PeriodicScheduling(period = BATTERY_PROCESS_PERIOD)
	public static void determineBatteryLevel(
			@InOut("batteryLevel") ParamHolder<MetadataWrapper<Double>> batteryLevel
	) {
		// TODO: decrease battery level - introduce state variable that determines whether the robot moves, cleans or is idle
		// TODO: use battery noise
	}
	
	@Process
	@PeriodicScheduling(period = MOVE_PROCESS_PERIOD)
	public static void move(@In("mover") TrajectoryExecutor mover,
			@In("trajectory") List<Link> trajectory,
			@InOut("position") ParamHolder<LinkPosition> position,
			@In("map") DirtinessMap map,
			@InOut("isOnDirt") ParamHolder<Boolean> isOnDirt,
			@InOut("isOnDock") ParamHolder<Boolean> isOnDock) {
		mover.move(trajectory, position.value);
		Node currentNode = position.value.atNode();
		if(currentNode != null){
			if(map.getDockingStations().contains(currentNode)){
				isOnDock.value = true;
			} else {
				isOnDock.value = false;
			}
			isOnDirt.value = false;
			if(map.getDirtiness().containsKey(currentNode)
					&& map.getDirtiness().get(currentNode) > 0){
				isOnDirt.value = true;
			}
		}
	}
	
	@Process
	@Mode(SearchMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planSearch(@In("searchPlanner") TrajectoryPlanner planner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory) {
		planner.updateTrajectory(trajectory.value);
	}

	@Process
	@Mode(DockingMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planDock(@In("dockPlanner") TrajectoryPlanner planner,
			@InOut("trajectory") ParamHolder<List<Link>> trajectory) {
		planner.updateTrajectory(trajectory.value);
	}
	
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = PLAN_PROCESS_PERIOD)
	public static void planClean(
			@InOut("trajectory") ParamHolder<List<Link>> trajectory) {
		// No movement during cleaning
		trajectory.value.clear();
	}
	
	@Process
	@Mode(SearchMode.class)
	@PeriodicScheduling(period = CLEAN_PROCESS_PERIOD)
	public static void checkDirt(@InOut("map") ParamHolder<DirtinessMap> map,
			@In("position") LinkPosition position) {
		Node node = position.atNode();
		if(node != null){
			map.value.checkDirtiness(node);
		}
	}
	
	@Process
	@Mode(CleanMode.class)
	@PeriodicScheduling(period = CLEAN_PROCESS_PERIOD)
	public static void clean(@InOut("map") ParamHolder<DirtinessMap> map,
			@In("position") LinkPosition position) {
		Node node = position.atNode();
		if(node != null){
			Double intensity = map.value.getDirtiness().get(node);
			if(intensity != null && intensity > 0){
				map.value.cleanDirtiness(node, intensity);
			}
		}
	}

	@Process
	@PeriodicScheduling(period = STATUS_PROCESS_PERIOD)
	public static void printStatus(@In("id") String id,
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel,
			@In("position") LinkPosition position) {
		System.out.println("#########################################");
		System.out.println("TIME: " + ProcessContext.getTimeProvider().getCurrentMilliseconds());
		System.out.println("ID: " + id);
		System.out.println("batteryLevel = " + batteryLevel.getValue());
		System.out.println("position = " + position);
		System.out.println("#########################################");
	}
}

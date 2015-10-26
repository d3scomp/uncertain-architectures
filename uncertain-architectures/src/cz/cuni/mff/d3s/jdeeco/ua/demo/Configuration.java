package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.Random;

import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestOldestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.ShortestTrajectoryExecutor;

/**
 * This class holds the overall configuration of the demo simulation.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class Configuration {

	/** Random number generator for the simulation */
	public static final Random RANDOM = new Random(246811);
	
	///////////////////////////////////////////////////////////////////////////
	// MAP CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * The width of a tile used in the {@link DirtinessMap}.
	 * Tiles are squares. The dimension is expresses in meters.
	 */
	public static final double TILE_WIDTH = 1; // m

	/** 
	 * Width of the map.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	public static final int MAP_WIDTH = 20;

	/**
	 * Heigt of the map.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	public static final int MAP_HEIGHT = 20;
	
	///////////////////////////////////////////////////////////////////////////
	// ROBOT CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The period of a process that moves the robot.
	 * Expressed in milliseconds.
	 */
	public static final long MOVE_PROCESS_PERIOD = 100; // ms

	/**
	 * The period of a process that determines the robots position.
	 * Expressed in milliseconds.
	 */
	public static final long DETERMINE_POSITION_PERIOD = 1000; // ms
	
	/**
	 * The period of a process that plans the robots trajectory.
	 * Expressed in milliseconds.
	 */
	public static final long PLAN_PROCESS_PERIOD = 1000; // ms

	/**
	 * The period of a process that measures the robots battery.
	 * Expressed in milliseconds.
	 */
	public static final long BATTERY_PROCESS_PERIOD = 1000; // ms
	
	/**
	 * The period of a process that prints the robots status.
	 * Expressed in milliseconds.
	 */
	public static final long STATUS_PROCESS_PERIOD = 1000; // ms
	
	/**
	 * The Speed of the robot.
	 * Expressed in meters per second.
	 */
	public static final int ROBOT_SPEED = 1; // m/s

	/**
	 * The limit beyond which two positions are considered the same.
	 * Expressed in meters.
	 */
	public static final double POSITION_ACCURACY = 0.01; // m
	
	/**
	 * The energy consumption during robot movement.
	 * Expressed in percents per second.
	 */
	public static final double MOVEMENT_ENERGY_COST = 0.001; // %/s

	/**
	 * The energy consumption during robot cleaning.
	 * Expressed in percents per second.
	 */
	public static final double CLEANING_ENERGY_COST = 0.002; // %/s

	/**
	 * The energy consumption when robot is idle.
	 * Expressed in percents per second.
	 */
	public static final double IDLE_ENERGY_COST = 0.0001; // %/s
		
	// ROBOT 1 ////////////////////////////////////////////////////////////////
	
	public static final Robot ROBOT1 = RobotFactory.newRobot("TB1")
			.atPosition(5)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();

	// ROBOT 2 ////////////////////////////////////////////////////////////////

	public static final Robot ROBOT2 = RobotFactory.newRobot("TB2")
			.atPosition(10)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();
	
	// ROBOT 3 ////////////////////////////////////////////////////////////////

	public static final Robot ROBOT3 = RobotFactory.newRobot("TB3")
			.atPosition(0)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();
	
	///////////////////////////////////////////////////////////////////////////
}

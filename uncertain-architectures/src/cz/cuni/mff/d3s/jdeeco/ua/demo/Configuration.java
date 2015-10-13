package cz.cuni.mff.d3s.jdeeco.ua.demo;

import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleNoise;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionNoise;
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
	 * The period of a process that plant the robots trajectory.
	 * Expressed in milliseconds.
	 */
	public static final long PLAN_PROCESS_PERIOD = 1000; // ms
	
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
		
	// ROBOT 1 ////////////////////////////////////////////////////////////////
	
	public static final Robot ROBOT1 = RobotFactory.newRobot("TB1")
			.atPosition(new Position(0, 0))
			.withPositionNoise(new PositionNoise("TB1_position_noise", 0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleNoise("TB1_battery_noise", 0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();

	// ROBOT 2 ////////////////////////////////////////////////////////////////

	public static final Robot ROBOT2 = RobotFactory.newRobot("TB2")
			.atPosition(new Position(0, 0))
			.withPositionNoise(new PositionNoise("TB2_position_noise", 0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleNoise("TB3_battery_noise", 0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();
	
	// ROBOT 3 ////////////////////////////////////////////////////////////////

	public static final Robot ROBOT3 = RobotFactory.newRobot("TB3")
			.atPosition(new Position(0, 0))
			.withPositionNoise(new PositionNoise("TB3_position_noise", 0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleNoise("TB3_battery_noise", 0.0, 0.01))
			.withTrajectoryPlanner(new NearestOldestTrajectoryPlanner())
			.withTrajectoryExecutor(new ShortestTrajectoryExecutor())
			.create();
	
	///////////////////////////////////////////////////////////////////////////
}

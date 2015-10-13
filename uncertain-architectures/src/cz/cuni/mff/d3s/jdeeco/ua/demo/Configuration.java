package cz.cuni.mff.d3s.jdeeco.ua.demo;

import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;

/**
 * This class holds the overall configuration of the demo simulation.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class Configuration {

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
}

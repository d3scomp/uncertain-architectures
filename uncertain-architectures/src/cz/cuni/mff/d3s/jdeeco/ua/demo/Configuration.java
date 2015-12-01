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

import java.util.Random;

import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;

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
	public static final int MAP_WIDTH = 5;

	/**
	 * Heigt of the map.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	public static final int MAP_HEIGHT = 5;
	
	public static final double DIRT_GENERATION_RATE = 0.1;
	
	///////////////////////////////////////////////////////////////////////////
	// ROBOT CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The period of a process that moves the robot.
	 * Expressed in milliseconds.
	 */
	public static final long MOVE_PROCESS_PERIOD = 100; // ms
	
	/**
	 * The period of a process that moves the robot.
	 * Expressed in milliseconds.
	 */
	public static final long CLEAN_PROCESS_PERIOD = 1000; // ms

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
	public static final double MOVEMENT_ENERGY_COST = 0.001; // %/s, 1 is 100%

	/**
	 * The energy consumption during robot cleaning.
	 * Expressed in percents per second.
	 */
	public static final double CLEANING_ENERGY_COST = 0.002; // %/s, 1 is 100%

	/**
	 * The energy consumption when robot is idle.
	 * Expressed in percents per second.
	 */
	public static final double IDLE_ENERGY_COST = 0.0001; // %/s, 1 is 100%
	
	/**
	 * The charging rate of the robot.
	 * Expressed in percents per second.
	 */
	public static final double CHARGING_RATE = 0.05; // %/s, 1 is 100%
		
	// ROBOT 1 ////////////////////////////////////////////////////////////////
	
	public static final Robot createRobot1(){
		return RobotFactory.newRobot("TB1")
			.atPosition(5)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}

	// ROBOT 2 ////////////////////////////////////////////////////////////////

	public static final Robot createRobot2(){
		return RobotFactory.newRobot("TB2")
			.atPosition(10)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	// ROBOT 3 ////////////////////////////////////////////////////////////////

	public static final Robot createRobot3(){
		return RobotFactory.newRobot("TB3")
			.atPosition(0)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(1)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	///////////////////////////////////////////////////////////////////////////
}

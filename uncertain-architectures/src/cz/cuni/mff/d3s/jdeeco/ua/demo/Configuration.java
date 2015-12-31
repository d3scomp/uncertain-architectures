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

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.jdeeco.ua.component.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;

/**
 * <p>
 * This class holds the overall configuration of the demo simulation.
 * </p>
 * <p>
 * Parameters that are common to both the simulation and the analysis scripts
 * (e.g. simulation duration) are found in config/simulationParameters.txt
 * </p>
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class Configuration {
	
	///////////////////////////////////////////////////////////////////////////
	// COMPONENTS IDs AND SEEDS
	///////////////////////////////////////////////////////////////////////////

	public static final String ROBOT1_NAME = "TB1";
	public static final long ROBOT1_SEED = 9387;
	public static final String ROBOT2_NAME = "TB2";
	public static final long ROBOT2_SEED = 437436;
	public static final String ROBOT3_NAME = "TB3";
	public static final long ROBOT3_SEED = 453987;

	public static final String DOCK1_NAME = "Dock1";
	public static final String DOCK2_NAME = "Dock2";

	public static final String ENVIRONMENT_NAME = "Environment";
	public static final long ENVIRONMENT_SEED = 85326;
	
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
	
	public static final double DIRT_GENERATION_RATE = 0.1;
	
	
	///////////////////////////////////////////////////////////////////////////
	// SIMULATION CONFIGURATION
	///////////////////////////////////////////////////////////////////////////
	
	public static final String DIRT_DETECTION_FAILURE_ROBOT = ROBOT1_NAME;
	
	public static final long DIRT_DETECTION_FAILURE_TIME = 300_000;
	
	public static final String DOCK_TO_FAIL = DOCK2_NAME;
	
	public static final long DOCK_FAILURE_TIME = 200_000;
	
	
	///////////////////////////////////////////////////////////////////////////
	// ROBOT CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The period of a process that moves the robot.
	 * Expressed in milliseconds.
	 */
	public static final long MOVE_PROCESS_PERIOD = 100; // ms
	
	/**
	 * The period of a cleaning process.
	 * Expressed in milliseconds.
	 */
	public static final long CLEAN_PROCESS_PERIOD = 500; // ms

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
	 * The period of a process that generates dirtiness.
	 * Expressed in milliseconds.
	 */
	public static final long DIRT_GENERATION_PERIOD = 200; // ms
	
	/**
	 * The period of a process that checks whether the docking station works.
	 * Expressed in milliseconds.
	 */
	public static final long DOCK_CHECK_PERIOD = 1000; // ms
	
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
	public static final double MOVEMENT_ENERGY_COST = 0.0015; // %/s, 1 is 100%

	/**
	 * The energy consumption during robot cleaning.
	 * Expressed in percents per second.
	 */
	public static final double CLEANING_ENERGY_COST = 0.003; // %/s, 1 is 100%

	/**
	 * The energy consumption when robot is idle.
	 * Expressed in percents per second.
	 */
	public static final double IDLE_ENERGY_COST = 0.0005; // %/s, 1 is 100%
	
	/**
	 * The amount of dirt that is cleaned in one cycle of cleaning.
	 * Expressed in percents per {@link #CLEAN_PROCESS_PERIOD}
	 */
	public static final double CLEANING_RATE = 0.1;
	
	/**
	 * The charging rate of the robot.
	 * Expressed in percents per second.
	 */
	public static final double CHARGING_RATE = 0.05; // %/s, 1 is 100%
	
	/**
	 * The time after which the record about available dock becomes obsolete.
	 * Expressed in milliseconds.
	 */
	public static final long AVAILABLE_DOCK_OBSOLETE_THRESHOLD = 3000; // ms
		
	// ROBOT 1 ////////////////////////////////////////////////////////////////
	
	public static final Robot createRobot1(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT1_NAME, ROBOT1_SEED, runtimeLogger)
			.atPosition(5)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.3)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}

	// ROBOT 2 ////////////////////////////////////////////////////////////////

	public static final Robot createRobot2(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT2_NAME, ROBOT2_SEED, runtimeLogger)
			.atPosition(10)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.3)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	// ROBOT 3 ////////////////////////////////////////////////////////////////

	public static final Robot createRobot3(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT3_NAME, ROBOT3_SEED, runtimeLogger)
			.atPosition(0)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.3)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	///////////////////////////////////////////////////////////////////////////
}

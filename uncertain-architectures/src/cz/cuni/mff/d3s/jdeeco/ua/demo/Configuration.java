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

	public static final boolean WITH_SEED = false;

	public static final String ROBOT1_NAME = "TB1";
	public static final long ROBOT1_SEED = 9387;
	public static final String ROBOT2_NAME = "TB2";
	public static final long ROBOT2_SEED = 437436;
	public static final String ROBOT3_NAME = "TB3";
	public static final long ROBOT3_SEED = 453987;
	public static final String ROBOT4_NAME = "TB4";
	public static final long ROBOT4_SEED = 847222;
	public static final String ROBOT5_NAME = "TB5";
	public static final long ROBOT5_SEED = 453443;

	public static final String DOCK1_NAME = "Dock1";
	public static final String DOCK2_NAME = "Dock2";

	public static final String ENVIRONMENT_NAME = "Environment";
	public static final long ENVIRONMENT_SEED = 85328;
	
	/////////////////////////////////////////////////////////////////////////
	// SIMULATION CONFIGURATION
	///////////////////////////////////////////////////////////////////////////

	public static final long SIMULATION_DURATION = 600_000;

	public static double PROBABILITY = 0;
	public static boolean CORRELATION_ON = false;
	public static boolean ROLE_REMOVAL_ON = false;
	
	public static boolean DIRT_DETECTION_FAILURE_ON = false;
	public static boolean DOCK_FAILURE_ON = false;

	public static final String DIRT_DETECTION_FAILURE_ROBOT = ROBOT1_NAME;
	public static final long DIRT_DETECTION_FAILURE_TIME = 100_000;

	public static final String DOCK_TO_FAIL = DOCK2_NAME;
	public static final long DOCK_FAILURE_TIME = 50_000;
	
	///////////////////////////////////////////////////////////////////////////
	// MAP CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * The width of a tile (square) used in the {@link DirtinessMap}.
	 */
	public static final double TILE_WIDTH = 1; // m

	/** 
	 * Width of the map in number of tiles.
	 */
	public static final int MAP_WIDTH = 20;

	/**
	 * Height of the map in number of tiles.
	 */
	public static final int MAP_HEIGHT = 20;
	
	/**
	 * Used in {@link DirtinessMap#generateDirt(java.util.Random)}. Together with
	 * {@link DIRT_GENERATION_PERIOD} determines how often dirt will be
	 * generated.
	 */
	public static final double DIRT_GENERATION_RATE = 0.1;
	
	///////////////////////////////////////////////////////////////////////////
	// ROBOT CONFIGURATION 
	///////////////////////////////////////////////////////////////////////////

	/////////////////
	// PERIODS 
	/////////////////

	public static final long MOVE_PROCESS_PERIOD = 100; // ms
	
	public static final long CLEAN_PROCESS_PERIOD = 500; // ms

	public static final long DETERMINE_POSITION_PERIOD = 1000; // ms
	
	public static final long PLAN_PROCESS_PERIOD = 1000; // ms

	public static final long BATTERY_PROCESS_PERIOD = 1000; // ms
	
	public static final long STATUS_PROCESS_PERIOD = 1000; // ms
	
	public static final long DIRT_GENERATION_PERIOD = 600; // ms
	
	public static final long DOCK_CHECK_PERIOD = 1000; // ms
	
	/////////////////
	// EXTRAS 
	/////////////////

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
	public static final double MOVEMENT_ENERGY_COST = 0.003; // %/s, 1 is 100%

	/**
	 * The energy consumption during robot cleaning.
	 * Expressed in percents per second.
	 */
	public static final double CLEANING_ENERGY_COST = 0.006; // %/s, 1 is 100%

	/**
	 * The energy consumption when robot is idle.
	 * Expressed in percents per second.
	 */
	public static final double IDLE_ENERGY_COST = 0.001; // %/s, 1 is 100%
	
	/**
	 * The amount of dirt that is cleaned in one cycle of cleaning.
	 * Expressed in percents per {@link #CLEAN_PROCESS_PERIOD}
	 */
	public static final double CLEANING_RATE = 0.1;
	
	/**
	 * The charging rate of the robot.
	 * Expressed in percents per second.
	 */
	public static final double CHARGING_RATE = 0.02; // %/s, 1 is 100%
	
	/**
	 * The time after which the record about available dock becomes obsolete.
	 * Expressed in milliseconds.
	 */
	public static final long AVAILABLE_DOCK_OBSOLETE_THRESHOLD = 2000; // ms
		
	/////////////////
	// ROBOT 1 
	/////////////////

	public static final Robot createRobot1(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT1_NAME, WITH_SEED, ROBOT1_SEED, runtimeLogger)
			.atPosition(5)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.4)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}

	/////////////////
	// ROBOT 2 
	/////////////////

	public static final Robot createRobot2(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT2_NAME, WITH_SEED, ROBOT2_SEED, runtimeLogger)
			.atPosition(10)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.4)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	/////////////////
	// ROBOT 3 
	/////////////////

	public static final Robot createRobot3(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT3_NAME, WITH_SEED, ROBOT3_SEED, runtimeLogger)
			.atPosition(0)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.4)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	/////////////////
	// ROBOT 4 
	/////////////////

	public static final Robot createRobot4(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT4_NAME, WITH_SEED, ROBOT4_SEED, runtimeLogger)
			.atPosition(15)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.4)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	
	/////////////////
	// ROBOT 5 
	/////////////////

	public static final Robot createRobot5(RuntimeLogger runtimeLogger){
		return RobotFactory.newRobot(ROBOT5_NAME, WITH_SEED, ROBOT5_SEED, runtimeLogger)
			.atPosition(20)
			.withPositionNoise(new PositionFilter(0.0, 0.1))
			.withBatteryLevel(0.4)
			.withBatteryNoise(new DoubleFilter(0.0, 0.01))
			.withTrajectoryPlanner(new SearchTrajectoryPlanner())
			.withDockingPlanner(new NearestTrajectoryPlanner())
			.withTrajectoryExecutor(new TrajectoryExecutor())
			.create();
	}
	///////////////////////////////////////////////////////////////////////////
}

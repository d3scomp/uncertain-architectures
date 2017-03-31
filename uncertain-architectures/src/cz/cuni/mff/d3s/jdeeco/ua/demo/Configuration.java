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

import java.lang.reflect.Field;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.cuni.mff.d3s.deeco.logging.Log;
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
	// COMPONENTS IDs
	///////////////////////////////////////////////////////////////////////////

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
	public static final String DOCK3_NAME = "Dock3";
	public static final String[] DOCK_NAMES = new String[]{DOCK1_NAME, DOCK2_NAME, DOCK3_NAME};

	public static final String ENVIRONMENT_NAME = "Environment";
	
	/////////////////////////////////////////////////////////////////////////
	// SIMULATION CONFIGURATION
	///////////////////////////////////////////////////////////////////////////

	public static String LOG_DIR = null;

	public static int ROBOT_COUNT = 3;
	public static int DOCK_COUNT = 3;

	public static boolean WITH_SEED = false;
	public static long ENVIRONMENT_SEED = 85328;
	
	public static long WARM_UP_TIME = 0;
	public static long SIMULATION_DURATION = 600_000;
	public static int DIRT_DETECTION_RADIUS = 2;
	
	// COLLABORATIVE SENSING
	
	public static boolean CORRELATION_ON = false;
	public static boolean DIRT_DETECTION_FAILURE_ON = false;
	public static long DIRT_DETECTION_FAILURE_TIME = 100_000;
	public static final String DIRT_DETECTION_FAILURE_ROBOT = ROBOT1_NAME;
	
	// FAULTY COMPONENT ISOLATION

	public static boolean ROLE_REMOVAL_ON = false;
	public static boolean DOCK_FAILURE_ON = false;
	public static long DOCK_FAILURE_TIME = 50_000;
	public static final String DOCK_TO_FAIL = DOCK2_NAME;
	
//	public static double NON_DET_INIT_PROBABILITY = 0.0001;
//	public static double NON_DET_PROBABILITY_STEP = 0.00005;
//	public static long NON_DET_START_TIME = 0;
//	public static long NON_DET_END_TIME = SIMULATION_DURATION;
	
	// ENHANCING MODE SWITCHING

	public static boolean NON_DETERMINISM_ON = true;
	public static double TRANSITION_PROBABILITY = 0.01;
	public static int TRANSITION_PRIORITY = 10;
	public static boolean NON_DETERMINISM_TRAINING = true;
	public static String NON_DETERMINISM_TRAIN_FROM = "DeadBatteryMode";
	public static String NON_DETERMINISM_TRAIN_TO = "WaitingMode";
	public static String NON_DETERMINISM_TRAINING_OUTPUT = "train.txt";//null;
	
	// MODE SWITCHING PROPERTIES

	public static boolean MODE_SWITCH_PROPS_ON = false;
	
	public static String UTILITY_DIRECTORY = "results\\logs\\06)-!DDF-!DF-UMS-6\\UMS_logs";
		

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
	public static final double DIRT_GENERATION_RATE = 0.5;
	
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
	public static final int ROBOT_SPEED = 2; // m/s

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
	public static final double IDLE_ENERGY_COST = 0.0005; // %/s, 1 is 100%
	
	/**
	 * The amount of dirt that is cleaned in one cycle of cleaning.
	 * Expressed in percents per {@link #CLEAN_PROCESS_PERIOD}
	 */
	public static final double CLEANING_RATE = 0.2;
	
	/**
	 * The charging rate of the robot.
	 * Expressed in percents per second.
	 */
	public static final double CHARGING_RATE = 0.015; // %/s, 1 is 100%
	
	/**
	 * The time after which the record about available dock becomes obsolete.
	 * Expressed in milliseconds.
	 */
	public static final long AVAILABLE_DOCK_OBSOLETE_THRESHOLD = 2000; // ms
		
	/////////////////
	// ROBOTS
	/////////////////
	
	private static Random randBattery = new Random();

	public static final RobotParameters ROBOT_PARAMS[] = 
			new RobotParameters[]{
					new RobotParameters(ROBOT1_NAME,
							ROBOT1_SEED,
							5, // Start Link Number (Position)
							new PositionFilter(0.0, 0.1),
							0.3 + randBattery.nextDouble()*0.5, // Init Battery Level
							new DoubleFilter(0.0, 0.01), // Battery noise
							new SearchTrajectoryPlanner(), // Search planner
							new NearestTrajectoryPlanner(), // Docking planner
							new TrajectoryExecutor()),
					new RobotParameters(ROBOT2_NAME,
							ROBOT2_SEED,
							10, // Start Link Number (Position)
							new PositionFilter(0.0, 0.1),
							0.3 + randBattery.nextDouble()*0.5, // Init Battery Level
							new DoubleFilter(0.0, 0.01), // Battery noise
							new SearchTrajectoryPlanner(), // Search planner
							new NearestTrajectoryPlanner(), // Docking planner
							new TrajectoryExecutor()),
					new RobotParameters(ROBOT3_NAME,
							ROBOT3_SEED,
							0, // Start Link Number (Position)
							new PositionFilter(0.0, 0.1),
							0.3 + randBattery.nextDouble()*0.5, // Init Battery Level
							new DoubleFilter(0.0, 0.01), // Battery noise
							new SearchTrajectoryPlanner(), // Search planner
							new NearestTrajectoryPlanner(), // Docking planner
							new TrajectoryExecutor()),
					new RobotParameters(ROBOT4_NAME,
							ROBOT4_SEED,
							15, // Start Link Number (Position)
							new PositionFilter(0.0, 0.1),
							0.3 + randBattery.nextDouble()*0.5, // Init Battery Level
							new DoubleFilter(0.0, 0.01), // Battery noise
							new SearchTrajectoryPlanner(), // Search planner
							new NearestTrajectoryPlanner(), // Docking planner
							new TrajectoryExecutor()),
					new RobotParameters(ROBOT5_NAME,
							ROBOT5_SEED,
							20, // Start Link Number (Position)
							new PositionFilter(0.0, 0.1),
							0.3 + randBattery.nextDouble()*0.5, // Init Battery Level
							new DoubleFilter(0.0, 0.01), // Battery noise
							new SearchTrajectoryPlanner(), // Search planner
							new NearestTrajectoryPlanner(), // Docking planner
							new TrajectoryExecutor())
			};

	public static final Robot createRobot(int robotNumber, RuntimeLogger runtimeLogger){
		RobotParameters params = ROBOT_PARAMS[robotNumber];
		return RobotFactory.newRobot(params.robotName, WITH_SEED, params.robotSeed, runtimeLogger)
			.atPosition(params.startLink)
			.withPositionNoise(params.positionNoise)
			.withBatteryLevel(params.initBattery)
			.withBatteryNoise(params.batteryNoise)
			.withTrajectoryPlanner(params.trajectoryPlanner)
			.withDockingPlanner(params.dockingPlanner)
			.withTrajectoryExecutor(params.trajectoryExecutor)
			.create();
	}

	///////////////////////////////////////////////////////////////////////////
	
	public static void override(String[] params){
		final Pattern nameValuePattern = Pattern.compile("(\\w+)=(.+)");
		
		for(String param : params){
			final Matcher nameValueMatcher = nameValuePattern.matcher(param);
			if((!nameValueMatcher.matches()) ||
					nameValueMatcher.groupCount() != 2){
				Log.e(String.format("The \"%s\" parameter is not valid.", param));
				continue;
			}
			
			final String name = nameValueMatcher.group(1);
			final String value = nameValueMatcher.group(2);
			try{
				Field field = Configuration.class.getField(name);
				setValue(field, value);
			} catch(NoSuchFieldException e){
				Log.e(String.format("The configuration field \"%s\" cannot be found", name));
				continue;
			}
			
		}
	}
	
	private static void setValue(Field field, String value){
		Log.i(String.format("Overriding: %s = %s", field.getName(), value));
		
		Class<?> type = field.getType();
		if(type == long.class || type == Long.class){
			try{
				long v = Long.parseLong(value);
				field.set(null, v);
			}catch (Exception e) {
				Log.e(e.getMessage());
			}
		} else if(type == int.class || type == Integer.class){
			try{
				int v = Integer.parseInt(value);
				field.set(null, v);
			}catch (Exception e) {
				Log.e(e.getMessage());
			}
		} else if(type == boolean.class || type == Boolean.class){
			try{
				boolean v = Boolean.parseBoolean(value);
				field.set(null, v);
			}catch (Exception e) {
				Log.e(e.getMessage());
			}
		} else if(type == double.class || type == Double.class){
			try{
				double v = Double.parseDouble(value);
				field.set(null, v);
			}catch (Exception e) {
				Log.e(e.getMessage());
			}
		} else if(type == String.class){
			try{
				field.set(null, value);
			}catch (Exception e) {
				Log.e(e.getMessage());
			}
		} else {
			Log.e(String.format("Unknown type to assign: %s", type.toString()));
		}
	}
}

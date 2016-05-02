/*******************************************************************************
 * Copyright 2014, 2015 Charles University in Prague
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.CORRELATION_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_DETECTION_FAILURE_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_DETECTION_FAILURE_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_NAMES;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_FAILURE_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_FAILURE_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_NAME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_SEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_END_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_INIT_PROBABILITY;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_PROBABILITY_STEP;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_START_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROLE_REMOVAL_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.SIMULATION_DURATION;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.WITH_SEED;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogWriters;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.CorrelationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchAnnealState;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDeterministicModeSwitchingPlugin;
import cz.cuni.mff.d3s.jdeeco.modes.ModeSwitchingPlugin;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;
import cz.cuni.mff.d3s.jdeeco.ua.component.Dock;
import cz.cuni.mff.d3s.jdeeco.ua.component.Environment;
import cz.cuni.mff.d3s.jdeeco.ua.ensemble.CleaningPlanEnsemble;
import cz.cuni.mff.d3s.jdeeco.ua.ensemble.DockingEnsemble;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.mode.adapt.AnnealingParams;
import cz.cuni.mff.d3s.jdeeco.ua.mode.adapt.DirtinessDurationFitness;
import cz.cuni.mff.d3s.jdeeco.ua.visualization.VisualizationSettings;

/**
 * This class contains main for centralized run.
 */
public class Run {

	/** Deployment-related configuration parameters */
	static final boolean enableMultipleDEECoNodes = false;

	/**
	 * Runs centralized simulation.
	 * 
	 * @param args
	 *            command line arguments, ignored
	 * @throws DEECoException
	 * @throws AnnotationProcessorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void main(final String args[]) throws Exception {
		Log.i("Preparing simulation");

		// Process arguments
		String logPath = "STANDARD";
		RuntimeLogWriters writers;
		int robotCnt = 3;
		int dockCnt = 3;
		
		if (args.length == 0) {
			writers = new RuntimeLogWriters();
		} else {
			logPath = args[0];
			writers = new RuntimeLogWriters(logPath);
			
			int i = 1;
			SIMULATION_DURATION = Integer.parseInt(args[i]);
			System.out.println(String.format("%s = %d", "SIMULATION_DURATION", SIMULATION_DURATION));
			i++;
			
			robotCnt = Integer.parseInt(args[i]);
			if(robotCnt < 1 || robotCnt > Configuration.ROBOT_PARAMS.length){
				System.out.println(String.format("Invalid number of robots: %d", robotCnt));
				return;
			}
			System.out.println(String.format("Number of robots = %d", robotCnt));
			i++;
			
			dockCnt = Integer.parseInt(args[i]);
			if(dockCnt < 1 || dockCnt > Configuration.DOCK_NAMES.length){
				System.out.println(String.format("Invalid number of docks: %d", dockCnt));
				return;
			}
			System.out.println(String.format("Number of docks = %d", dockCnt));
			i++;
			
			DIRT_DETECTION_FAILURE_ON = Boolean.parseBoolean(args[i]);
			System.out.println(String.format("%s = %s", "DIRT_DETECTION_FAILURE_ON", DIRT_DETECTION_FAILURE_ON));
			i++;
			if(DIRT_DETECTION_FAILURE_ON){
				DIRT_DETECTION_FAILURE_TIME = Long.parseLong(args[i]);
				System.out.println(String.format("%s = %s", "DIRT_DETECTION_FAILURE_TIME", DIRT_DETECTION_FAILURE_TIME));
				i++;
				
				CORRELATION_ON = Boolean.parseBoolean(args[i]);
				System.out.println(String.format("%s = %s", "CORRELATION_ON", CORRELATION_ON));
				i++;
			}
			DOCK_FAILURE_ON = Boolean.parseBoolean(args[i]);
			System.out.println(String.format("%s = %s", "DOCK_FAILURE_ON", DOCK_FAILURE_ON));
			i++;
			if(DOCK_FAILURE_ON){
				DOCK_FAILURE_TIME = Long.parseLong(args[i]);
				System.out.println(String.format("%s = %s", "DOCK_FAILURE_TIME", DOCK_FAILURE_TIME));
				i++;
				
				ROLE_REMOVAL_ON = Boolean.parseBoolean(args[i]);
				System.out.println(String.format("%s = %s", "ROLE_REMOVAL_ON", ROLE_REMOVAL_ON));
				i++;
			}
			NON_DETERMINISM_ON = Boolean.parseBoolean(args[i]);
			System.out.println(String.format("%s = %s", "NON_DETERMINISM_ON", NON_DETERMINISM_ON));
			i++;
			if(NON_DETERMINISM_ON){
				NON_DET_INIT_PROBABILITY = Double.parseDouble(args[i]);
				System.out.println(String.format("%s = %s", "NON_DET_INIT_PROBABILITY", NON_DET_INIT_PROBABILITY));
				i++;
				NON_DET_PROBABILITY_STEP = Double.parseDouble(args[i]);
				NonDetModeSwitchAnnealState.NON_DETERMINISTIC_STEP = NON_DET_PROBABILITY_STEP;
				System.out.println(String.format("%s = %s", "NON_DET_PROBABILITY_STEP", NON_DET_PROBABILITY_STEP));
				i++;
				if(args.length > i) {
					NON_DET_START_TIME = Long.parseLong(args[i]);
					i++;
				}
				System.out.println(String.format("%s = %s", "NON_DET_START_TIME", NON_DET_START_TIME));
				if(args.length > i) {
					NON_DET_END_TIME = Long.parseLong(args[i]);
				} else {
					NON_DET_END_TIME = SIMULATION_DURATION;
				}
				System.out.println(String.format("%s = %s", "NON_DET_END_TIME", NON_DET_END_TIME));
			}
		}
		
		VisualizationSettings.createConfigFile();
		DirtinessMap.outputToFile(VisualizationSettings.MAP_FILE);

		final List<DEECoNode> nodesInSimulation = new ArrayList<DEECoNode>();
		final SimulationTimer simulationTimer = new DiscreteEventTimer();
		AnnealingParams.timer = simulationTimer; // HACK: rather provide deeco node to the search  engine

		// create main application container
		final DEECoSimulation simulation = new DEECoSimulation(simulationTimer);
		simulation.addPlugin(new SimpleBroadcastDevice(0, 0, SimpleBroadcastDevice.DEFAULT_RANGE, 128));
		simulation.addPlugin(Network.class);
		simulation.addPlugin(DefaultKnowledgePublisher.class);
		simulation.addPlugin(KnowledgeInsertingStrategy.class);
		simulation.addPlugin(new ModeSwitchingPlugin().withPeriod(50));
		simulation.addPlugin(new PositionPlugin(0, 0));

		if(CORRELATION_ON || NON_DETERMINISM_ON){
			simulation.addPlugin(new AdaptationPlugin().withPeriod(10000));
		}
		
		// Prepare adaptation plugins
		List<DEECoPlugin> adaptPlugins = new ArrayList<>();
		if (CORRELATION_ON) {
			// create correlation plugin
			CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation)
					.withVerbosity(true).withDumping(false).withGeneratedEnsemblesLogging(false);
			adaptPlugins.add(correlationPlugin);
		}
		if(NON_DETERMINISM_ON && !enableMultipleDEECoNodes){
			NonDeterministicModeSwitchingPlugin nonDetPlugin =
					new NonDeterministicModeSwitchingPlugin(DirtinessDurationFitness.class)
					.startAt(NON_DET_START_TIME)
					.withStartingNondetermoinism(NON_DET_INIT_PROBABILITY)
					.withVerbosity(false);
			adaptPlugins.add(nonDetPlugin);
		}

		// Create node -1 (default node)
		DEECoNode defaultNode;
		if(adaptPlugins.size() > 0) {
			defaultNode = simulation.createNode(-1, writers, adaptPlugins.toArray(new DEECoPlugin[]{}));
		} else {
			defaultNode = simulation.createNode(-1, writers);
		}
		nodesInSimulation.add(defaultNode);

		// Deploy environment
		Environment environment = new Environment(ENVIRONMENT_NAME, WITH_SEED, ENVIRONMENT_SEED);
		defaultNode.deployComponent(environment);

		// Deploy docking stations
		for(int i : IntStream.range(0, dockCnt).toArray()){
			Dock d = new Dock(DOCK_NAMES[i], DirtinessMap.randomNode(environment.random), defaultNode.getRuntimeLogger());
			defaultNode.deployComponent(d);
		}
		defaultNode.deployEnsemble(DockingEnsemble.class);
		defaultNode.deployEnsemble(CleaningPlanEnsemble.class);

		// Deploy robots
		deployRobots(IntStream.range(0, robotCnt).toArray(), simulation, defaultNode, nodesInSimulation, writers);
		
		// Start the simulation
		System.out.println("Simulation Starts - writing to '" + logPath + "'");
		Log.i("Simulation Starts");
		simulation.start(SIMULATION_DURATION);
		Log.i("Simulation Finished");
		System.out.println("Simulation Finished - writen to '" + logPath + "'");
	}
	
	private static void deployRobots(int robots[], DEECoSimulation simulation,
			DEECoNode defaultNode, List<DEECoNode> nodesInSimulation,
			RuntimeLogWriters writers) throws Exception {
		for(int i : robots){
			if (enableMultipleDEECoNodes) {
				// Create node
				DEECoNode deeco;
				if(NON_DETERMINISM_ON){
					NonDeterministicModeSwitchingPlugin nonDetPlugin =
							new NonDeterministicModeSwitchingPlugin(DirtinessDurationFitness.class)
							.startAt(NON_DET_START_TIME)
							.withStartingNondetermoinism(NON_DET_INIT_PROBABILITY)
							.withVerbosity(false);
					deeco = simulation.createNode(i, writers, nonDetPlugin);
				} else {
					deeco = simulation.createNode(i, writers);
				}
				nodesInSimulation.add(deeco);
	
				// Deploy robot
				deeco.deployComponent(Configuration.createRobot(i, deeco.getRuntimeLogger()));
	
				// Deploy ensembles on node
				deeco.deployEnsemble(DockingEnsemble.class);
				deeco.deployEnsemble(CleaningPlanEnsemble.class);
			} else {
				// Deploy robot
				defaultNode.deployComponent(Configuration.createRobot(i, defaultNode.getRuntimeLogger()));
			}
		}
	}

}

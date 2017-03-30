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
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_COUNT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK_NAMES;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_NAME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_SEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.LOG_DIR;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MODE_SWITCH_PROPS_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROBOT_COUNT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROLE_REMOVAL_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.SIMULATION_DURATION;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.WARM_UP_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.WITH_SEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.TRANSITION_PROBABILITY;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.TRANSITION_PRIORITY;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_TRAINING;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_TRAIN_FROM;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_TRAIN_TO;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_TRAINING_OUTPUT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationUtility;
import cz.cuni.mff.d3s.jdeeco.adaptation.componentIsolation.ComponentIsolationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.CorrelationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDeterministicModeSwitchingPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitchprops.ModeSwitchPropsPlugin;
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
		Configuration.override(args);

		// Configure Logs
		RuntimeLogWriters writers;
		if (LOG_DIR == null) {
			writers = new RuntimeLogWriters();
		} else {
			writers = new RuntimeLogWriters(LOG_DIR);
		}

		// Check robot and dock count
		if (ROBOT_COUNT < 1 || ROBOT_COUNT > Configuration.ROBOT_PARAMS.length) {
			Log.e(String.format("Invalid number of robots: %d", ROBOT_COUNT));
			return;
		}
		if (DOCK_COUNT < 1 || DOCK_COUNT > Configuration.DOCK_NAMES.length) {
			Log.e(String.format("Invalid number of docks: %d", DOCK_COUNT));
			return;
		}


		VisualizationSettings.createConfigFile();
		DirtinessMap.outputToFile(VisualizationSettings.MAP_FILE);

		final Set<DEECoNode> nodesInSimulation = new HashSet<DEECoNode>();
		final SimulationTimer simulationTimer = new DiscreteEventTimer(-WARM_UP_TIME);
//		AnnealingParams.timer = simulationTimer; // HACK: rather provide deeco node to the search engine

		// create main application container
		final DEECoSimulation simulation = new DEECoSimulation(simulationTimer);
		simulation.addPlugin(new SimpleBroadcastDevice(0, 0, SimpleBroadcastDevice.DEFAULT_RANGE, 128));
		simulation.addPlugin(Network.class);
		simulation.addPlugin(DefaultKnowledgePublisher.class);
		simulation.addPlugin(KnowledgeInsertingStrategy.class);
		simulation.addPlugin(new ModeSwitchingPlugin().withPeriod(50));
		simulation.addPlugin(new PositionPlugin(0, 0));

		// Prepare adaptation plugins
		List<DEECoPlugin> adaptPlugins = new ArrayList<>();

		if (CORRELATION_ON) {
			// create correlation plugin
			CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation).withVerbosity(true)
					.withDumping(true);
			adaptPlugins.add(correlationPlugin);
		}
		if (ROLE_REMOVAL_ON) {
			ComponentIsolationPlugin roleRemovalPlugin = new ComponentIsolationPlugin(nodesInSimulation)
					.withVerbosity(true);
			adaptPlugins.add(roleRemovalPlugin);
		}
		if (NON_DETERMINISM_ON && !enableMultipleDEECoNodes) {
			//NonDetModeSwitchAnnealState.NON_DETERMINISTIC_STEP = NON_DET_PROBABILITY_STEP;
			Map<String, AdaptationUtility> utilities = new HashMap<>(); // TODO: make universal utility
			utilities.put(Configuration.ROBOT1_NAME, new DirtinessDurationFitness(simulationTimer));
			utilities.put(Configuration.ROBOT2_NAME, new DirtinessDurationFitness(simulationTimer));
			utilities.put(Configuration.ROBOT3_NAME, new DirtinessDurationFitness(simulationTimer));

			NonDeterministicModeSwitchingPlugin nonDetPlugin = new NonDeterministicModeSwitchingPlugin(utilities)
					.withVerbosity(true)
					.withTraining(NON_DETERMINISM_TRAINING)
					.withTransitionProbability(TRANSITION_PROBABILITY)
					.withTransitionPriority(TRANSITION_PRIORITY)
					.withTrainFrom(NON_DETERMINISM_TRAIN_FROM)
					.withTrainTo(NON_DETERMINISM_TRAIN_TO)
					.withTrainingOutput(NON_DETERMINISM_TRAINING_OUTPUT);
			adaptPlugins.add(nonDetPlugin);
		}
		if (MODE_SWITCH_PROPS_ON && !enableMultipleDEECoNodes) {
			Map<String, AdaptationUtility> utilities = new HashMap<>();
			utilities.put(Configuration.ROBOT1_NAME, new DirtinessDurationFitness(simulationTimer));
			utilities.put(Configuration.ROBOT2_NAME, new DirtinessDurationFitness(simulationTimer));
			utilities.put(Configuration.ROBOT3_NAME, new DirtinessDurationFitness(simulationTimer));

			ModeSwitchPropsPlugin mspPlugin = new ModeSwitchPropsPlugin(nodesInSimulation, utilities)
					.withVerbosity(true);
			adaptPlugins.add(mspPlugin);
		}

		// Create node -1 (default node)
		DEECoNode defaultNode;
		if (adaptPlugins.size() > 0) {
			adaptPlugins.add(new AdaptationPlugin().withPeriod(10000));
			defaultNode = simulation.createNode(-1, writers, adaptPlugins.toArray(new DEECoPlugin[] {}));
		} else {
			defaultNode = simulation.createNode(-1, writers);
		}
		nodesInSimulation.add(defaultNode);

		// Deploy environment
		Environment environment = new Environment(ENVIRONMENT_NAME, WITH_SEED, ENVIRONMENT_SEED);
		defaultNode.deployComponent(environment);

		// Deploy docking stations
		for (int i : IntStream.range(0, DOCK_COUNT).toArray()) {
			Dock d = new Dock(DOCK_NAMES[i], DirtinessMap.randomNode(environment.random),
					defaultNode.getRuntimeLogger());
			defaultNode.deployComponent(d);
		}
		defaultNode.deployEnsemble(DockingEnsemble.class);
		defaultNode.deployEnsemble(CleaningPlanEnsemble.class);

		// Deploy robots
		deployRobots(IntStream.range(0, ROBOT_COUNT).toArray(), simulation, simulationTimer, defaultNode,
				nodesInSimulation, writers);

		// Start the simulation
		System.out.println("Simulation Starts - writing to '" + LOG_DIR + "'");
		Log.i("Simulation Starts");
		simulation.start(SIMULATION_DURATION);
		Log.i("Simulation Finished");
		System.out.println("Simulation Finished - writen to '" + LOG_DIR + "'");
	}

	private static void deployRobots(int robots[], DEECoSimulation simulation, SimulationTimer simulationTimer,
			DEECoNode defaultNode, Set<DEECoNode> nodesInSimulation, RuntimeLogWriters writers) throws Exception {
		for (int i : robots) {
			if (enableMultipleDEECoNodes) {
				// Create node
				DEECoNode deeco;
				NonDeterministicModeSwitchingPlugin nonDetPlugin = null;
				ModeSwitchPropsPlugin mspPlugin = null;
				if (NON_DETERMINISM_ON) {
//					NonDetModeSwitchAnnealState.NON_DETERMINISTIC_STEP = NON_DET_PROBABILITY_STEP;
					Map<String, AdaptationUtility> utilities = new HashMap<>();
					utilities.put(Configuration.ROBOT1_NAME, new DirtinessDurationFitness(simulationTimer));
					utilities.put(Configuration.ROBOT2_NAME, new DirtinessDurationFitness(simulationTimer));
					utilities.put(Configuration.ROBOT3_NAME, new DirtinessDurationFitness(simulationTimer));

					nonDetPlugin = new NonDeterministicModeSwitchingPlugin(utilities)
							.withVerbosity(true)
							.withTraining(NON_DETERMINISM_TRAINING)
							.withTransitionProbability(TRANSITION_PROBABILITY)
							.withTransitionPriority(TRANSITION_PRIORITY)
							.withTrainFrom(NON_DETERMINISM_TRAIN_FROM)
							.withTrainTo(NON_DETERMINISM_TRAIN_TO)
							.withTrainingOutput(NON_DETERMINISM_TRAINING_OUTPUT);
				}
				if (MODE_SWITCH_PROPS_ON) {
					Map<String, AdaptationUtility> utilities = new HashMap<>();
					utilities.put(Configuration.ROBOT1_NAME, new DirtinessDurationFitness(simulationTimer));
					utilities.put(Configuration.ROBOT2_NAME, new DirtinessDurationFitness(simulationTimer));
					utilities.put(Configuration.ROBOT3_NAME, new DirtinessDurationFitness(simulationTimer));

					mspPlugin = new ModeSwitchPropsPlugin(nodesInSimulation, utilities).withVerbosity(true);
				}
				if (nonDetPlugin != null && mspPlugin != null) {
					deeco = simulation.createNode(i, writers, nonDetPlugin, mspPlugin);
				} else if (nonDetPlugin != null) {
					deeco = simulation.createNode(i, writers, nonDetPlugin);
				} else if (mspPlugin != null) {
					deeco = simulation.createNode(i, writers, mspPlugin);
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

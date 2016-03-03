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
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK1_NAME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DOCK2_NAME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_NAME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ENVIRONMENT_SEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DETERMINISM_ON;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_START_TIME;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_INIT_PROBABILITY;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_EVAL_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.NON_DET_RECONF_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.SIMULATION_DURATION;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.WITH_SEED;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogWriters;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.CorrelationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDeterministicModeSwitchingPlugin;
import cz.cuni.mff.d3s.jdeeco.modes.ModeSwitchingPlugin;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;
import cz.cuni.mff.d3s.jdeeco.ua.component.Dock;
import cz.cuni.mff.d3s.jdeeco.ua.component.Environment;
import cz.cuni.mff.d3s.jdeeco.ua.ensemble.DockingEnsemble;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.mode.adapt.AnnealingParams;
import cz.cuni.mff.d3s.jdeeco.ua.mode.adapt.DirtinessDurationEval;
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

		String logPath = "STANDARD";
		RuntimeLogWriters writers;
		if (args.length == 0) {
			writers = new RuntimeLogWriters();
		} else {
			logPath = args[0];
			writers = new RuntimeLogWriters(logPath);
			
			Configuration.CORRELATION_ON = Boolean.parseBoolean(args[1]);
			Configuration.ROLE_REMOVAL_ON = Boolean.parseBoolean(args[2]);
			Configuration.DIRT_DETECTION_FAILURE_ON = Boolean.parseBoolean(args[3]);
			Configuration.DOCK_FAILURE_ON = Boolean.parseBoolean(args[4]);
			
			Configuration.NON_DETERMINISM_ON = Boolean.parseBoolean(args[5]);
			if(Configuration.NON_DETERMINISM_ON){
				// These arguments expected only if non-determinism is on
				Configuration.NON_DET_INIT_PROBABILITY = Double.parseDouble(args[6]);
				Configuration.NON_DET_START_TIME = Long.parseLong(args[7]);
				Configuration.NON_DET_END_TIME = Long.parseLong(args[8]);
				Configuration.NON_DET_EVAL_PERIOD = Long.parseLong(args[9]);
				Configuration.NON_DET_RECONF_PERIOD = Long.parseLong(args[10]);
			}
		}

		// Prepare adaptation plugins
		List<DEECoPlugin> adaptPlugins = new ArrayList<>();
		if (CORRELATION_ON) {
			// create correlation plugin
			CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation)
					.withVerbosity(false).withDumping(false).withGeneratedEnsemblesLogging(false);
			adaptPlugins.add(correlationPlugin);
		}
		if(NON_DETERMINISM_ON && !enableMultipleDEECoNodes){
			NonDeterministicModeSwitchingPlugin nonDetPlugin =
					new NonDeterministicModeSwitchingPlugin(DirtinessDurationEval.class)
					.startAt(NON_DET_START_TIME)
					.withStartingNondetermoinism(NON_DET_INIT_PROBABILITY)
					.withEvalPeriod(NON_DET_EVAL_PERIOD)
					.withReconfigPeriod(NON_DET_RECONF_PERIOD)
					.withVerbosity(true);
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
		Dock d1 = new Dock(DOCK1_NAME, DirtinessMap.randomNode(environment.random), defaultNode.getRuntimeLogger());
		defaultNode.deployComponent(d1);
		Dock d2 = new Dock(DOCK2_NAME, DirtinessMap.randomNode(environment.random), defaultNode.getRuntimeLogger());
		defaultNode.deployComponent(d2);
		defaultNode.deployEnsemble(DockingEnsemble.class);

		// Deploy robots
		deployRobots(new int[]{1, 2, 3}, simulation, defaultNode, nodesInSimulation, writers);
		
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
							new NonDeterministicModeSwitchingPlugin(DirtinessDurationEval.class)
							.startAt(NON_DET_START_TIME)
							.withStartingNondetermoinism(NON_DET_INIT_PROBABILITY)
							.withEvalPeriod(NON_DET_EVAL_PERIOD)
							.withReconfigPeriod(NON_DET_RECONF_PERIOD)
							.withVerbosity(true);
					deeco = simulation.createNode(i, writers, nonDetPlugin);
				} else {
					deeco = simulation.createNode(i, writers);
				}
				nodesInSimulation.add(deeco);
	
				// Deploy robot
				deeco.deployComponent(Configuration.createRobot(i, deeco.getRuntimeLogger()));
	
				// Deploy ensembles on node
				deeco.deployEnsemble(DockingEnsemble.class);
			} else {
				// Deploy robot
				defaultNode.deployComponent(Configuration.createRobot(i, defaultNode.getRuntimeLogger()));
			}
		}
	}

}

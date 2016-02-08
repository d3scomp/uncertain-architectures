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
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.WITH_SEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.SIMULATION_DURATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.modes.ModeSwitchingPlugin;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogWriters;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.CorrelationPlugin;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;
import cz.cuni.mff.d3s.jdeeco.ua.component.Dock;
import cz.cuni.mff.d3s.jdeeco.ua.component.Environment;
import cz.cuni.mff.d3s.jdeeco.ua.component.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.ensemble.DockingEnsemble;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
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
	public static void main(final String args[]) throws DEECoException, AnnotationProcessorException,
			InstantiationException, IllegalAccessException, IOException {
		Log.i("Preparing simulation");

		VisualizationSettings.createConfigFile();
		DirtinessMap.outputToFile(VisualizationSettings.MAP_FILE);

		final List<DEECoNode> nodesInSimulation = new ArrayList<DEECoNode>();
		final SimulationTimer simulationTimer = new DiscreteEventTimer();

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
			
			Configuration.PROBABILITY = Double.parseDouble(args[1]);
			Configuration.CORRELATION_ON = Boolean.parseBoolean(args[2]);
			Configuration.ROLE_REMOVAL_ON = Boolean.parseBoolean(args[3]);
			Configuration.DIRT_DETECTION_FAILURE_ON = Boolean.parseBoolean(args[4]);
			Configuration.DOCK_FAILURE_ON = Boolean.parseBoolean(args[5]);
		}

		// Create node 1
		DEECoNode deeco1;
		if (CORRELATION_ON) {
			// create correlation plugin
			final CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation)
					.withVerbosity(false).withDumping(false);
			deeco1 = simulation.createNode(1, writers, correlationPlugin);
		} else {
			deeco1 = simulation.createNode(1, writers);
		}
		nodesInSimulation.add(deeco1);

		Environment environment = new Environment(ENVIRONMENT_NAME, WITH_SEED, ENVIRONMENT_SEED);
		deeco1.deployComponent(environment);

		// Deploy docking stations
		Dock d1 = new Dock(DOCK1_NAME, DirtinessMap.randomNode(environment.random), deeco1.getRuntimeLogger());
		deeco1.deployComponent(d1);
		Dock d2 = new Dock(DOCK2_NAME, DirtinessMap.randomNode(environment.random), deeco1.getRuntimeLogger());
		deeco1.deployComponent(d2);

		// Deploy robot 1
		Robot r1 = Configuration.createRobot1(deeco1.getRuntimeLogger());
		deeco1.deployComponent(r1);

		// Deploy ensembles on node 1
		deeco1.deployEnsemble(DockingEnsemble.class);

		if (enableMultipleDEECoNodes) {
			// Create node 2
			DEECoNode deeco2 = simulation.createNode(2, writers);
			nodesInSimulation.add(deeco2);

			// Deploy robot 2
			deeco2.deployComponent(Configuration.createRobot2(deeco2.getRuntimeLogger()));

			// Deploy ensembles on node 2
			deeco2.deployEnsemble(DockingEnsemble.class);
		} else {
			// Deploy robot 2
			deeco1.deployComponent(Configuration.createRobot2(deeco1.getRuntimeLogger()));
		}

		if (enableMultipleDEECoNodes) {
			// Create node 3
			DEECoNode deeco3 = simulation.createNode(3, writers);
			nodesInSimulation.add(deeco3);

			// Deploy robot 3
			deeco3.deployComponent(Configuration.createRobot3(deeco3.getRuntimeLogger()));

			// Deploy ensembles on node 3
			deeco3.deployEnsemble(DockingEnsemble.class);
		} else {
			// Deploy robot 3
			deeco1.deployComponent(Configuration.createRobot3(deeco1.getRuntimeLogger()));
		}

		deeco1.deployComponent(Configuration.createRobot4(deeco1.getRuntimeLogger()));
		deeco1.deployComponent(Configuration.createRobot5(deeco1.getRuntimeLogger()));
		
		// Start the simulation
		System.out.println("Simulation Starts - writing to '" + logPath + "'");
		Log.i("Simulation Starts");
		simulation.start(SIMULATION_DURATION);
		Log.i("Simulation Finished");
		System.out.println("Simulation Finished - writen to '" + logPath + "'");
	}

}

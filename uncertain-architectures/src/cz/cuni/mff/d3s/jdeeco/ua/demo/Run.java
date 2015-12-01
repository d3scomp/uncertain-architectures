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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.modes.ModeSwitchingPlugin;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.CorrelationPlugin;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.KnowledgeMetadataHolder;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.DifferenceMetric;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.Metric;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionMetric;
import cz.cuni.mff.d3s.jdeeco.ua.visualization.VisualizationSettings;

/**
 * This class contains main for centralized run.
 */
public class Run {

	/** End of the simulation in milliseconds. */
	static private final long SIMULATION_END = 500_000;

	static final boolean enableMetaAdaptation = false;

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
	public static void main(final String args[])
			throws DEECoException, AnnotationProcessorException, InstantiationException, IllegalAccessException,
			IOException {
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

		DEECoNode deeco1;
		//final DEECoNode deeco3;
		if (enableMetaAdaptation) {
			// Meta-adaptation enabled
			// create correlation plugin
		    registerMetadataForFields();
			final CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation);
			//deeco3 = simulation.createNode(3, correlationPlugin);
			deeco1 = simulation.createNode(1, correlationPlugin);
			// Deploy the data aggregation ensemble
			//deeco3.deployEnsemble(RobotDataAggregation.class);
			deeco1.deployEnsemble(RobotDataAggregation.class);
		} else {
			//deeco3 = simulation.createNode(3);
			deeco1 = simulation.createNode(3);
		}

		
		// create nodes without adaptation
//		DEECoNode deeco1 = simulation.createNode(1);
		nodesInSimulation.add(deeco1);
		Robot r1 = Configuration.createRobot1();
		deeco1.deployComponent(r1);

		// Place docking stations
		r1.map.placeDockingStation(r1.map.getRandomNode(), deeco1.getRuntimeLogger());
		r1.map.placeDockingStation(r1.map.getRandomNode(), deeco1.getRuntimeLogger());

		//DEECoNode deeco2 = simulation.createNode(2);
		//nodesInSimulation.add(deeco2);
		//deeco2.deployComponent(Configuration.createRobot2());
		deeco1.deployComponent(Configuration.createRobot2());

		//final DEECoNode deeco3;
		if (enableMetaAdaptation) {
			// Meta-adaptation enabled
			// create correlation plugin
		    registerMetadataForFields();
			final CorrelationPlugin correlationPlugin = new CorrelationPlugin(nodesInSimulation);
			//deeco3 = simulation.createNode(3, correlationPlugin);
			deeco1 = simulation.createNode(1, correlationPlugin);
			// Deploy the data aggregation ensemble
			//deeco3.deployEnsemble(RobotDataAggregation.class);
			deeco1.deployEnsemble(RobotDataAggregation.class);
		} else {
			//deeco3 = simulation.createNode(3);
			deeco1 = simulation.createNode(3);
		}
		
		//nodesInSimulation.add(deeco3);
		// deploy components
		//deeco3.deployComponent(Configuration.createRobot3());
		deeco1.deployComponent(Configuration.createRobot3());

		// Assign the FF1 to the evaluation component

		Log.i("Simulation Starts");
		simulation.start(SIMULATION_END);
		Log.i("Simulation Finished");
	}

	/**
	 * Prepare and register metadata for fields.
	 */
	private static void registerMetadataForFields() {
		// TODO: revise this
		final String positionLabel = "position";
		final String batteryLabel = "batteryLevel";

		final int positionBoundary = 4;
		final int batteryBoundary = 20;

		final Metric simpleMetric = new DifferenceMetric();
		final Metric positionMetric = new PositionMetric();

		final double positionConfidence = 0.9;
		final double batteryConfidence = 0.9;

		KnowledgeMetadataHolder.setBoundAndMetric(positionLabel, positionBoundary, positionMetric, positionConfidence);
		KnowledgeMetadataHolder.setBoundAndMetric(batteryLabel, batteryBoundary, simpleMetric, batteryConfidence);
	}
}

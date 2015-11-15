package cz.cuni.mff.d3s.jdeeco.modes.example;

import org.junit.Test;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.modes.ModeSwitchingPlugin;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;

public class AcceptanceTest {

	@Test
	public void sampleRun() throws InstantiationException, IllegalAccessException, DEECoException, AnnotationProcessorException {

		SimulationTimer simulationTimer = new DiscreteEventTimer();
		
		/* create main application container */
		DEECoSimulation simulation = new DEECoSimulation(simulationTimer);
		simulation.addPlugin(new ModeSwitchingPlugin().withPeriod(50));
		
		/* deploy components and ensembles */
		DEECoNode deecoNode = simulation.createNode(1);
		deecoNode.deployComponent(new Robot());

		/* start simulation */
		simulation.start(2000);
	}

}

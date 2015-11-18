package cz.cuni.mff.d3s.jdeeco.modes.example.modechart;

import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;

public class RobotModeChartHolder extends ModeChartHolder {

	public RobotModeChartHolder(){
		ModeChartFactory factory = new ModeChartFactory();
		factory.withTransitionWithGuard(Searching.class, Cleaning.class, new CleaningGuard());
		factory.withInitialMode(Searching.class);
		
		modeChart = factory.create();
	}
}

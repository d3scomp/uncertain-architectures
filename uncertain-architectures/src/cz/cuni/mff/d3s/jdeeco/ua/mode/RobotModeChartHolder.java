package cz.cuni.mff.d3s.jdeeco.ua.mode;

import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;
import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.modes.TrueGuard;
import cz.cuni.mff.d3s.deeco.modes.ModeChartFactory;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;

public class RobotModeChartHolder extends ModeChartHolder {
	
	public RobotModeChartHolder(){
		ModeGuard<MetadataWrapper<Double>> batteryDrainedGuard = new ModeGuard<MetadataWrapper<Double>>() {
			@Override
			public boolean isSatisfied(MetadataWrapper<Double> knowledgeValue) {
				return knowledgeValue.getValue() < 0.2;
			}
			
			@Override
			public String getKnowledgeName() {
				return "batteryLevel";
			}
		};
		ModeGuard<Boolean> dockReachedGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDock";
			}
		};
		ModeGuard<MetadataWrapper<Double>> batteryChargedGuard = new ModeGuard<MetadataWrapper<Double>>() {
			@Override
			public boolean isSatisfied(MetadataWrapper<Double> knowledgeValue) {
				return knowledgeValue.getValue() > 0.95;
			}
			
			@Override
			public String getKnowledgeName() {
				return "batteryLevel";
			}
		};
		ModeGuard<Boolean> CleanGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDirt";
			}
		};
		ModeGuard<Boolean> SearchGuard = new ModeGuard<Boolean>() {
			@Override
			public boolean isSatisfied(Boolean knowledgeValue) {
				return !knowledgeValue;
			}
			
			@Override
			public String getKnowledgeName() {
				return "isOnDirt";
			}
		};
		
		ModeChartFactory factory = new ModeChartFactory();
		factory.withTransitionWithGuard(CleanMode.class, SearchMode.class, SearchGuard);
		factory.withTransition(SearchMode.class, CleanMode.class, CleanGuard, 0.9);
		factory.withTransition(SearchMode.class, DockingMode.class, batteryDrainedGuard, 0.9);
		factory.withTransition(SearchMode.class, DockingMode.class, new TrueGuard(), 0.1);
		factory.withTransitionWithGuard(DockingMode.class, ChargingMode.class, dockReachedGuard);
		factory.withTransitionWithGuard(ChargingMode.class, SearchMode.class, batteryChargedGuard);
		factory.withInitialMode(SearchMode.class);
		//currentMode = SearchMode.class;
		
		modeChart = factory.create();
	}
	
	/*@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		currentMode =  modeChart.switchMode(component, currentMode);
		return currentMode;
	}*/

}

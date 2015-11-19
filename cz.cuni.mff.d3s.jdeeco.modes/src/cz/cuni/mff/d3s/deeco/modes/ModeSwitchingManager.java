package cz.cuni.mff.d3s.deeco.modes;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.SystemComponent;
import cz.cuni.mff.d3s.deeco.model.runtime.api.ComponentInstance;
import cz.cuni.mff.d3s.deeco.model.runtime.api.ComponentProcess;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;

/**
 * Manages the mode-switching of all the components that are deployed on the
 * same deecoNode.
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
@Component
@SystemComponent
public class ModeSwitchingManager {

	/**
	 * just to pass the annotation processor check:
	 * "every DEECo component should have an ID"
	 */
	public String id;

	@Process
	@PeriodicScheduling(period = 1)
	/*
	 * this period is set here just to pass the annotation processor checks, the
	 * actual period is set from the IRMPlugin
	 */
	public static void reason(@In("id") String id) {

		ComponentInstance component = ProcessContext.getCurrentProcess().getComponentInstance();
		RuntimeMetadata runtime = (RuntimeMetadata) component.eContainer();

		for (ComponentInstance c : runtime.getComponentInstances()) {
			ModeChart modeChart = c.getModeChart();
			if (modeChart != null) {
				Class<? extends DEECoMode> currentMode = modeChart.switchMode();

				reconfigureArchitecture(c, currentMode);
			}
		}

	}

	private static void reconfigureArchitecture(ComponentInstance c, Class<? extends DEECoMode> currentMode) {
		for (ComponentProcess p : c.getComponentProcesses()) {
			
			List<DEECoMode> processModes = p.getModes();
			
			if (processModes != null && !processModes.isEmpty()) {
				
				List<Class> processModeClasses = new ArrayList<>();
				processModes.forEach(pm -> processModeClasses.add(pm.getClass()));
			
				if (processModeClasses.contains(currentMode)) {
					if (!p.isActive()) {
						p.setActive(true);
					}
				} else {
					if (p.isActive()) {
						p.setActive(false);
					}
				}
			}
		}
	}

}

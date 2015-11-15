package cz.cuni.mff.d3s.deeco.modes;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.DEECoMode;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.ModeChart;
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
			ModeChart modeChart = c.getModechart();
			if (modeChart != null) {
				Class<? extends DEECoMode> currentMode = modeChart.findSetAndReturnCurrentMode();

				reconfigureArchitecture(c, currentMode);
			}
		}

	}

	private static void reconfigureArchitecture(ComponentInstance c, Class<? extends DEECoMode> currentMode) {
		for (ComponentProcess p : c.getComponentProcesses()) {
			DEECoMode processMode = p.getMode();
			if (processMode != null) {
				if (processMode.getClass().equals(currentMode)) {
					p.setActive(true);
				} else {
					p.setActive(false);
				}
			}
		}
	}

}

/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
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
	public String id = "ModeSwitchingManager";

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
				Class<? extends DEECoMode> currentMode = modeChart.switchMode(c);

				reconfigureArchitecture(c, currentMode);
			}
		}

	}

	private static void reconfigureArchitecture(ComponentInstance c, Class<? extends DEECoMode> currentMode) {
		for (ComponentProcess p : c.getComponentProcesses()) {
			
			List<DEECoMode> processModes = p.getModes();
			
			if (processModes != null && !processModes.isEmpty()) {
				
				List<Class<? extends DEECoMode>> processModeClasses = new ArrayList<>();
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

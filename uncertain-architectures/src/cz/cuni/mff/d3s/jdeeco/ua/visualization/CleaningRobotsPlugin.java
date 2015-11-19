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
package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.util.Arrays;
import java.util.Collection;

import cz.cuni.mff.d3s.jdeeco.visualizer.extensions.MapSceneExtensionPoint;
import cz.cuni.mff.d3s.jdeeco.visualizer.extensions.OtherEventHandler;
import cz.cuni.mff.d3s.jdeeco.visualizer.extensions.VisualizerPlugin;

/**
 * Extends the functionality of the Visualizer according to the needs of the
 * "cleaning robots" scenario.
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
public class CleaningRobotsPlugin implements VisualizerPlugin {

	@Override
	public Collection<OtherEventHandler> getDynamicEventHandlers(Double startAt, Double endAt) {
		return Arrays.asList(
				new DirtinessEventHandler(startAt, endAt),
				new DockingStationEventHandler()
			);
	}

	@Override
	public Collection<MapSceneExtensionPoint> getMapSceneExtensionPoints() {
		return Arrays.asList(
				new DirtinessMapSceneExtension(),
				new DockingStationMapSceneExtension()				
			);
	}

}

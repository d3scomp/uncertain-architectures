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

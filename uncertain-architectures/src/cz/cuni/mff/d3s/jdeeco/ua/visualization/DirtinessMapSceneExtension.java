package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.jdeeco.visualizer.extensions.MapSceneExtensionPoint;
import cz.filipekt.jdcv.MapScene;
import cz.filipekt.jdcv.SceneImportHandler.ImageProvider;
import cz.filipekt.jdcv.SceneImportHandler.ShapeProvider;
import cz.filipekt.jdcv.events.Event;
import cz.filipekt.jdcv.network.MyNode;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Provides the capability to add key frames corresponding to dirtiness events
 * to the simulation.
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
public class DirtinessMapSceneExtension implements MapSceneExtensionPoint {
	
	@Override
	public Collection<KeyFrame> buildFrames(Map<String, List<Event>> otherEvents, MapScene mapScene) throws IOException {
		Collection<KeyFrame> res = new ArrayList<>();
		Map<String,Node> localDirtinessShapes = new HashMap<>();
		List<Event> dirtinessEvents = otherEvents.get(DirtinessEvent.DIRTINESS_EVENT_TYPE);
		for (Event e : dirtinessEvents) {
			DirtinessEvent de = (DirtinessEvent) e;
			double timeVal = mapScene.convertToVisualizationTime(de.getTime());
			Duration time = new Duration(timeVal);
			MyNode node = mapScene.getNodes().get(de.getNode()); 
			if (mapScene.getAdditionalResourcesPath() == null) {
				// additionalResourcesPath is not yet set, this is done by the acmescripts 
				return res; 
			}
			double intensity = de.getIntensity();
			ShapeProvider provider = new ImageProvider(false, mapScene.getAdditionalResourcesPath() + "dirt.png",
					mapScene.NODE_IMAGE_WIDTH, mapScene.NODE_IMAGE_HEIGHT, intensity);
			Node dirtinessShape = MapSceneExtensionHelper.generateNodeWithBackgroundImage(mapScene, provider, node);
			KeyValue kv = new KeyValue(dirtinessShape.visibleProperty(), Boolean.TRUE);
			KeyFrame kf = new KeyFrame(time, kv);
			String id = DirtinessEvent.DIRTINESS_EVENT_TYPE + de.getNode();
			localDirtinessShapes.put(id, dirtinessShape);
			res.add(kf);
		}
		Map<String,Node> otherShapes = mapScene.getOtherShapes();

		otherShapes.entrySet().removeIf(e-> e.getKey().startsWith(DirtinessEvent.DIRTINESS_EVENT_TYPE));
		otherShapes.putAll(localDirtinessShapes);
		
		for (Node dirtinessShape : otherShapes.values()){
			KeyValue kv = new KeyValue(dirtinessShape.visibleProperty(), Boolean.FALSE);
			KeyFrame kf = new KeyFrame(Duration.ZERO, kv);
			res.add(kf);
		}
		return res;
	} 
	
}

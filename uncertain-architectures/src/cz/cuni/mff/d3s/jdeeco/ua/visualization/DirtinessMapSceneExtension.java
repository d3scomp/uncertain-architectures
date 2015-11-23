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
			ShapeProvider provider;
			if (intensity == 0.0) {
				provider = new ImageProvider(false, mapScene.getAdditionalResourcesPath() + "tile.png", mapScene.NODE_IMAGE_WIDTH, mapScene.NODE_IMAGE_HEIGHT, 1);
			} else { 
				provider = new ImageProvider(false, mapScene.getAdditionalResourcesPath() + "dirt.png", mapScene.NODE_IMAGE_WIDTH, mapScene.NODE_IMAGE_HEIGHT, intensity);
			}
			Node dirtinessShape = MapSceneExtensionHelper.generateNodeWithBackgroundImage(mapScene, provider, node);
			KeyValue kv = new KeyValue(dirtinessShape.visibleProperty(), Boolean.TRUE);
			KeyFrame kf = new KeyFrame(time, kv);
			String id = DirtinessEvent.DIRTINESS_EVENT_TYPE + "_" + de.getNode() + "_" + de.getTime();
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

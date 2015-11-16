package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import cz.filipekt.jdcv.MapScene;
import cz.filipekt.jdcv.SceneImportHandler.ShapeProvider;
import cz.filipekt.jdcv.network.MyNode;
import cz.filipekt.jdcv.plugins.InfoPanel;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Helper class for common functionality across different map scene extensions
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
public class MapSceneExtensionHelper {

	public static Node generateNodeWithBackgroundImage(MapScene mapScene, ShapeProvider provider, MyNode node)
			throws IOException {

		double x = mapScene.getMatsimToVisual().transformX(node.getX());
		double y = mapScene.getMatsimToVisual().transformY(node.getY());
		Node shape = provider.getNewShape();

		if (shape != null) {
			shape.setTranslateX(x);
			shape.setTranslateY(y);
		}

		final Map<String, String> data = new LinkedHashMap<>();
		data.put("Node ID", node.getId());
		data.put("x-coordinate", node.getX() + "");
		data.put("y-coordinate", node.getY() + "");

		shape.setOnMouseEntered(null);
		shape.setOnMouseExited(null);
		shape.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				InfoPanel.getInstance().setInfo("Node selected:", data);
			}
		});
		return shape;
	}

}

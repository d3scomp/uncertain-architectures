package cz.cuni.mff.d3s.jdeeco.modes.example.modechart;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.DEECoMode;
import cz.cuni.mff.d3s.deeco.annotations.ModeChart;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.model.runtime.api.PathNodeField;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.modes.example.DirtySpot;

/**
 * The mode chart that governs the mode-switching of the Robot component.
 *  
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 * 
 * @see cz.cuni.mff.d3s.jdeeco.modes.example.Robot
 */
public class RobotModeChart extends ModeChart {

	public RobotModeChart() {
		currentMode = Searching.class;
	}
	@Override
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode() {
		
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		System.out.println("CurrentMode: " + currentMode + " at time " + currentTime);
		
		if (currentMode.equals(Searching.class)) {
			
			@SuppressWarnings("unchecked")
			List<DirtySpot> dirtySpots = (List<DirtySpot>) getValue("dirtySpots");
			
			if (!dirtySpots.isEmpty()) {
				System.out.println("##switching to: Cleaning at time " + currentTime);
				
				currentMode = Cleaning.class;
			}
		} 

		return currentMode;
	}
	
	private Object getValue(String knowledge) {
		KnowledgePath path = RuntimeMetadataFactoryExt.eINSTANCE.createKnowledgePath();
		PathNodeField pNode = RuntimeMetadataFactoryExt.eINSTANCE.createPathNodeField();
		pNode.setName(knowledge);
		path.getNodes().add(pNode);
		ArrayList<KnowledgePath> paths = new ArrayList<>();
		paths.add(path);
		try {
			return component.getKnowledgeManager().get(paths).getValue(path);
		} catch (KnowledgeNotFoundException e) {
			Log.e("Couldn't find knowledge " + knowledge + " in component " + component);
			return null;
		}
	}

}
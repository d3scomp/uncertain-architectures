package cz.cuni.mff.d3s.jdeeco.modes.example.modechart;

import java.util.List;

import cz.cuni.mff.d3s.deeco.modes.ModeGuard;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.modes.example.DirtySpot;

public class CleaningGuard implements ModeGuard<List<DirtySpot>> {

	@Override
	public String getKnowledgeName() {
		return "dirtySpots";
	}

	@Override
	public boolean isSatisfied(List<DirtySpot> knowledgeValue) {
		if (!knowledgeValue.isEmpty()) {
			long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
			System.out.println("##switching to: Cleaning at time " + currentTime);
			return true;
		}
		return false;
	}

}

package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

public class DirtinessRecord extends RuntimeLogRecord {

	public DirtinessRecord(String id) {
		super(id, new HashMap<>());
	}
	
	public void setIntensity(double intensity){
		recordValues.put("intensity", intensity);
	}
	
	public void setNode(Node node){
		recordValues.put("node", node.getId());
	}
	
}

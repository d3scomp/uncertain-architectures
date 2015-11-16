package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DockingStationRecord extends RuntimeLogRecord {

	public DockingStationRecord(String id) {
		super(id, new HashMap<>());
	}
	
	public void setNode(Node node){
		recordValues.put("node", node.getId());
	}
}

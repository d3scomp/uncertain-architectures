package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

public abstract class LinkRecord extends RuntimeLogRecord {

	public LinkRecord(String id) {
		super(id, new HashMap<>());
	}
	public void setLink(Link link){
		recordValues.put("link", link.getId());
	}
	
	public void setVehicle(String vehicle){
		recordValues.put("vehicle", vehicle);
	}

}

package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import cz.filipekt.jdcv.events.Event;

/**
 * Models an event of type "dirtiness", which appears in the JDEECo
 * event log (runtimeData.xml)
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 *
 */
public class DirtinessEvent implements Event {

	/**
	 * A point in time when a node changes dirtiness level (became more
	 * dirty/less dirty/clean)
	 */
	private final double time;

	/**
	 * ID of node which changed dirtiness level (became more dirty/less
	 * dirty/clean)
	 */
	private String node;
	
	public static String DIRTINESS_EVENT_TYPE = "dirtiness";

	/**
	 * The level of dirtiness captured in the event. Max level is 1.0 (very
	 * dirty), min is 0.0 (clean)
	 */
	private double intensity;

	public DirtinessEvent(double time) {
		this.time = time;
	}

	@Override
	public String getType() {
		return DIRTINESS_EVENT_TYPE;
	}

	@Override
	public double getTime() {
		return time;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	@Override
	public String toString(){
		return "Dirtiness at node " + node + " changed to intensity " + intensity + " at time " + time;
	}
}

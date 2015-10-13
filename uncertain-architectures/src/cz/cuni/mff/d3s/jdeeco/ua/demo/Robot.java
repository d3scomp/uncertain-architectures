package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.Deque;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.RobotHelper.*;

@Component
public class Robot {

	///////////////////////////////////////////////////////////////////////////
	//     KNOWLEDGE                                                         //
	///////////////////////////////////////////////////////////////////////////

	/** Mandatory id field. */
	public String id;

	/** Battery level. */
	public MetadataWrapper<Double> batteryLevel;

	/** Position in corridor coordinate system. */
	public MetadataWrapper<PositionKnowledge> position;

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Only constructor.
	 * @param id component id
	 */
	public Robot(final String id) {
		this.id = id;
		batteryLevel = new MetadataWrapper<>(Environment.getInitialBattery(id));
		position = new MetadataWrapper<>(Environment.getInitialPosition(id));
	}

	@Process
	@PeriodicScheduling(period=1000)
	public static void determineBatteryLevel(
			@In("id") String id,
			@InOut("batteryLevel") ParamHolder<MetadataWrapper<Double>> batteryLevel
	) {

		if (batteryLevel.value.isOperational()) {
			batteryLevel.value.setValue(Environment.getBatteryLevel(id), currentTime());
		}
		resetBatteryStateIfNeeded(batteryLevel.value.getValue());
	}

	@Process
	@PeriodicScheduling(period = Environment.INITIAL_POSITION_PERIOD)
	public static void determinePosition(
		@In("id") String id,
		@InOut("position") ParamHolder<MetadataWrapper<PositionKnowledge>> position
	) {
		final double inacc = computeCurrentInaccuracy(position.value);
		final Deque<Double> history = getInaccuracyHistory();
		if (history.size() >= POSION_STATE_HISTORY) {
			history.removeFirst();
		}
		history.add(inacc);
		if (position.value.isOperational()) {
			position.value.setValue(Environment.getPosition(id), currentTime());
		}
	}

}

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

	/** Environment temperature. */
	public MetadataWrapper<Double> temperature;

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
		temperature = new MetadataWrapper<>(0.0);
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

	public static boolean determineBatteryLevelSatisfaction(
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel) {
		return currentTime() - batteryLevel.getTimestamp() < TOO_OLD;
	}

	public static double determineBatteryLevelFitness(
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel) {
		final boolean satisfied = determineBatteryLevelSatisfaction(batteryLevel);
		return satisfied ? 1.0 : 0.0;
	}

	public static boolean batteryDrainageSatisfaction(
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel) {
		final double bl = batteryLevel.getValue();
		final double fitness = batteryDrainageSatisfactionInternal(bl);
		return fitness > SATISFACTION_BOUND;
	}

	public static double batteryDrainageFitness(
			@In("batteryLevel") MetadataWrapper<Double> batteryLevel) {
		final double bl = batteryLevel.getValue();
		final double fitness = batteryDrainageSatisfactionInternal(bl);
		return fitness;
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

	public static boolean determinePositionSatisfaction(
			@In("position") MetadataWrapper<PositionKnowledge> position) {
		return currentTime() - position.getTimestamp() < TOO_OLD;
	}

	public static double determinePositionFitness(
			@In("position") MetadataWrapper<PositionKnowledge> position) {
		final long time = currentTime();
		final boolean recent = time - position.getTimestamp() < TOO_OLD;
		return recent ? 1.0 : 0.0;
	}

	public static boolean positionAccuracySatisfaction(
			/*@AssumptionParameter(name = "bound", defaultValue = Environment.FF_POS_INAC_BOUND,
			maxValue = Environment.FF_POS_INAC_BOUND_MAX, minValue = Environment.FF_POS_INAC_BOUND_MIN, scope = Scope.COMPONENT,
			initialDirection = Direction.UP)*/
			double bound) {
		resetPositionStateIfNeeded(bound);
		int bad = 0;
		for (Double i : getInaccuracyHistory()) {
			if (i > bound) {
				++bad;
			}
		}
		return bad == 0;
	}

	public static double positionAccuracyFitness(
			/*@AssumptionParameter(name = "bound", defaultValue = Environment.FF_POS_INAC_BOUND,
			maxValue = Environment.FF_POS_INAC_BOUND_MAX, minValue = Environment.FF_POS_INAC_BOUND_MIN, scope = Scope.COMPONENT,
			initialDirection = Direction.UP)*/
			double bound) {
		int posBad = 0;
		int posOk = 0;
		int inacc = 0;
		for (Double i : getInaccuracyHistory()) {
			inacc += i;
			if (i > bound) {
				++posBad;
			} else {
				++posOk;
			}
		}
		double result;
		if (posBad > 0) {
			final double ratio = (1.0 * posOk) / (posOk + posBad);
			result = SATISFACTION_BOUND * ratio;
		} else if (posOk + posBad == 0) {
			result = 1.0;
		} else {
			final double ratio = 1.0 * inacc / ((posOk + posBad) * bound);
			result = (1.0 - SATISFACTION_BOUND) * ratio + SATISFACTION_BOUND;
		}
		return result;
	}

	@Process
	@PeriodicScheduling(period=1250, order = 2)
	public static void determineTemperature(
		@In("id") String id,
		@InOut("temperature") ParamHolder<MetadataWrapper<Double>> temperature
	) {
		if (temperature.value.isOperational()) {
			temperature.value.setValue(0.0, currentTime());
		} else {
			System.out.println("Temperature sensor not operational!");
		}
	}

	public static boolean determineTemperatureSatisfaction(
			@In("temperature") MetadataWrapper<Double> temperature) {
		return currentTime() - temperature.getTimestamp() < TOO_OLD;
	}

	public static double determineTemperatureFitness(
			@In("id") String id,
			@In("temperature") MetadataWrapper<Double> temperature) {
		long heldTemperatureTime = temperature.getTimestamp();
		long currentTime = currentTime();
		long delta = currentTime - heldTemperatureTime;
		double oldnessTreshold = 15000.0;

		double fitness = 1 - Math.min(1, (delta / oldnessTreshold));
		return fitness;
	}
}

package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleNoise;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionNoise;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;

/**
 * This whole component is a kind of hack.
 * It serves as sensors to FightFighter components.
 */
public class Environment {

	/** Checking position drains this much energy from battery. */
	static private final int GPS_ENERGY_COST = 4;

	/** FF1's GPS loses precision at this time. */
	static private final int GPS_BREAK_TIME = 50_000;

	/** FF1's thermometer dies at this time. */
	static private final int THERMO_DEAD_TIME = 150_000;

	/** Maximal distance between group members. */
	static final int MAX_GROUP_DISTANCE = 4;

	/** Returned when battery is too low to provide GPS readings. */
	static public final PositionKnowledge BAD_POSITION = new PositionKnowledge(Double.NaN, Double.NaN, Double.POSITIVE_INFINITY);

	/** Firefighter speed in m/s. */
	static public final double FF_SPEED = 1;

	/** HeatMap square size in meters. */
	static public final double CORRIDOR_SIZE = 2.0;

	/**
	 * Movement of a firefighter in squares per ms.
	 * Also inaccuracy caused by regular firefighter movement.
	 */
	static public final double FF_MOVEMENT = FF_SPEED / 1000 / CORRIDOR_SIZE;

	/** Simulation tick in ms. */
	static public final long SIMULATION_PERIOD = 50;

	/** Initial period for determine position. */
	static public final long INITIAL_POSITION_PERIOD = 1250;

	static public final long ADAPTED_POSITION_PERIOD = 250;

	static public final long POSITION_ADAPTATION_STEP = INITIAL_POSITION_PERIOD - ADAPTED_POSITION_PERIOD;

	/** Inaccuracy in case of GPS malfunction. */
	static public final double BROKEN_GSP_INACURRACY = FF_MOVEMENT * 2.25 * INITIAL_POSITION_PERIOD;

	/** Initial value of inaccuracy assumption parameter. */
	static public final double FF_POS_INAC_BOUND = 1.5; //(BROKEN_GSP_INACURRACY +  INITIAL_POSITION_PERIOD * FF_MOVEMENT) * 0.95;

	/** Maximal value of inaccuracy assumption parameter. */
	static public final double FF_POS_INAC_BOUND_MAX = 1.9; //FF_POS_INAC_BOUND * 1.25;

	/** Minimal value of inaccuracy assumption parameter. */
	static public final double FF_POS_INAC_BOUND_MIN = 1.1; //FF_POS_INAC_BOUND * 0.75;

	/** Firefighters' initial battery level. */
	static public final Double INITIAL_BATTERY_LEVEL = 1.0 * RobotHelper.TARGET_DURABILITY / ADAPTED_POSITION_PERIOD * GPS_ENERGY_COST;

	/** RNG. */
	static private final Random RANDOM = new Random(246811);
	
}

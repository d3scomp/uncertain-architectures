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

	/** Firefighter leading the group. */
	static public final String FF_LEADER_ID = "FF1";

	/** Firefighter following in the group. */
	static public final String FF_FOLLOWER_ID = "FF2";

	/** Firefighter with this id goes left at start. */
	static public final String LONELY_FF_ID = "FF3";

	/** Default Location for firefighters not contained in INITIAL_LOCATIONS. */
	static private final Position DEFAULT_LOCATION = new Position(0, 0);

	/** Firefighters' initial location. */
	static private final Map<String, Position> INITIAL_LOCATIONS = ((Supplier<Map<String, Position>>) () -> {
		final Map<String, Position> result = new HashMap<>();
		result.put(FF_LEADER_ID, new Position(11, 0));
		result.put(FF_FOLLOWER_ID, new Position(38, 2));
		result.put(LONELY_FF_ID, new Position(11, 0));
		return Collections.unmodifiableMap(result);
	}).get();

	/** Inaccuracy of fully operational gps sensor. */
	static private final double GPS_INACCURACY = 0.0;

	/** Default Position for firefighters not contained in INITIAL_LOCATIONS. */
	static private final PositionKnowledge DEFAULT_POSITION =
			new PositionKnowledge(DEFAULT_LOCATION, GPS_INACCURACY);

	/** Firefighters' initial position. */
	static private final Map<String, PositionKnowledge> INITIAL_POSITIONS =
			INITIAL_LOCATIONS.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
				return new PositionKnowledge(e.getValue(), GPS_INACCURACY);
			}));

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
	
	/** Filter for position. */
	static private Map<String, PositionNoise> positionNoise;

	/** Filter for position if GPS is broken. */
	static private Map<String, PositionNoise> brokenGPSInaccuracy;

	/** Filter for battery level. */
	static private Map<String, DoubleNoise> batteryNoise;

	static {
		PositionNoise pn1 = new PositionNoise("leader pos", 0.0, 0.1);
		PositionNoise pn2 = new PositionNoise("follow pos", 0.0, 0.1);
		PositionNoise pn3 = new PositionNoise("lonely pos", 0.0, 0.1);
		
		
		positionNoise = new HashMap<>();
		positionNoise.put(FF_LEADER_ID, pn1);
		positionNoise.put(FF_FOLLOWER_ID, pn2);
		positionNoise.put(LONELY_FF_ID, pn3);
		
		brokenGPSInaccuracy = new HashMap<>();
		brokenGPSInaccuracy.put(FF_LEADER_ID, new PositionNoise("leader iac", 0, 0.5, pn1));
		brokenGPSInaccuracy.put(FF_FOLLOWER_ID, new PositionNoise("follow iac", 0, 0.5, pn2));
		brokenGPSInaccuracy.put(LONELY_FF_ID, new PositionNoise("lonely iac", 0, 0.5, pn3));
		
		batteryNoise = new HashMap<>();
		batteryNoise.put(FF_LEADER_ID, new DoubleNoise("leader bat", 0.0, 1.0));
		batteryNoise.put(FF_FOLLOWER_ID, new DoubleNoise("follow bat", 0.0, 1.0));
		batteryNoise.put(LONELY_FF_ID, new DoubleNoise("lonely bat", 0.0, 1.0));
	
	}
	
}

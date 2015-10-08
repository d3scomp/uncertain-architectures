package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.SystemComponent;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleNoise;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionNoise;
import cz.cuni.mff.d3s.jdeeco.ua.map.Position;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionMetric;

/**
 * This whole component is a kind of hack.
 * It serves as sensors to FightFighter components.
 */
@Component
@SystemComponent
public class Environment {

	@Local
	public static PrintWriter positionWriter;


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

	/** Filter for temperature. */
	static private Map<String, DoubleNoise> temperatureNoise;

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
		
		temperatureNoise = new HashMap<>();
		temperatureNoise.put(FF_LEADER_ID, new DoubleNoise("leader tmp", 0.0, 2));
		temperatureNoise.put(FF_FOLLOWER_ID, new DoubleNoise("follow tmp", 0.0, 2));
		temperatureNoise.put(LONELY_FF_ID, new DoubleNoise("lonely tmp", 0.0, 2));
	}
	
	/////////////////////
	//ENVIRONMENT STATE//
	/////////////////////

	/** Firefighters state. */
	static private Map<String, FireFighterState> firefighters =
			INITIAL_LOCATIONS.keySet().stream().collect(Collectors.toMap(Function.identity(), id -> {
				return new FireFighterState(id);
			}));

	/**
	 * Returns Firefighter's states.
	 * Creates new map if needed.
	 * @return map with firefighters map
	 */
	static protected Map<String, FireFighterState> getFirefighters() {
		return firefighters;
	}

	/**
	 * Returns FireFighterState for given firefighter.
	 * Creates new state if needed.
	 * @param ffId firefighter id
	 * @return FireFighterState for given firefighter
	 */
	static protected FireFighterState getFirefighter(final String ffId) {
		final Map<String, FireFighterState> firefighters = getFirefighters();
		FireFighterState ff = firefighters.get(ffId);
		if (ff == null) { //should not happen, but just to be sure
			ff = new FireFighterState(ffId);
			firefighters.put(ffId, ff);
		}
		return ff;
	}

	static public Double getInitialBattery(final String id) {
		return batteryNoise.get(id).apply(INITIAL_BATTERY_LEVEL);
	}

	static public PositionKnowledge getInitialPosition(final String id) {
		final PositionKnowledge pos = INITIAL_POSITIONS.get(id);
		final Position noised = positionNoise.get(id).apply(pos != null ? pos : DEFAULT_POSITION);
		return new PositionKnowledge(noised, GPS_INACCURACY);
	}

	/**
	 * Returns location of the firefighter with the given id.
	 * @param ffId firefighter id
	 * @return firefighter's location
	 */
	static Position getLocation(final String ffId) {
		return getFirefighter(ffId).location;
	}

	/**
	 * Returns position of given firefighter or NaN with insufficient energy.
	 * Drains energy from the battery! If not enough energy left,
	 * Integer.MIN_VALUE returned.
	 * @param ffId firefighter id
	 * @return position of given firefighter or NaN with insufficient energy
	 */
	static public PositionKnowledge getPosition(final String ffId) {
		final FireFighterState ff = getFirefighter(ffId);
		ff.batteryLevel -= GPS_ENERGY_COST;
		if (ff.batteryLevel <= 0.0) {
			ff.batteryLevel = 0;
			return BAD_POSITION;
		} else {
			if (ffId.equals(FF_LEADER_ID)
					&& ProcessContext.getTimeProvider().getCurrentMilliseconds() >= GPS_BREAK_TIME) {
				final Position position = brokenGPSInaccuracy.get(ffId).apply(ff.location);
				return new PositionKnowledge(position, BROKEN_GSP_INACURRACY);
			} else {
				final Position position = positionNoise.get(ffId).apply(ff.location);
				return new PositionKnowledge(position, GPS_INACCURACY);
			}
		}
	}

	/**
	 * Returns battery level of given firefighter.
	 * @param ffId firefighter id
	 * @return battery level of given firefighter
	 */
	static public double getRealBatteryLevel(final String ffId) {
		return getFirefighter(ffId).batteryLevel;
	}

	/**
	 * Returns battery level of given firefighter.
	 * @param ffId firefighter id
	 * @return battery level of given firefighter
	 */
	static public double getBatteryLevel(final String ffId) {
		return batteryNoise.get(ffId).apply(getFirefighter(ffId).batteryLevel);
	}

	/** Environment component id. Not used, but mandatory. */
	public String id;

	/**
	 * Computes movement for given firefighter in one simulation tick.
	 * @param ffId firefighter id
	 * @param ff firefighter state
	 * @return movement for given firefighter
	 */
	static private double computeMovement(final String ffId, final FireFighterState ff) {
		return FF_MOVEMENT * SIMULATION_PERIOD;
	}

	@Process
	@PeriodicScheduling(period=SIMULATION_PERIOD, order = 1)
	static public void simulation(
			@In("id") String id) {
		final Map<String, FireFighterState> firefighters = getFirefighters();
		for (final String ffId : firefighters.keySet()) {
			final FireFighterState ff = getFirefighter(ffId);

			// TODO: implement movement

			System.out.println("TIME: " + ProcessContext.getTimeProvider().getCurrentMilliseconds());
			System.out.println(ffId + " batteryLevel = " + ff.batteryLevel);
			System.out.println(ffId + " position = " + ff.location);
			// TODO: print dirtiness System.out.println(ffId + " temperature = " + DirtinessMap.temperature(ff.Position));
		}
		final FireFighterState leader = getFirefighter(FF_LEADER_ID);
		final FireFighterState follower = getFirefighter(FF_FOLLOWER_ID);
		if (leader != null && follower != null) {
			System.out.println("#########################################");
			System.out.println("LEADER POS: " + leader.location + "(" + leader.location.x + ", " + leader.location.y + ")");
			System.out.println("FOLLOW POS: " + follower.location  + "(" + follower.location.x + ", " + follower.location.y + ")");
			System.out.println("DISTANCE  : " + PositionMetric.distance(leader.location, follower.location));
		}
	}

	/**
	 * Simple holder of data related to firefighter's state.
	 */
	protected static class FireFighterState {

		/** Firefighter's location. */
		protected Position location;

		/** Firefighter's battery level. */
		protected double batteryLevel = INITIAL_BATTERY_LEVEL;

		/** Target where the firefighter moves. */
		protected Position target = new Position(0, 0);

		/** Plan how to reach the target. It contains list of corridor indices. */
		protected Deque<Integer> plan = new ArrayDeque<>();

		/**
		 * Only constructor.
		 * @param ffId firefighter id
		 */
		public FireFighterState(final String ffId) {
			location = INITIAL_LOCATIONS.get(ffId).clone();
			// TODO: preparePlan(this);
		}
	}
}

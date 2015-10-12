package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.Position;

/**
 * Creates the trajectory plan to visit the nearest tile that has the oldest time when visited lastly.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class NearestOldestTrajectoryPlanner implements TrajectoryPlanner {

	/**
	 * The minimum length of a plan.
	 */
	private final static int MIN_PLAN_LENGTH = 1;
	/**
	 * The default maximum length of a plan.
	 */
	private final static int MAX_PLAN_LENGTH = 5;

	/**
	 * The {@link DirtinessMap} to operate on.
	 */
	private DirtinessMap map;
	
	/**
	 * The maximum length of a plan.
	 */
	private final int maxPlanLength;
	
	
	/**
	 * Create a new instance of {@link NearestOldestTrajectoryPlanner} and associate
	 * the given {@link DirtinessMap} with it. Each robot is supposed to have its own
	 * {@link TrajectoryPlanner} because the planner holds information private to each
	 * robot.
	 *  
	 * @param map The {@link DirtinessMap} to operate on.
	 * @throws IllegalArgumentException Thrown if the map argument is null.
	 */
	public NearestOldestTrajectoryPlanner(DirtinessMap map) {
		if(map == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "map"));
		this.map = map;
		maxPlanLength = MAX_PLAN_LENGTH;
	}
	
	public NearestOldestTrajectoryPlanner(DirtinessMap map, int maxPlanLength) {
		if(map == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "map"));
		if(maxPlanLength < MIN_PLAN_LENGTH) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument needs to be greater or equal than %d",
				"maxPlanLength", MIN_PLAN_LENGTH));
		this.map = map;
		this.maxPlanLength = maxPlanLength;
	}
	
	@Override
	public void updateTrajectory(List<Position> currentPlan, List<Position> newPlan) {
		

	}

}

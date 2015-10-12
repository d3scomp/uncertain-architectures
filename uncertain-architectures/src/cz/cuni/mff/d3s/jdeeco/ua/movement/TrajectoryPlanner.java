package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.ua.map.Position;

/**
 * The interface for classes that provide planning of the robot trajectory.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public interface TrajectoryPlanner {
	
	/**
	 *	Update or create a route plan with the knowledge of the current plan.
	 * 
	 * @param currentPlan The Current route plan.
	 * @param newPlan The new or updated route plan.
	 */
	void updateTrajectory(List<Position> currentPlan, List<Position> newPlan);
}

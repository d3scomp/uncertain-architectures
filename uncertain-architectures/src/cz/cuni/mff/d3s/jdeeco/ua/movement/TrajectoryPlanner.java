package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.position.Position;

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
	 * @param plan The Current route plan to be updated.
	 */
	void updateTrajectory(List<Position> plan);
}

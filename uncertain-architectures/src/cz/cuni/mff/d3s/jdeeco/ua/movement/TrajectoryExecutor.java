package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.ua.demo.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.filipekt.jdcv.graph.Link;

/**
 * The interface for moving a robot on a planned trajectory.
 * There can be a different approaches to follow a trajectory.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public interface TrajectoryExecutor {

	/**
	 * Moves the robot within the period of its movement process.
	 * 
	 * @param plan The trajectory plan of the robot. if the next checkpoint
	 * 		 is reached, it is removed from the plan.
	 * @param position The current position of the robot.
	 * 
	 * @return The new position of the robot as it moved.
	 */
	void move(List<Link> plan, LinkPosition position);

	/**
	 * Associate the given {@link Robot} with this {@link TrajectoryExecutor}.
	 * 
	 * @param robot The robot associated with this {@link TrajectoryExecutor}.
	 */
	void setRobot(Robot robot);
}

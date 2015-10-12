package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.Position;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;

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
	 * @param position The in-out argument with the position of the robot.
	 */
	void move(List<Position> plan, MetadataWrapper<PositionKnowledge> position);
}

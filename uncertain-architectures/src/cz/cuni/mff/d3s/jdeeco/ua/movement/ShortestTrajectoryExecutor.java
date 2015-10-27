package cz.cuni.mff.d3s.jdeeco.ua.movement;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROBOT_SPEED;

import java.util.List;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;;

/**
 * Moves the robot on the shortest path to the next planned trajectory
 * checkpoint.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class ShortestTrajectoryExecutor implements TrajectoryExecutor {

	/**
	 * The map used to adjust the robot movement.
	 */
	private DirtinessMap map;
	
	private String robotId;
	
	private RuntimeLogger runtimeLogger;

	/**
	 * Associate the given {@link Robot} with this {@link TrajectoryExecutor}.
	 * 
	 * @param robot The robot associated with this {@link TrajectoryExecutor}.
	 *
	 * @throws IllegalArgumentException Thrown if the robot argument is null.
	 */
	@Override
	public void setRobot(Robot robot) {
		if (robot == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument cannot be null.", "robot"));
		if (robot.map == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument doesn't contain any map.", "robot"));
		this.map = robot.map;
		robotId = robot.id;
	}
	
	
	@Override
	public void setRuntimeLogger(RuntimeLogger runtimeLogger) {
		this.runtimeLogger = runtimeLogger;
	}

	/**
	 * Move the robot using it's maximum speed towards the next trajectory check point
	 * using the shortest path. If the checkpoint is reached it is removed from the plan.
	 * 
	 * @param plan The trajectory to follow.
	 * @param position The current position of the robot.
	 * 
	 * @return The new position of the robot as it moved.
	 * 
	 * @throws IllegalStateException Thrown if the {@link #map} field is not initialized.
	 */
	@Override
	public void move(List<Link> plan, LinkPosition position) {
		// NOTE: This strategy works only for rectangular maps, for more complex maps this needs to be revised.
		if(map == null) throw new IllegalStateException(String.format(
				"The \"%s\" field is not initialized.", "map"));
		
		if(plan.isEmpty()){
			// If there is no plan don't move
			return;
		}
		// Compute the distance between origin and destination
		final double destinationDistance = position.getRemainingDistance();
		// Compute the maximum distance the robot can travel in a single step
		final double maxStepDistance = (double) ROBOT_SPEED / (double) MOVE_PROCESS_PERIOD;
		// Compute the distance the robot will travel in this step
		final double stepDistance = Math.min(destinationDistance, maxStepDistance);
		// Move towards the next node
		position.move(stepDistance);
		// Check whether the robot already overcame the link
		if(position.isEndReached()){
			plan.remove(0);
			if(!plan.isEmpty()){
				position.startFrom(plan.get(0));
			}
		}
		// Update robots position in the centralized storage
		map.updateRobotsPosition(robotId, position);
	}

}

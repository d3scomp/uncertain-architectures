package cz.cuni.mff.d3s.jdeeco.ua.movement;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROBOT_SPEED;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.POSITION_ACCURACY;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.Position;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;;

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

	/**
	 * Create a new instance of {@link ShortestTrajectoryExecutor} and associate
	 * it with the given {@link DirtinessMap}.
	 *  
	 * @param map The {@link DirtinessMap} limiting the area to move in.
	 * @throws IllegalArgumentException Thrown if the map argument is null.
	 */
	public ShortestTrajectoryExecutor(DirtinessMap map) {
		if (map == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument cannot be null.", "map"));
		this.map = map;
	}

	/**
	 * Move the robot using it's maximum speed towards the next trajectory check point
	 * using the shortest path. If the checkpoint is reached it is removed from the plan.
	 * 
	 * @throws IllegalStateException Thrown if the {@link #map} field is not initialized.
	 */
	@Override
	public void move(List<Position> plan, MetadataWrapper<PositionKnowledge> position) {
		// NOTE: This strategy works only for rectangular maps, for more complex maps this needs to be revised.
		if(map == null) throw new IllegalStateException(String.format(
				"The \"%s\" field is not initialized.", "map"));
		
		if(plan.isEmpty()){
			// If there is no plan don't move
			return;
		} else {
			// Extract the destination of the robot
			final Position destination = plan.get(0);
			// Extract the current position of the robot
			final Position origin = position.getValue();
			// Compute the distance between origin and destination
			final double destinationDistance = euclidianDistance(destination, origin);
			// Compute the maximum distance the robot can travel in a single step
			final double maxStepDistance = (double) ROBOT_SPEED / (double) MOVE_PROCESS_PERIOD;
			// Compute the distance the robot will travel in this step
			final double stepDistance = Math.min(destinationDistance, maxStepDistance);
			// Compute the fraction of a distance to the destination that will be overcame in this step
			final double distanceFractionToDestination = destinationDistance / stepDistance;
			// Compute the distance that will be traveled on the X coordinate
			final double stepX = (destination.x - origin.x) * distanceFractionToDestination;
			// Compute the distance that will be traveled on the Y coordinate
			final double stepY = (destination.y - origin.y) * distanceFractionToDestination;
			
			// Compute the new position of the robot
			PositionKnowledge newPosition = new PositionKnowledge(
					origin.x + stepX, origin.y + stepY, 0); // TODO: trace inaccuracy here
			
			// Update the robot position
			position.setValue(newPosition, 0); // TODO: timestamp
			
			// If the checkpoint is reached remove it from the plan
			if(euclidianDistance(destination, newPosition) < POSITION_ACCURACY){
				plan.remove(0);
			}
		}
	}

	private double euclidianDistance(Position a, Position b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}

}

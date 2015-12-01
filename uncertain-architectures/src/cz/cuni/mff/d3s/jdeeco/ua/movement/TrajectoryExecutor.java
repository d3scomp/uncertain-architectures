/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.movement;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MOVE_PROCESS_PERIOD;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.ROBOT_SPEED;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.ua.demo.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;;

/**
 * Moves the robot on the shortest path to the next planned trajectory
 * checkpoint.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class TrajectoryExecutor {

	/**
	 * The map used to adjust the robot movement.
	 */
	private DirtinessMap map;
	
	private String robotId;

	/**
	 * Associate the given {@link Robot} with this {@link TrajectoryExecutor}.
	 * 
	 * @param robot The robot associated with this {@link TrajectoryExecutor}.
	 *
	 * @throws IllegalArgumentException Thrown if the robot argument is null.
	 */
	public void setRobot(Robot robot) {
		if (robot == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument cannot be null.", "robot"));
		if (robot.map == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument doesn't contain any map.", "robot"));
		this.map = robot.map;
		robotId = robot.id;
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
	public void move(List<Link> plan, LinkPosition position) {
		if(map == null) throw new IllegalStateException(String.format(
				"The \"%s\" field is not initialized.", "map"));
		
		if(plan.isEmpty()){
			// If there is no plan don't move
			return;
		}
		// Compute the distance between origin and destination
		final double destinationDistance = position.getRemainingDistance();
		// Compute the maximum distance the robot can travel in a single step
		final double maxStepDistance = (double) ROBOT_SPEED * (double) MOVE_PROCESS_PERIOD / 1000;
		// Compute the distance the robot will travel in this step
		final double stepDistance = Math.min(destinationDistance, maxStepDistance);
		// Move towards the next node
		position.move(stepDistance);
		// Check whether the robot already overcame the link
		if(position.isEndReached()){
			if(position.atNode() == plan.get(0).getTo()){
				plan.remove(0);
			}
			if(!plan.isEmpty()){
				// Check collisions
				Link nextLink = plan.get(0);
				Collection<LinkPosition> others = map.getOthersPosition(robotId);
				// Can't go where other goes or meet them between nodes
				if(isNodeCollision(nextLink.getTo(), others)
						|| isLinkCollision(nextLink, others)){
					// Go randomly or wait, clear plan
					nextLink = getDeflection(position.atNode(), others);
					plan.clear();
				}
				if(nextLink != null){
					position.startFrom(nextLink);
				}
			}
		}
		// Update robots position in the centralized storage
		map.updateRobotsPosition(robotId, position);
	}
	
	private boolean isNodeCollision(Node target, Collection<LinkPosition> others){
		for(LinkPosition otherPosition : others){
			if(otherPosition.getLink().getTo().equals(target)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isLinkCollision(Link link, Collection<LinkPosition> others){
		for(LinkPosition otherPosition : others){
			Link otherLink = otherPosition.getLink(); 
			if(link.getTo().equals(otherLink.getFrom())
					&& link.getFrom().equals(otherLink.getTo())){
				return true;
			}
		}
		return false;
	}
	
	private Link getDeflection(Node from, Collection<LinkPosition> others){
		Set<Link> choices = DirtinessMap.getNetwork().getLinksFrom(from);
		for(Link choice : choices){
			if(!isNodeCollision(choice.getTo(), others)
					&& !isLinkCollision(choice, others)){
				return choice;
			}
		}
		return null;
	}

}

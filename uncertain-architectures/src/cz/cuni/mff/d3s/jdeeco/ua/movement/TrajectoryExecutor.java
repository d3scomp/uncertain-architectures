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

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.ua.demo.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

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

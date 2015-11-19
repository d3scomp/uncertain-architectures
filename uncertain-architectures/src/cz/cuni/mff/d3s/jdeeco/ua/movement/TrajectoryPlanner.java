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
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

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
	void updateTrajectory(List<Link> plan);
	
	/**
	 * Associate the given {@link Robot} with this {@link TrajectoryPlanner}.
	 * 
	 * @param robot The robot associated with this {@link TrajectoryPlanner}.
	 */
	void setRobot(Robot robot);
}

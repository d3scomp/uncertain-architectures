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
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Robot;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Dijkstra;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * Creates the trajectory plan to visit the nearest tile that has the oldest time when visited lastly.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DockTrajectoryPlanner implements TrajectoryPlanner {

	/**
	 * The {@link DirtinessMap} to operate on.
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
	@Override
	public void setRobot(Robot robot) {
		if (robot == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument cannot be null.", "robot"));
		if (robot.map == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument doesn't contain any map.", "robot"));
		robotId = robot.id;
		this.map = robot.map;
	}	

	/**
	 * Create a new instance of {@link DockTrajectoryPlanner}.
	 * Each robot is supposed to have its own {@link TrajectoryPlanner} because
	 * the planner holds information private to each robot.
	 */
	public DockTrajectoryPlanner() {
	}
	
	/**
	 * Update or create a route plan with the knowledge of the current plan.
	 * 
	 * @param plan The Current route plan to be updated.
	 * 
	 * @throws IllegalArgumentException Thrown if the plan argument is null.
	 */
	@Override
	public void updateTrajectory(List<Link> plan) {
		if(plan == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "plan"));
		
		// Fill the plan
		if(plan.isEmpty()){
			LinkPosition robotPosition = map.getPosition(robotId);
			Set<Node> dockingNodes = map.getDockingStations();
			if(!dockingNodes.isEmpty()){
				// Visit the nearest docking station
				Node dock = getRandomDock(dockingNodes);
				
				List<Link> newPlan = Dijkstra.getShortestPath(map.getNetwork(),
						robotPosition.getLink().getTo(), dock);
				assert(newPlan != null);
				assert(!newPlan.isEmpty());
				plan.addAll(newPlan);
			}
			if(plan.isEmpty()){
				Log.e("Empty plan has been generated.");
				return;
			}
		}

	}
	
	private Node getRandomDock(Set<Node> dockingNodes){
		
		Random rand = new Random();// TODO: use centralized random with seed
		int end = rand.nextInt(dockingNodes.size());
		int index = 0;
		for(Node dock : dockingNodes){
			if(index == end){
				return dock;
			}
			index++;
		}
		// Should never reach this code
		assert(false);
		return null;
	}

}

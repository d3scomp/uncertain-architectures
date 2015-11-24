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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class SearchTrajectoryPlanner {

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
	public void setRobot(Robot robot) {
		if (robot == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument cannot be null.", "robot"));
		if (robot.map == null)
			throw new IllegalArgumentException(String.format("The \"%s\" argument doesn't contain any map.", "robot"));
		robotId = robot.id;
		this.map = robot.map;
	}	

	/**
	 * Create a new instance of {@link SearchTrajectoryPlanner}.
	 * Each robot is supposed to have its own {@link TrajectoryPlanner} because
	 * the planner holds information private to each robot.
	 */
	public SearchTrajectoryPlanner() {
	}
	
	/**
	 * Update or create a route plan with the knowledge of the current plan.
	 * 
	 * @param plan The Current route plan to be updated.
	 * 
	 * @throws IllegalArgumentException Thrown if the plan argument is null.
	 */
	public void updateTrajectory(List<Link> plan) {
		if(plan == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "plan"));
		
		// Fill the plan
		if(plan.isEmpty()){
			LinkPosition robotPosition = map.getPosition(robotId);
			Node lastNode = lastNodeInPlan(plan, robotPosition);
// TODO: check whether the visited nodes are update correctly
			// Visit the nearest unvisited node or the oldest visited node
			Node nodeToVisit = unvisitedNodes().isEmpty() 
								? getOldestNode(map.getVisitedNodes())
								: getClosestNode(lastNode, unvisitedNodes());
								
			if(nodeToVisit == null)
			{
				Log.e("The planning in " + this.getClass().getName() + " doesn't work!");
				return;
			}
			List<Link> newPlan = Dijkstra.getShortestPath(map.getNetwork(),
					lastNode , nodeToVisit);
			plan.addAll(newPlan);

			if(plan.isEmpty()){
				Log.w("Empty plan has been generated.");
				return;
			}
		}

	}

	/**
	 * Return the set of unvisited {@link Tile}s.
	 * 
	 * @return The set of unvisited {@link Tile}s.
	 */
	private Set<Node> unvisitedNodes(){
		// Performance optimization - Check the number of visited tiles first
		if(map.size() <= map.getVisitedNodes().size()){
			return Collections.emptySet();
		}
		Set<Node> nodes = new HashSet<>(map.getNetwork().getNodes());
		nodes.removeAll(map.getVisitedNodes().keySet());
		return nodes;
	}
	
	/**
	 * Returns the last {@link Tile} in the plan or robot position if the plan is empty.
	 * 
	 * @param plan The trajectory plan.
	 * 
	 * @return The last {@link Tile} in plan or the most recently visited
	 * {@link Tile} if the plan is empty.
	 */
	private Node lastNodeInPlan(List<Link> plan, LinkPosition robotPosition){
		if(!plan.isEmpty()){
			return plan.get(plan.size()-1).getTo();
		}
		return robotPosition.getLink().getTo();
	}

	/**
	 * @param visitedNodes
	 * @return
	 */
	private Node getOldestNode(Map<Node, Long> nodes) {
		if(nodes.size() == 0){
			return null;
		}
		
		Node oldest = null;
		long timestamp = 0; // NOTE: assuming that time cannot be negative
		for(Node n : nodes.keySet()){
			if(oldest == null){
				oldest = n;
				timestamp = nodes.get(n);
			} else {
				long newTimestamp = nodes.get(n); 
				if(newTimestamp < timestamp){
					oldest = n;
					timestamp = newTimestamp;
				}
			}
		}
		return oldest;
	}
		
	/**
	 * Randomly choose the closest {@link Tile} to visit.
	 * @param tile The {@link Tile} to be close to.
	 * @param rand Random number generator.
	 * @return The closest {@Tile} to visit from the given tile.
	 * @throws IllegalArgumentException Thrown if either tile of rand argument
	 * 			is null.
	 */
	private Node getClosestNode(Node node, Set<Node> nodes){
		if(node == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "node"));
		if(nodes == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "nodes"));

		if(nodes.isEmpty()){
			return null;
		}
		
		Set<Node> newReachableNodes = new HashSet<>();
		Set<Node> oldReachableNodes = new HashSet<>();
		newReachableNodes.add(node);
		
		// DFS search
		while(!oldReachableNodes.containsAll(newReachableNodes)){
			oldReachableNodes.addAll(newReachableNodes);
			// DFS step
			for(Node reachableNode : oldReachableNodes){
				for(Link successor : map.getNetwork().getLinksFrom(reachableNode)){
					if(nodes.contains(successor.getTo())){
						// If the successor node is in the set we want to reach return it
						return successor.getTo();
					}
					newReachableNodes.add(successor.getTo());
				}
			}
		}
		
		// If we are here it means that no Node from 'nodes' is reachable from 'node' 
		return null;
	}

}

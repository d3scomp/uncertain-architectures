package cz.cuni.mff.d3s.jdeeco.ua.movement;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_HEIGHT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_WIDTH;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;
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
public class NearestOldestTrajectoryPlanner implements TrajectoryPlanner {

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
	 * Create a new instance of {@link NearestOldestTrajectoryPlanner}.
	 * Each robot is supposed to have its own {@link TrajectoryPlanner} because
	 * the planner holds information private to each robot.
	 */
	public NearestOldestTrajectoryPlanner() {
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
			Set<Node> unvisitedNodes = unvisitedNodes();
			if(!unvisitedNodes.isEmpty()){
				// Visit the nearest unvisited tile
				Node lastNode = lastNodeInPlan(plan);
				Node nodeToVisit;
				if(lastNode == null){
					// return random tile if none was visited yet
					nodeToVisit = getRandomNode();
				} else {
					nodeToVisit = getClosestNode(lastNode);
				}
				assert(nodeToVisit != null);
				LinkPosition robotPosition = map.getPosition(robotId);
				List<Link> newPlan = Dijkstra.getShortestPath(map.getNetwork(),
						robotPosition.getLink().getTo() , nodeToVisit);
				assert(newPlan != null);
				assert(!newPlan.isEmpty());
				plan.addAll(newPlan);
			}
		}

	}
	
	private Node getRandomNode(){
		Random rand = Configuration.RANDOM;
		int end = rand.nextInt(MAP_WIDTH*MAP_HEIGHT);
		int index = 0;
		for(Node n : map.getNetwork().getNodes()){
			if(index == end){
				return n;
			}
			index++;
		}
		// Should never reach this code
		assert(false);
		return null;
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
	 * Returns the last {@link Tile} in plan or the most recently visited
	 * {@link Tile} if the plan is empty.
	 * 
	 * @param plan The trajectory plan.
	 * 
	 * @return The last {@link Tile} in plan or the most recently visited
	 * {@link Tile} if the plan is empty.
	 */
	private Node lastNodeInPlan(List<Link> plan){
		if(!plan.isEmpty()){
			return plan.get(plan.size()-1).getTo();
		}
		Node node = lastVisitedNode();
		return node;
	}
	
	/**
	 * Get the most recently visited {@link Tile}.
	 * 
	 * @return The most recently visited {@link Tile}.
	 */
	private Node lastVisitedNode(){
		if(map.getVisitedNodes().size() == 0){
			return null;
		}
		
		Node lastNode = null;
		long timestamp = 0;
		for(Node n : map.getVisitedNodes().keySet()){
			if(lastNode == null){
				lastNode = n;
				timestamp = map.getVisitedNodes().get(n);
			} else {
				long newTimestamp = map.getVisitedNodes().get(n); 
				if(timestamp < newTimestamp){
					lastNode = n;
					timestamp = newTimestamp;
				}
			}
		}
		return lastNode;
	}
	
	/**
	 * Randomly choose the closest {@link Tile} to visit.
	 * @param tile The {@link Tile} to be close to.
	 * @param rand Random number generator.
	 * @return The closest {@Tile} to visit from the given tile.
	 * @throws IllegalArgumentException Thrown if either tile of rand argument
	 * 			is null.
	 */
	private Node getClosestNode(Node node){
		if(node == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "node"));

		Random rand = Configuration.RANDOM;
		Set<Link> sucessors = map.getNetwork().getLinksFrom(node);
		int end = rand.nextInt(sucessors.size());
		int index = 0;
		for(Link sucessor : sucessors){
			if(index == end){
				return sucessor.getTo();
			}
			index++;
		}
		// Should never reach this code
		assert(false);
		return null;
	}

}

package cz.cuni.mff.d3s.jdeeco.ua.map;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_HEIGHT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_WIDTH;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Network;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;


/**
 * Environment heat map holder.
 */
public class DirtinessMap {

	/**
	 * This field stores the position of each robot for the purposes of
	 * easy collision avoidance implementation.
	 */
	private static final Map<String, LinkPosition> ROBOT_LOCATIONS = new HashMap<>();
	
	private final Map<Node, Double> dirtiness;
	
	/**
	 * The network representation of the map.
	 */
	private static final Network network;

	/**
	 * A set of timestamps of the last visit of individual tiles.
	 */
	private Map<Node, Long> visitedNodes;

	static{
		network = new Network();
		ArraySet<Node> nodes = new ArraySet<>(); // Hold the nodes in an array for a while
		// Create nodes
		for(int x = 0; x < MAP_WIDTH; x++){
			for(int y = 0; y < MAP_HEIGHT; y++){
				Node n = new Node(x, y);
				// Add the node to the network
				nodes.add(n);
				network.addNode(n);
			}
		}
		// Create links
		for(int x = 0; x < MAP_WIDTH; x++){
			for(int y = 0; y < MAP_HEIGHT; y++){
				Node n = getElement(nodes, x, y);
				// horizontal shift
				for(int h = -1; h <= 1; h++){
					// vertical shift
					for(int v = -1; v <= 1; v++){
						// Check neighbor is not out of bounds
						Node neighbor = getElement(nodes, x+h, y+v);
						if(neighbor != null){
							Link l = new Link(n, neighbor);
							// Add the link to the network
							network.addLink(l);
						}
					}
				}
			}
		}
	}
	
	private static <T> T getElement(ArraySet<T> elements, int x, int y){
		// Check the indices are in bounds
		if(x >= 0 
			&& x < MAP_WIDTH
			&& y >= 0
			&& y < MAP_HEIGHT){	
		return elements.get(y*MAP_WIDTH + x);
		}
		return null;
	}
	
	public DirtinessMap() {
		visitedNodes = new HashMap<>();
		dirtiness = new HashMap<>(MAP_WIDTH*MAP_HEIGHT);
		for(Node n : network.getNodes()){
			dirtiness.put(n, 0.0);
		}
	}
	
	public Network getNetwork(){
		return network;
	}
	
	/**
	 * Get the visited {@link Tile}s with the timestamps of the last visit.
	 * @return
	 */
	public Map<Node, Long> getVisitedNodes(){
		return visitedNodes;
	}
	
	/**
	 * Get the {@link Position} of the given {@link Tile}.
	 * 
	 * @param tile The {@link Tile} of which the position is required.
	 * 
	 * @return The {@link Position} of the given {@link Tile}.
	 */
	/*public Position getPosition(Tile tile){
		return new Position(tile.x * TILE_WIDTH + 0.5 * TILE_WIDTH,
							tile.y * TILE_WIDTH + 0.5 * TILE_WIDTH);
	}*/

	/**
	 * The size of the map as number of tiles it contains.
	 * @return The number of tiles in the map.
	 */
	public int size() {
		return MAP_WIDTH*MAP_HEIGHT;
	}
	
	/**
	 * Update the position of the specified robot in the centralized field.
	 * 
	 * @param robotId The ID of a robot whose position will be updated.
	 * @param position The updated position of the robot.
	 * 
	 * @throws IllegalArgumentException Thrown if either the robotId or the
	 * 			position argument is null. 
	 */
	public void updateRobotsPosition(String robotId, LinkPosition position){
		if(robotId == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "robotId"));
		if(position == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "position"));
		
		ROBOT_LOCATIONS.put(robotId, position);
	}
	
	/**
	 * Get the positions of robots other than the specified one. If the robotId
	 * argument is null no position is excluded from the result.
	 * 
	 * @param robotId The robot to exclude its position in the obtained collection.
	 * 			Can be null if no position is required to be excluded.
	 * @return The collection of positions of robots whose ID has not been specified.
	 */
	public Collection<LinkPosition> getOthersPosition(String robotId){
		Collection<LinkPosition> positions = ROBOT_LOCATIONS.values();
		if(robotId != null && ROBOT_LOCATIONS.containsKey(robotId)){
			LinkPosition excludedPosition = ROBOT_LOCATIONS.get(robotId);
			positions.remove(excludedPosition);
		}
		
		return positions;
	}
	
	public LinkPosition getPosition(String robotId){
		if(ROBOT_LOCATIONS.containsKey(robotId)){
			return ROBOT_LOCATIONS.get(robotId);
		}
		return null;
	}
}

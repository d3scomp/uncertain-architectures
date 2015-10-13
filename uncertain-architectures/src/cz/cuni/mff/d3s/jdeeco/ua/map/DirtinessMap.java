package cz.cuni.mff.d3s.jdeeco.ua.map;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_HEIGHT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_WIDTH;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.TILE_WIDTH;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Environment heat map holder.
 */
public class DirtinessMap {

	/**
	 * This field stores the position of each robot for the purposes of
	 * easy collision avoidance implementation.
	 */
	private static final Map<String, Position> ROBOT_LOCATIONS = new HashMap<>();
	
	/**
	 * A set of {@link Tile}s in the map.
	 */
	private final ArraySet<Tile> tiles;

	/**
	 * A set of timestamps of the last visit of individual tiles.
	 */
	private Map<Tile, Long> visitedTiles;

	/**
	 * Utility classes need no constructor.
	 */
	private DirtinessMap() {
		tiles = new ArraySet<>(MAP_WIDTH*MAP_HEIGHT);
		for(int x = 0; x < MAP_WIDTH; x++){
			for(int y = 0; y < MAP_HEIGHT; y++){
				tiles.add(new Tile(x, y));
			}
		}
		visitedTiles = new HashMap<>();
	}
	
	/**
	 * Get all the {@link Tile}s in the map.
	 * @return
	 */
	public Set<Tile> getTiles(){
		return Collections.unmodifiableSet(tiles);
	}
	
	/**
	 * Get the visited {@link Tile}s with the timestamps of the last visit.
	 * @return
	 */
	public Map<Tile, Long> getVisitedTiles(){
		return visitedTiles;
	}
	
	/**
	 * Returns a {@link Tile} at the given position.
	 * 
	 * @param position The position at which the {@link Tile} is required.
	 * 
	 * @return A {@link Tile} at the given position.
	 */
	public Tile getTile(Position position){
		int x = (int) Math.floor(position.x / TILE_WIDTH);
		int y = (int) Math.floor(position.y / TILE_WIDTH);
		
		return getTile(x, y);
	}

	/**
	 * Returns the specified {@link Tile}.
	 * 
	 * @param x The X coordinate of the {@link Tile}.
	 * @param y The Y coordinate of the {@link Tile}.
	 * 
	 * @return A {@link Tile} at the given indices.
	 */
	public Tile getTile(int x, int y){
		return tiles.get(y*MAP_WIDTH + x);
	}
	
	/**
	 * Get the {@link Position} of the given {@link Tile}.
	 * 
	 * @param tile The {@link Tile} of which the position is required.
	 * 
	 * @return The {@link Position} of the given {@link Tile}.
	 */
	public Position getPosition(Tile tile){
		return new Position(tile.x * TILE_WIDTH + 0.5 * TILE_WIDTH,
							tile.y * TILE_WIDTH + 0.5 * TILE_WIDTH);
	}

	/**
	 * The size of the map as number of tiles it contains.
	 * @return The number of tiles in the map.
	 */
	public int size() {
		return tiles.size();
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
	public void updateRobotsPosition(String robotId, Position position){
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
	public Collection<Position> getOthersPosition(String robotId){
		Collection<Position> positions = ROBOT_LOCATIONS.values();
		if(robotId != null && ROBOT_LOCATIONS.containsKey(robotId)){
			Position excludedPosition = ROBOT_LOCATIONS.get(robotId);
			positions.remove(excludedPosition);
		}
		
		return positions;
	}
}

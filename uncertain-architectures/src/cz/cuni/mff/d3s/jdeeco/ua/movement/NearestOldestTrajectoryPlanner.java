package cz.cuni.mff.d3s.jdeeco.ua.movement;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.Position;
import cz.cuni.mff.d3s.jdeeco.ua.map.Tile;

/**
 * Creates the trajectory plan to visit the nearest tile that has the oldest time when visited lastly.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class NearestOldestTrajectoryPlanner implements TrajectoryPlanner {

	/**
	 * The minimum length of a plan.
	 */
	private final static int MIN_PLAN_LENGTH = 1;
	/**
	 * The default maximum length of a plan.
	 */
	private final static int MAX_PLAN_LENGTH = 5;

	/**
	 * The {@link DirtinessMap} to operate on.
	 */
	private DirtinessMap map;
	
	/**
	 * The maximum length of a plan.
	 */
	private final int maxPlanLength;
	
	/**
	 * Create a new instance of {@link NearestOldestTrajectoryPlanner} and associate
	 * the given {@link DirtinessMap} with it. Each robot is supposed to have its own
	 * {@link TrajectoryPlanner} because the planner holds information private to each
	 * robot.
	 *  
	 * @param map The {@link DirtinessMap} to operate on.
	 * @throws IllegalArgumentException Thrown if the map argument is null.
	 */
	public NearestOldestTrajectoryPlanner(DirtinessMap map) {
		if(map == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "map"));
		this.map = map;
		maxPlanLength = MAX_PLAN_LENGTH;
	}

	/**
	 * Create a new instance of {@link NearestOldestTrajectoryPlanner} and associate
	 * the given {@link DirtinessMap} with it. Each robot is supposed to have its own
	 * {@link TrajectoryPlanner} because the planner holds information private to each
	 * robot.
	 *  
	 * @param map The {@link DirtinessMap} to operate on.
	 * @throws IllegalArgumentException Thrown if the map argument is null or
	 * 			if the maxPlanLength argument is less than {@value #MIN_PLAN_LENGTH}.
	 */
	public NearestOldestTrajectoryPlanner(DirtinessMap map, int maxPlanLength) {
		if(map == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "map"));
		if(maxPlanLength < MIN_PLAN_LENGTH) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument needs to be greater or equal than %d",
				"maxPlanLength", MIN_PLAN_LENGTH));
		this.map = map;
		this.maxPlanLength = maxPlanLength;
	}
	
	/**
	 * Update or create a route plan with the knowledge of the current plan.
	 * 
	 * @param plan The Current route plan to be updated.
	 * 
	 * @throws IllegalArgumentException Thrown if the plan argument is null.
	 */
	@Override
	public void updateTrajectory(List<Position> plan) {
		if(plan == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "plan"));

		Random rand = new Random();
		
		// Fill the plan
		while(plan.size() < maxPlanLength){
			Set<Tile> unvisitedTiles = unvisitedTiles();
			if(!unvisitedTiles.isEmpty()){
				// Visit the nearest unvisited tile
				Tile lastTile = lastTileInPlan(plan);
				Tile tileToVisit;
				if(lastTile == null){
					// return random tile if none was visited yet
					tileToVisit = map.getTile(rand.nextInt(Configuration.MAP_WIDTH),
									   rand.nextInt(Configuration.MAP_HEIGHT));
				} else {
					tileToVisit = getClosestTile(lastTile, rand);
				}
				plan.add(map.getPosition(tileToVisit));
			}
		}

	}
	
	/**
	 * Return the set of unvisited {@link Tile}s.
	 * 
	 * @return The set of unvisited {@link Tile}s.
	 */
	private Set<Tile> unvisitedTiles(){
		// Performance optimization - Check the number of visited tiles first
		if(map.size() <= map.getVisitedTiles().size()){
			return Collections.emptySet();
		}
		Set<Tile> tiles = new HashSet<>(map.getTiles());
		tiles.removeAll(map.getVisitedTiles().keySet());
		return tiles;
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
	private Tile lastTileInPlan(List<Position> plan){
		if(!plan.isEmpty()){
			return map.getTile(plan.get(plan.size()-1));
		}
		Tile tile = lastVisitedTile();
		return tile;
	}
	
	/**
	 * Get the most recently visited {@link Tile}.
	 * 
	 * @return The most recently visited {@link Tile}.
	 */
	private Tile lastVisitedTile(){
		if(map.getVisitedTiles().size() == 0){
			return null;
		}
		
		Tile lastTile = null;
		long timestamp = 0;
		for(Tile t : map.getVisitedTiles().keySet()){
			if(lastTile == null){
				lastTile = t;
				timestamp = map.getVisitedTiles().get(t);
			} else {
				long newTimestamp = map.getVisitedTiles().get(t); 
				if(timestamp < newTimestamp){
					lastTile = t;
					timestamp = newTimestamp;
				}
			}
		}
		return lastTile;
	}
	
	/**
	 * Randomly choose the closest {@link Tile} to visit.
	 * @param tile The {@link Tile} to be close to.
	 * @param rand Random number generator.
	 * @return The closest {@Tile} to visit from the given tile.
	 * @throws IllegalArgumentException Thrown if either tile of rand argument
	 * 			is null.
	 */
	private Tile getClosestTile(Tile tile, Random rand){
		if(tile == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "tile"));
		if(rand == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "rand"));
		
		Tile nextTile = null;
		switch(rand.nextInt(4)){
		case 0:
			if(tile.x+1 >= Configuration.MAP_WIDTH){
				nextTile = map.getTile(tile.x-1, tile.y);
			} else {
				nextTile = map.getTile(tile.x+1, tile.y);
			}
			break;
		case 1:
			if(tile.x-1 < 0){
				nextTile = map.getTile(tile.x+1, tile.y);
			} else {
				nextTile = map.getTile(tile.x-1, tile.y);
			}
			break;
		case 2:
			if(tile.y+1 >= Configuration.MAP_HEIGHT){
				nextTile = map.getTile(tile.x, tile.y-1);
			} else {
				nextTile = map.getTile(tile.x, tile.y+1);
			}
			break;
		case 3:
			if(tile.y-1 < 0){
				nextTile = map.getTile(tile.x, tile.y+1);
			} else {
				nextTile = map.getTile(tile.x, tile.y-1);
			}
			break;
		}
		
		return nextTile;
	}

}

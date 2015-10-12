package cz.cuni.mff.d3s.jdeeco.ua.map;

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.TILE_WIDTH;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Environment heat map holder.
 */
public class DirtinessMap {

	/** 
	 * Width of the map.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	public static final int MAP_WIDTH = 20;
	
	/**
	 * Heigt of the map.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	public static final int MAP_HEIGHT = 20;
	
	/**
	 * A set of {@link Tile}s in the map.
	 */
	private final ArraySet<Tile> tiles;
	
	/**
	 * The number of tiles in the map.
	 */
	public final int size = MAP_WIDTH * MAP_HEIGHT;

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
}

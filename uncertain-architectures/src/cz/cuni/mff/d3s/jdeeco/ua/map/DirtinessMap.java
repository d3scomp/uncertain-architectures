package cz.cuni.mff.d3s.jdeeco.ua.map;

import java.awt.Dimension;

/**
 * Environment heat map holder.
 */
public class DirtinessMap {

	/** Map dimensions.
	 * The dimensions are expressed as number of tiles.
	 * So far the map is represented only as a rectangle.
	 */
	static final Dimension MAP_DIMENSIONS = new Dimension(20, 20);

	/**
	 * Utility classes need no constructor.
	 */
	private DirtinessMap() {
		//nothing
	}
	
	// TODO: provide dynamic dirtiness
	// TODO: enable dirtiness to be cleaned
}

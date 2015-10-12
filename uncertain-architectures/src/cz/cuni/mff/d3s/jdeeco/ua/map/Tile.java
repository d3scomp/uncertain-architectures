package cz.cuni.mff.d3s.jdeeco.ua.map;

/**
 * This class represents a single tile in the {@link DirtinessMap}.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class Tile implements Comparable<Tile> {

	/**
	 * The minimum dirtiness value represents clean tile.
	 */
	public final static double MIN_DIRTINESS = 0;
	
	/**
	 * The maximum dirtiness value represents very dirty tile.
	 */
	public final static double MAX_DIRTINESS = 1;
	
	/**
	 * The X coordinate of the tile.
	 */
	public final int x;

	/**
	 * The Y coordinate of the tile.
	 */
	public final int y;

	/**
	 * The dirtiness level of the tile.
	 */
	private double dirtiness;
	
	/**
	 * Construct the {@link Tile} on the given coordinates.
	 * 
	 * @param x The X coordinate of the {@link Tile}.
	 * @param y The Y coordinate of the {@link Tile}. 
	 */
	public Tile(int x, int y){
		this.x = x;
		this.y = y;
		dirtiness = 0;
	}
	
	/**
	 * Set the dirtiness level of the {@link Tile} within the interval
	 * [{@value #MIN_DIRTINESS}, {@value #MAX_DIRTINESS}].
	 * 
	 * @param dirtiness The dirtiness level to be set.
	 * 
	 * @throws IllegalArgumentException Thrown if the dirtiness value is not
	 * within the interval [{@value #MIN_DIRTINESS}, {@value #MAX_DIRTINESS}].
	 */
	public void setDirtiness(double dirtiness){
		if(dirtiness < MIN_DIRTINESS || dirtiness > MAX_DIRTINESS)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument has to fit in the interval [%f, %f], "
					+ "but the provided value was %f.", "dirtiness",
					MIN_DIRTINESS, MAX_DIRTINESS, dirtiness));
		
		this.dirtiness = dirtiness;
	}
	
	/**
	 * Get the dirtiness level of the {@link Tile}.
	 * 
	 * @return The dirtiness level of the {@link Tile}.
	 */
	public double getDirtiness(){
		return dirtiness;
	}
	
	/**
	 * Compare the {@link Tile} to the given {@link Tile}.
	 * {@link Tile}s are compared based on theirs coordinates,
	 * the X coordinate is compared first, if the X coordinate values
	 * are equal the Y coordinate values are compared. 
	 */
	@Override
	public int compareTo(Tile other) {
		if(this.x < other.x){
			return -1;
		}
		if(this.x > other.x){
			return 1;
		}
		if(this.y < other.y){
			return -1;
		}
		if(this.y > other.y){
			return 1;
		}
		return 0;
	}
}

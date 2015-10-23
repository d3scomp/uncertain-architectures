package cz.cuni.mff.d3s.jdeeco.ua.filter;

/**
 * A filter for applying noise and inaccuracy to a data.
 * Filter is bound to a certain data type is it meant to
 * modify. 
 *  
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 * @param <T> The data type that can be filtered by the filter.
 */
public interface Filter<T> {
	
/**
 * Apply the noise defined by the filter instance to the
 * given data.
 * @param data The data to apply the filter at.
 * @return The given data with applied noise.
 */
public T applyNoise(final T data);
	
}

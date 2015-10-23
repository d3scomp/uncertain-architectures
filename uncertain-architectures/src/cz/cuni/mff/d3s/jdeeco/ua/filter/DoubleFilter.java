package cz.cuni.mff.d3s.jdeeco.ua.filter;

/**
 * Represents noise filter for the temperature reading sensors.
 * The level of noise is adjusted by the mean and deviation of the
 * normal distribution.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DoubleFilter extends GaussianFilter<Double> {

	public DoubleFilter(double mean, double deviation) {
		super(mean,deviation);
	}

	@Override
	public Double applyNoise(final Double data) {
		double r = noise.getNext();
		return (data + r);
	}

}

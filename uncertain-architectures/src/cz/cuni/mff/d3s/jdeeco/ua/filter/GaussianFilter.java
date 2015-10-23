package cz.cuni.mff.d3s.jdeeco.ua.filter;

public abstract class GaussianFilter<T> implements Filter<T> {

	/**
	 * Generator of random numbers with the normal distribution.
	 */
	protected NormalDistribution noise;
	
	/**
	 * Create new instance of {@link DoubleFilter} with the normal
	 * distribution with specified mean and standard deviation.
	 * 
	 * @param mean The mean of the normal distribution.
	 * @param deviation The standard deviation of the normal distribution.
	 */
	public GaussianFilter(double mean, double deviation) {
		noise = new NormalDistribution(mean, deviation);
	}
	
	public GaussianFilter() {
		noise = new NormalDistribution(0, 1);
	}
	
	/**
	 * Get the deviation of this noise filter.
	 * @return The deviation of this noise filter.
	 */
	public double getDeviation(){
		return noise.getDeviation();
	}
	
}

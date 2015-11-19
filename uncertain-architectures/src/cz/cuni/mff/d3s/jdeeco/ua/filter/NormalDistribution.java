/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.filter;

import java.util.Random;

/**
 * A generator of random numbers with normal distribution.
 * Mean and standard deviation can be specified. The seed
 * of the random is fixed and depends on the order in which
 * the {@link NormalDistribution} objects are instantiated.
 * 
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class NormalDistribution {

	/**
	 * The initial seed for the {@link NormalDistribution} object.
	 * The seed is changed after new instance of the {@link NormalDistribution}
	 * is created.
	 */
	private static long seed = 75698;
	
	/**
	 * The mean of the normal distribution.
	 */
	private double mean;
	/**
	 * The deviation of the normal distribution.
	 */
	private double deviation;
	/**
	 * {@link Random} number generator.
	 */
	private Random rand;
	
	/**
	 * Create new instance of {@link NormalDistribution} with the mean 0.0
	 * and the standard deviation 1.0.
	 */
	public NormalDistribution() {
		init(0.0, 1.0);
	}
	
	/**
	 * Create new instance of {@link NormalDistribution} with the given mean
	 * and the given standard deviation.
	 * 
	 * @param mean The mean of the normal distribution.
	 * @param deviation The standard deviation of the normal distribution.
	 */
	public NormalDistribution(double mean, double deviation) {
		init(mean, deviation);
	}
	
	/**
	 * Assign the mean, deviation and {@link Random} number generator.
	 * Change the seed for the next instance.
	 * 
	 * @param mean The mean of the normal distribution.
	 * @param deviation The standard deviation of the normal distribution.
	 */
	private void init(double mean, double deviation) {
		setDistribution(mean, deviation);
		
		rand = new Random(seed);
		seed += 498349;
	}

	/**
	 * Assign the mean and standard deviation.
	 * 
	 * @param mean The mean of the normal distribution.
	 * @param deviation The standard deviation of the normal distribution.
	 */
	public void setDistribution(double mean, double deviation) {
		this.mean = mean;
		this.deviation = deviation;
	}
	
	/**
	 * Get the deviation of this normal distribution.
	 * @return The deviation of this normal distribution.
	 */
	public double getDeviation(){
		return deviation;
	}
	
	/**
	 * Get next random number with normal distribution with predefined
	 * mean and standard deviation.
	 * @return Next random number with normal distribution with predefined
	 * mean and standard deviation.
	 */
	protected double getNext() {
		return mean + rand.nextGaussian()*deviation;
	}

}

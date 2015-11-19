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

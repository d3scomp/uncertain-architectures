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

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

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class PositionFilter extends GaussianFilter<Position> {

	public PositionFilter(double mean, double deviation) {
		super(mean,deviation);
	}

	@Override
	public Position applyNoise(final Position data) {
		// horizontal noise
		double h = noise.getNext();
		// vertical noise
		double v = noise.getNext();
		
		return new Position(data.x + h, data.y + v);
	}
}

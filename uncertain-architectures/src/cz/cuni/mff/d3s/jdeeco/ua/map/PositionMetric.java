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
package cz.cuni.mff.d3s.jdeeco.ua.map;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.Metric;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class PositionMetric implements Metric {

	static public double distance(final Position pos1, final Position pos2) {
		return Math.sqrt(Math.pow(pos1.x - pos2.x, 2) + Math.pow(pos1.y - pos2.y, 2));
	}

	@Override
	public double distance(Object value1, Object value2) {
		if(!(value1 instanceof Position) || !(value2 instanceof Position))
			throw new IllegalArgumentException("Can't compute a distance of anything else than Positions.");

		final Position pos1 = (Position) value1;
		final Position pos2 = (Position) value2;
		return distance(pos1, pos2);
	}
}

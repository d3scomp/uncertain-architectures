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
package cz.cuni.mff.d3s.jdeeco.ua.metric;

import java.util.List;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.Metric;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Dijkstra;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

public class PositionMetric implements Metric {

	static public double distance(final LinkPosition pos1, final LinkPosition pos2) {
		// Find the shortest path
		List<Link> path = Dijkstra.getShortestPath(DirtinessMap.getNetwork(),
				pos1.getLink().getTo(),
				pos1.getLink().getTo());
		// Initialize the distance
		double dist = pos1.getRemainingDistance();
		// Compute the distance of the path
		for(Link l : path){
			dist += l.getLength();
		}
		
		return dist;
	}

	@Override
	public double distance(Object value1, Object value2) {
		if(!(value1 instanceof LinkPosition) || !(value2 instanceof LinkPosition))
			throw new IllegalArgumentException("Can't compute a distance of anything else than LinkPosition.");

		final LinkPosition pos1 = (LinkPosition) value1;
		final LinkPosition pos2 = (LinkPosition) value2;
		return distance(pos1, pos2);
	}
}

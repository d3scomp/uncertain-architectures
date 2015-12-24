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

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metric.Metric;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessMapMetric implements Metric {

	private static final long timeWindow = 5000;
	
	private static final double dirtWindow = 0.1;
	
	private static final double differencePenalty = 1;
	
	static public double distance(final DirtinessMap map1, final DirtinessMap map2) {
		double dist = 0;
		
		for(Node n : DirtinessMap.getNetwork().getNodes()){
			// If the nodes were visited in same-ish time
			if(Math.abs(map1.getVisitedNodes().get(n) - map2.getVisitedNodes().get(n))
					<= timeWindow){
				double dirt1 = map1.getDirtiness().containsKey(n)
						? map1.getDirtiness().get(n)
						: 0;
				double dirt2 = map2.getDirtiness().containsKey(n)
						? map2.getDirtiness().get(n)
						: 0;
				if(Math.abs(dirt1 - dirt2) > dirtWindow){
					dist += differencePenalty;
				}
			}
		}
		
		return dist;
	}

	@Override
	public double distance(Object value1, Object value2) {
		if(!(value1 instanceof DirtinessMap) || !(value2 instanceof DirtinessMap))
			throw new IllegalArgumentException(
					"Can't compute a distance of anything else than DirtinessMap.");

		final DirtinessMap map1 = (DirtinessMap) value1;
		final DirtinessMap map2 = (DirtinessMap) value2;
		return distance(map1, map2);
	}
}
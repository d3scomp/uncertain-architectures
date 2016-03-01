/*******************************************************************************
 * Copyright 2016 Charles University in Prague
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
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class RobotParameters {
	public final String robotName;
	public final long robotSeed;
	public final int startLink;
	public final PositionFilter positionNoise;
	public final double initBattery;
	public final DoubleFilter batteryNoise;
	public final SearchTrajectoryPlanner trajectoryPlanner;
	public final NearestTrajectoryPlanner dockingPlanner;
	public final TrajectoryExecutor trajectoryExecutor;
	
	public RobotParameters(String robotName,
			long robotSeed,
			int startLink,
			PositionFilter positionFilter,
			double initBattery,
			DoubleFilter batteryNoise,
			SearchTrajectoryPlanner trajectoryPlanner,
			NearestTrajectoryPlanner dockingPlanner,
			TrajectoryExecutor trajectoryExecutor){
		this.robotName = robotName;
		this.robotSeed = robotSeed;
		this.startLink = startLink;
		this.positionNoise = positionFilter;
		this.initBattery = initBattery;
		this.batteryNoise = batteryNoise;
		this.trajectoryPlanner = trajectoryPlanner;
		this.dockingPlanner = dockingPlanner;
		this.trajectoryExecutor = trajectoryExecutor;
	}
}

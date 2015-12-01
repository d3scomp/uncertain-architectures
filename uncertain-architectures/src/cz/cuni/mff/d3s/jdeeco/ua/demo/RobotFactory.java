/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/
package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.movement.NearestTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.SearchTrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

public class RobotFactory {

	private final Robot robot;
	private boolean batterySet = false;
	private boolean batteryNoiseSet = false;
	private boolean positionSet = false;
	private boolean positionNoiseSet = false;
	private boolean plannerSet = false;
	private boolean moverSet = false;
	
	private RobotFactory(String robotId){
		robot = new Robot(robotId);
	}
	
	public static RobotFactory newRobot(String robotId){
		if(robotId == null || robotId.length() == 0) throw new IllegalArgumentException(
				String.format("The \"%s\" argument cannot be null nor empty string.", "robotId"));
		return new RobotFactory(robotId);
	}
	
	public RobotFactory withBatteryLevel(double initialBatteryLevel){
		robot.batteryLevel = new CorrelationMetadataWrapper<>(initialBatteryLevel);
		batterySet = true;
		return this;
	}
	
	public RobotFactory withBatteryNoise(DoubleFilter batteryNoise){
		robot.batteryInaccuracy = batteryNoise;
		batteryNoiseSet = true;
		return this;
	}
	
	public RobotFactory atPosition(int linkNumber){
		Set<Link> links = DirtinessMap.getNetwork().getLinks();
		if(linkNumber < 0 || linkNumber >= links.size())
			throw new IllegalArgumentException(String.format(
				"The \"%s\" argument is out of bounds.", "linkNumber"));
		for(Link link : links)
		{
			if(link.getId() == linkNumber){
				robot.position = new CorrelationMetadataWrapper<>(new LinkPosition(link, robot.id));
				positionSet = true;
				robot.map.updateRobotsPosition(robot.id, robot.position.getValue());
				break;
			}
		}
		return this;
	}
	
	public RobotFactory withPositionNoise(PositionFilter positionNoise){
		robot.positionInaccuracy = positionNoise;
		positionNoiseSet = true;
		return this;
	}
	
	public RobotFactory withTrajectoryPlanner(SearchTrajectoryPlanner planner){
		if(planner == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "planner"));
		planner.setRobot(robot);
		robot.searchPlanner = planner;
		plannerSet = true;
		return this;
	}
	
	public RobotFactory withDockingPlanner(NearestTrajectoryPlanner planner){
		if(planner == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "planner"));
		planner.setRobot(robot);
		robot.targetPlanner = planner;
		plannerSet = true;
		return this;
	}
	
	public RobotFactory withTrajectoryExecutor(TrajectoryExecutor mover){
		if(mover == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "mover"));
		mover.setRobot(robot);
		robot.mover = mover;
		moverSet = true;
		return this;
	}
	
	public Robot create(){
		if(!batterySet) throw new IllegalStateException("The initial battery has not been set.");
		if(!batteryNoiseSet) throw new IllegalStateException("The battery noise filter has not been set.");
		if(!positionSet) throw new IllegalStateException("The initial position has not been set.");
		if(!positionNoiseSet) throw new IllegalStateException("The position noise filter has not been set.");
		if(!plannerSet) throw new IllegalStateException("The trajectory planner has not been set.");
		if(!moverSet) throw new IllegalStateException("The trajectory executor has not been set.");
		
		return robot;
	}
	
}

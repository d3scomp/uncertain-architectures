package cz.cuni.mff.d3s.jdeeco.ua.demo;

import java.util.Set;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleFilter;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionFilter;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryPlanner;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;

public class RobotFactory {

	private Robot robot;
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
		robot.batteryLevel = new MetadataWrapper<>(initialBatteryLevel);
		batterySet = true;
		return this;
	}
	
	public RobotFactory withBatteryNoise(DoubleFilter batteryNoise){
		robot.batteryInaccuracy = batteryNoise;
		batteryNoiseSet = true;
		return this;
	}
	
	public RobotFactory atPosition(int linkNumber){
		Set<Link> links = robot.map.getNetwork().getLinks();
		if(linkNumber < 0 || linkNumber >= links.size())
			throw new IllegalArgumentException(String.format(
				"The \"%s\" argument is out of bounds.", "linkNumber"));
		int index = 0;
		for(Link link : links)
		{
			if(index == linkNumber){
				robot.position = new LinkPosition(link);
				positionSet = true;
				robot.map.updateRobotsPosition(robot.id, robot.position);
				break;
			}
			index++;
		}
		return this;
	}
	
	public RobotFactory withPositionNoise(PositionFilter positionNoise){
		robot.positionInaccuracy = positionNoise;
		positionNoiseSet = true;
		return this;
	}
	
	public RobotFactory withTrajectoryPlanner(TrajectoryPlanner planner){
		if(planner == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "planner"));
		planner.setRobot(robot);
		robot.planner = planner;
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

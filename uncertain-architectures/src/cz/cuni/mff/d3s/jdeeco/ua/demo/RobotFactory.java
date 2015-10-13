package cz.cuni.mff.d3s.jdeeco.ua.demo;

import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.MetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.ua.filter.DoubleNoise;
import cz.cuni.mff.d3s.jdeeco.ua.filter.PositionNoise;
import cz.cuni.mff.d3s.jdeeco.ua.map.PositionKnowledge;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryExecutor;
import cz.cuni.mff.d3s.jdeeco.ua.movement.TrajectoryPlanner;

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
	
	public RobotFactory withBatteryNoise(DoubleNoise batteryNoise){
		robot.batteryInaccuracy = batteryNoise;
		batteryNoiseSet = true;
		return this;
	}
	
	public RobotFactory atPosition(Position initialPosition){
		if(initialPosition == null) throw new IllegalArgumentException(String.format(
				"The \"%s\" argument cannot be null.", "initialPosition"));
		robot.position = initialPosition;
		robot.believedPosition = new MetadataWrapper<>(
				new PositionKnowledge(initialPosition, 0));
		positionSet = true;
		return this;
	}
	
	public RobotFactory withPositionNoise(PositionNoise positionNoise){
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

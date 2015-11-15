package cz.cuni.mff.d3s.jdeeco.modes.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.ComponentModeChart;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Mode;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.modes.example.modechart.Cleaning;
import cz.cuni.mff.d3s.jdeeco.modes.example.modechart.RobotModeChart;
import cz.cuni.mff.d3s.jdeeco.modes.example.modechart.Searching;

@ComponentModeChart(RobotModeChart.class)
@Component
public class Robot {

	public String id;
	public Integer position;
	
	public List<DirtySpot> dirtySpots;
	
	public POI POI;
	public Plan plan;
	public Map<Integer,Availability> availabilityList;
	public Boolean planFeasibility;
 
	public Robot() {
		position = 5;
		dirtySpots = new ArrayList<>();
	}
 
	@Mode(Searching.class)
	@Process
	@PeriodicScheduling(period=300) 
	public static void movingAround(
		@In("id") String id, 
		@In("position") Integer position,  
		@InOut("dirtySpots") ParamHolder<List<DirtySpot>> dirtySpots
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		System.out.println("Inside " + id + "#movingAround at time " + currentTime);
		
		if (currentTime >= 1000) {
			System.out.println("DirtyTiles detected at time " + currentTime);
			
			dirtySpots.value.add(new DirtySpot());
		}
	}

 
	@Mode(Cleaning.class)
	@Process
	@PeriodicScheduling(period=300) 
	public static void cleaningTile(
		@In("id") String id
	) {
		long currentTime = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		System.out.println("Inside " + id + "#cleaningTile at time " + currentTime);
	}

}

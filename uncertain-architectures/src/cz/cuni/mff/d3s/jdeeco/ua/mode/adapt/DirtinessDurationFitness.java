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
package cz.cuni.mff.d3s.jdeeco.ua.mode.adapt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationUtility;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtChangedListener;
import cz.cuni.mff.d3s.jdeeco.ua.map.DirtinessMap;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class DirtinessDurationFitness extends AdaptationUtility implements DirtChangedListener {

	/**
	 * Initial times of uncompleted dirtiness events.
	 * Initial time is when the dirt appears. The dirtiness event
	 * is completed when the dirt is cleaned.
	 */
	private final Map<Node, Long> discoveredDirt = new HashMap<>();
	
	private final SimulationTimer timer;
	

	private PrintWriter writer;

	/**
	 * Summary of time durations of completed dirtiness events.
	 */
	private long durationsSum = 0;
	
	/**
	 * Number of completed dirtiness events.
	 */
	private int durationsCnt = 0;
	
	
	public DirtinessDurationFitness(SimulationTimer timer, String file) {
		if(timer == null){
			throw new IllegalArgumentException(String.format("The %s argument is null.", "timer"));
		}
		if(file == null){
			throw new IllegalArgumentException(String.format("The %s argument is null.", "file"));
		}
		
		try {
			File f = new File(file);
			Path dir = f.getParentFile().toPath();
			Files.createDirectories(dir);
			writer = new PrintWriter(f);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(e.getMessage());
		}
		
		
		DirtinessMap.registerListener(this);
		this.timer = timer;
	}
	

	public void terminate(){
		if(writer != null){
			writer.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getKnowledgeNames()
	 */
	@Override
	public String[] getKnowledgeNames() {
		return new String[]{"id"};
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDetModeSwitchEval#getEnergy(long, java.lang.Object[])
	 */
	@Override
	public double getUtility(Object[] knowledgeValues) {
		long unfinishedDurationSum = 0;
		int unfinishedDurationCnt = 0;
		long currentTime = timer.getCurrentMilliseconds();
		
		for(Node n : discoveredDirt.keySet()){
			unfinishedDurationSum += (currentTime - discoveredDirt.get(n));
			unfinishedDurationCnt++;
		}
		
		if((durationsCnt + unfinishedDurationCnt) == 0){
			return 0;
		}
		
		double avg = (double) (durationsSum + unfinishedDurationSum)
				/ (double) (durationsCnt + unfinishedDurationCnt);
		
		System.out.println("Utility: " + avg);
		return avg;
	}

	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.adaptation.AdaptationUtility#getUtilityThreshold()
	 */
	@Override
	public double getUtilityThreshold() {
		return 200000; // ms
	}


	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.ua.map.DirtChangedListener#dirtAppeared(cz.cuni.mff.d3s.jdeeco.visualizer.network.Node, long)
	 */
	@Override
	public void dirtAppeared(Node node) {
		// Ignore
		
	}


	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.ua.map.DirtChangedListener#dirtDiscovered(cz.cuni.mff.d3s.jdeeco.visualizer.network.Node, long)
	 */
	@Override
	public void dirtDiscovered(Node node) {
		long currentTime = timer.getCurrentMilliseconds();
		if(!discoveredDirt.containsKey(node)){
			discoveredDirt.put(node, currentTime);
		}
		
	}


	/* (non-Javadoc)
	 * @see cz.cuni.mff.d3s.jdeeco.ua.map.DirtChangedListener#dirtCleaned(cz.cuni.mff.d3s.jdeeco.visualizer.network.Node, long)
	 */
	@Override
	public void dirtCleaned(Node node) {
		long currentTime = timer.getCurrentMilliseconds();
		if(discoveredDirt.containsKey(node)){
			writer.println(String.format("%d", currentTime - discoveredDirt.get(node)));
			durationsSum += (currentTime - discoveredDirt.get(node));
			durationsCnt++;
			discoveredDirt.remove(node);
		}
		
	}

}

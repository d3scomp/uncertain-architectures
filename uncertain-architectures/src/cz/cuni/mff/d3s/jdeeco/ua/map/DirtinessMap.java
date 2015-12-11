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

import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.DIRT_GENERATION_RATE;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_HEIGHT;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.MAP_WIDTH;
import static cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration.RANDOM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.jdeeco.ua.visualization.DirtinessRecord;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Link;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Network;
import cz.cuni.mff.d3s.jdeeco.visualizer.network.Node;

/**
 * Environment heat map holder.
 */
public class DirtinessMap implements Serializable{

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2027348736872922839L;

	/**
	 * The network representation of the map.
	 */
	private static final Network NETWORK;
	
	/**
	 * The threshold beyond which a tile is considered clean.
	 * Double rounding made problems around 0.
	 */
	private static final double DIRT_EPSILON = 0.01;

	/**
	 * This field stores the position of each robot for the purposes of easy
	 * collision avoidance implementation.
	 */
	private static final Map<String, LinkPosition> ROBOT_LOCATIONS = new HashMap<>();

	private static final Set<Node> DOCKING_STATIONS = new HashSet<>();

	/**
	 * Global dirtiness. Holds the objective state of the dirtiness in the environment.
	 */
	private static final Map<Node, Double> DIRTINESS = new HashMap<>();

	/**
	 * Local dirtiness. Holds the state of the dirtiness in the environment
	 * subjective to each robot.
	 */
	private final Map<Node, Double> dirtiness;

	/**
	 * A set of timestamps of the last visit of individual tiles.
	 */
	private final Map<Node, Long> visitedNodes;

	private final String robotId;

	static {
		NETWORK = new Network();
		ArraySet<Node> nodes = new ArraySet<>(); // Hold the nodes in an array
													// for a while
		// Create nodes
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				Node n = new Node(x, y);
				// Add the node to the network
				nodes.add(n);
				NETWORK.addNode(n);
			}
		}
		// Create links
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				Node n = getElement(nodes, x, y);
				// horizontal shift
				for (int h = -1; h <= 1; h++) {
					// vertical shift
					for (int v = -1; v <= 1; v++) {
						// Skip the node n (reflexive link not desired)
						if (h == 0 && v == 0)
							continue;
						// Check neighbor is not out of bounds
						Node neighbor = getElement(nodes, x + h, y + v);
						if (neighbor != null) {
							Link l = new Link(n, neighbor);
							// Add the link to the network
							NETWORK.addLink(l);
						}
					}
				}
			}
		}
	}

	private static <T> T getElement(ArraySet<T> elements, int x, int y) {
		// Check the indices are in bounds
		if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
			return elements.get(y * MAP_WIDTH + x);
		}
		return null;
	}

	public DirtinessMap(String robotId) {
		visitedNodes = new HashMap<>();
		initVisitedNodes();
		dirtiness = new HashMap<>(MAP_WIDTH * MAP_HEIGHT);
		this.robotId = robotId;
	}
	
	private void initVisitedNodes(){
		for(Node n : NETWORK.getNodes()){
			visitedNodes.put(n, 0L);
		}
	}

	public static Network getNetwork() {
		return NETWORK;
	}

	/**
	 * Get the visited {@link Tile}s with the timestamps of the last visit.
	 * 
	 * @return
	 */
	public Map<Node, Long> getVisitedNodes() {
		return visitedNodes;
	}

	/**
	 * Get the {@link Position} of the given {@link Tile}.
	 * 
	 * @param tile
	 *            The {@link Tile} of which the position is required.
	 * 
	 * @return The {@link Position} of the given {@link Tile}.
	 */
	/*
	 * public Position getPosition(Tile tile){ return new Position(tile.x *
	 * TILE_WIDTH + 0.5 * TILE_WIDTH, tile.y * TILE_WIDTH + 0.5 * TILE_WIDTH); }
	 */

	/**
	 * The size of the map as number of tiles it contains.
	 * 
	 * @return The number of tiles in the map.
	 */
	public int size() {
		return MAP_WIDTH * MAP_HEIGHT;
	}

	/**
	 * Update the position of the specified robot in the centralized field.
	 * 
	 * @param robotId
	 *            The ID of a robot whose position will be updated.
	 * @param position
	 *            The updated position of the robot.
	 * 
	 * @throws IllegalArgumentException
	 *             Thrown if either the robotId or the position argument is
	 *             null.
	 */
	public void updateRobotsPosition(String robotId, LinkPosition position) {
		if (robotId == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "robotId"));
		if (position == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "position"));

		ROBOT_LOCATIONS.put(robotId, position);
	}

	/**
	 * Get the positions of robots other than the specified one. If the robotId
	 * argument is null no position is excluded from the result.
	 * 
	 * @param robotId
	 *            The robot to exclude its position in the obtained collection.
	 *            Can be null if no position is required to be excluded.
	 * @return The collection of positions of robots whose ID has not been
	 *         specified.
	 */
	public Collection<LinkPosition> getOthersPosition(String robotId) {
		Collection<LinkPosition> positions = ROBOT_LOCATIONS.values();
		if (robotId != null && ROBOT_LOCATIONS.containsKey(robotId)) {
			LinkPosition excludedPosition = ROBOT_LOCATIONS.get(robotId);
			positions.remove(excludedPosition);
		}

		return positions;
	}

	public LinkPosition getPosition(String robotId) {
		if (ROBOT_LOCATIONS.containsKey(robotId)) {
			return ROBOT_LOCATIONS.get(robotId);
		}
		return null;
	}

	public void checkDirtiness(Node node) {
		if (node == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "node"));
		
		// Check the given tile
		if (DIRTINESS.containsKey(node) && DIRTINESS.get(node) > 0) {
			double intensity = DIRTINESS.get(node);
			dirtiness.put(node, intensity);
		} else if(dirtiness.containsKey(node)){
			dirtiness.remove(node);
		}
		
		// Check the surrounding tiles
		for(Node n : NETWORK.getSuccessors(node)){
			if (DIRTINESS.containsKey(n) && DIRTINESS.get(n) > 0) {
				double intensity = DIRTINESS.get(n);
				dirtiness.put(n, intensity);
			} else if(dirtiness.containsKey(n)){
				dirtiness.remove(n);
			}
		}
		
	}

	public static void generateDirt() {
		if (RANDOM.nextDouble() <= DIRT_GENERATION_RATE) {
			Node node = randomNode();
			if(DOCKING_STATIONS.contains(node)){
				// Don't generate dirt on docking stations
				return;
			}
			double intensityIncrement = ((double) RANDOM.nextInt(9) + 1 ) / 10.0;
			double currentIntensity;
			if(DIRTINESS.containsKey(node)){
				currentIntensity = DIRTINESS.get(node);
			} else {
				currentIntensity = 0;
			}

			double intensity = Math.min(currentIntensity + intensityIncrement, 1);
			DIRTINESS.put(node, intensity);

			logDirtiness(node, intensity);
		}
	}

	public Map<Node, Double> getDirtiness() {
		return Collections.unmodifiableMap(dirtiness);
	}
	
	public double getDirtinessLevel(){
		double dirt = 0;
		
		for(Double d : dirtiness.values()){
			dirt += d;
		}
		
		return dirt;
	}

	public double cleanDirtiness(Node node, double portion) {
		if (node == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "node"));
		if (!Double.isFinite(portion) || portion <= 0)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument has to be finite positive number, but was (%f)",
					"portion", portion));

		if (DIRTINESS.containsKey(node)) {
			double intensity = DIRTINESS.get(node);
			if(isCleaningRelevant(intensity)){
				intensity -= portion;
				if(intensity < DIRT_EPSILON){
					intensity = 0;
				}
				DIRTINESS.put(node, intensity);
				dirtiness.put(node, intensity);

				logDirtiness(node, intensity);
				
				if(intensity < DIRT_EPSILON){
					dirtiness.remove(node);
				}
			}
			return intensity;
		}

		isCleaningRelevant(0);
		return 0;
	}
	
	private static void logDirtiness(Node node, double intensity){
		if (node == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "node"));
		
		try {
			DirtinessRecord record = new DirtinessRecord("system");
			record.setNode(node);
			record.setIntensity(intensity);
			ProcessContext.getRuntimeLogger().log(record);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void placeDockingStation(Node node){
		if (node == null)
			throw new IllegalArgumentException(String.format(
					"The \"%s\" argument cannot be null.", "node"));
		
		if(!DOCKING_STATIONS.contains(node)){
			DOCKING_STATIONS.add(node);
		} else {
			Log.w(String.format("The dock at %s already placed.", node.toString()));
		}
	}

	private boolean isCleaningRelevant(double intensity) {
		if (!(intensity > 0.0)) {
			Log.w("Trying to clean not dirty tile.");
			return false;
		}
		return true;
	}

	public static Node randomNode() {
		int end = RANDOM.nextInt(MAP_WIDTH * MAP_HEIGHT);
		int index = 0;
		for (Node n : NETWORK.getNodes()) {
			if (index == end) {
				return n;
			}
			index++;
		}
		// Should never reach this code
		assert (false);
		return null;
	}
	
	public Node getRandomNode(){
		return randomNode();
	}

	public static void outputToFile(File file) throws FileNotFoundException {
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(file);
		writer.write(NETWORK.toString());
		writer.close();
	}
}

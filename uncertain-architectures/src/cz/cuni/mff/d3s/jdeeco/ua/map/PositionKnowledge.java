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

import cz.cuni.mff.d3s.jdeeco.position.Position;

/**
 * Extension of Position containing also information about sensor inaccuracy.
 */
public class PositionKnowledge extends Position {
// TODO: maybe not needed
	/** Generated UID. */
	private static final long serialVersionUID = -8628242433411285211L;

	/** Sensor inaccuracy. */
	public final Double inaccuracy;

	/**
	 * Create from coordinates.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param inaccuracy sensor inaccuracy
	 */
	public PositionKnowledge(final double x, final double y, final double inaccuracy) {
		super(x, y);
		this.inaccuracy = inaccuracy;
	}

	/**
	 * Create from Position.
	 * @param position Position blueprint
	 * @param inaccuracy sensor inaccuracy
	 */
	public PositionKnowledge(final Position position, final double inaccuracy) {
		super(position.x, position.y);
		this.inaccuracy = inaccuracy;
	}

	@Override
	public PositionKnowledge clone() {
		return (PositionKnowledge) super.clone();
	}
	
	@Override
	public String toString() {
		return String.format("%s inaccuracy = %fm", super.toString(), inaccuracy);
	}
}

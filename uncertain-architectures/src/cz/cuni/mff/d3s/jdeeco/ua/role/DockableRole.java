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
package cz.cuni.mff.d3s.jdeeco.ua.role;

import cz.cuni.mff.d3s.deeco.annotations.Role;
import cz.cuni.mff.d3s.jdeeco.adaptation.correlation.metadata.CorrelationMetadataWrapper;
import cz.cuni.mff.d3s.jdeeco.ua.map.LinkPosition;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
@Role
public class DockableRole {
	public String id;
	public CorrelationMetadataWrapper<Double> batteryLevel;
	public CorrelationMetadataWrapper<LinkPosition> position;
	public boolean isDocking;
	public String assignedDock;
}

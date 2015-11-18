/*******************************************************************************
 * Copyright 2015 Charles University in Prague
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package cz.cuni.mff.d3s.deeco.annotations.processor;

import java.lang.annotation.Annotation;

import cz.cuni.mff.d3s.deeco.annotations.ComponentModeChart;
import cz.cuni.mff.d3s.deeco.annotations.DEECoMode;
import cz.cuni.mff.d3s.deeco.annotations.Mode;
import cz.cuni.mff.d3s.deeco.annotations.ModeChart;
import cz.cuni.mff.d3s.deeco.model.runtime.api.ComponentInstance;
import cz.cuni.mff.d3s.deeco.model.runtime.api.ComponentProcess;
import cz.cuni.mff.d3s.deeco.modes.ModeChartHolder;

/**
 * Processes the annotations related to DEECo processes' modes.
 *
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 * @see AnnotationProcessor
 */
public class ModesAwareAnnotationProcessorExtension extends AnnotationProcessorExtensionPoint {

	@Override
	public void onComponentInstanceCreation(ComponentInstance componentInstance, Annotation unknownAnnotation) {
		if (unknownAnnotation instanceof ComponentModeChart) {
			Class<? extends ModeChartHolder> modeChartHolder = ((ComponentModeChart) unknownAnnotation).value();
			try { 
				ModeChart modeChart = (modeChartHolder.newInstance()).getModeChart();
				modeChart.setComponent(componentInstance);
				
				componentInstance.setModeChart(modeChart);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			System.out.println("Found mode chart: " + modeChartHolder.getName());
		}
	}
	
	@Override
	public void onComponentProcessCreation(ComponentProcess componentProcess, Annotation unknownAnnotation) {
		if (unknownAnnotation instanceof Mode) {
			Class<? extends DEECoMode> modeClass = ((Mode) unknownAnnotation).value();
			try {
				componentProcess.setMode(modeClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			System.out.println("Found mode: " + modeClass.getName());
		}
	}

}

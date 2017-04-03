/*******************************************************************************
 * Copyright 2017 Charles University in Prague
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.jdeeco.ua.demo.Configuration;

/**
 * @author Dominik Skoda <skoda@d3s.mff.cuni.cz>
 *
 */
public class UtilityLoader {

	private class ValueHolder{
		private int count;
		private long sum;
		
		public ValueHolder(){
			count = 0;
			sum = 0;
		}
		
		public void addValue(int value){
			sum += value;
			count++;
		}
		
		public double getAverage(){
			return (double) sum / (double) count;
		}
	}
	
	public Map<String, Double> loadUtilities(){
		if(Configuration.UTILITY_DIRECTORY == null){
			Log.e(String.format("The %s not specified.", "UTILITY_DIRECTORY"));
			return null;
		}

		File directory = new File(Configuration.UTILITY_DIRECTORY);
		if(!directory.exists()){
			Log.e(String.format("The %s directory doesn't exist.", Configuration.UTILITY_DIRECTORY));
			return null;
		}
		
		Map<String, Double> utilities = new HashMap<>();
		for(File transition : getSubDirs(directory)){
			utilities.put(transition.getName(), computeUtility(transition));
		}
		
		return utilities;
	}
	
	private File[] getSubDirs(File directory){
		File[] directories = directory.listFiles(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		return directories;
	}
	
	private File[] getFiles(File directory){
		File[] files = directory.listFiles(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isFile();
			  }
			});

		return files;
	}
	
	private double computeUtility(File directory){
		File[] files = getFiles(directory);
		final ValueHolder values = new ValueHolder();
		for(File file : files){
			extractUtility(file, values);
		}
		
		return values.getAverage();
	}
	
	private void extractUtility(File file, final ValueHolder values){
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			String line = reader.readLine();
			while(line != null){
				values.addValue(Integer.parseInt(line));
				
				line = reader.readLine();
			}
		} catch (Exception e) {
			Log.e(e.getMessage());
		}
	}
}

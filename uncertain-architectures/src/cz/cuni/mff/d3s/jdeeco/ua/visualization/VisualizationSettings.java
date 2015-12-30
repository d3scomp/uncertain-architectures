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
package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

public class VisualizationSettings {

	private static final File RUNTIME_LOG_DIR = new File("logs/runtime");
	
	private static final File RESOURCES_DIR = new File("resources");
	
	private static final String CONFIG_FILE_NAME = "config.txt";
	
	public static final File CONFIG_FILE = new File(RUNTIME_LOG_DIR, CONFIG_FILE_NAME);
	
	/**
	 * The location where the map definition file is written. 
	 */
	private static final String MAP_FILE_NAME = "network.xml";
	
	public static final File MAP_FILE = new File(RUNTIME_LOG_DIR, MAP_FILE_NAME);
	
	private static final String EVENT_FILE_NAME = "runtimeData.xml";
	
	public static final File EVENT_FILE = new File(RUNTIME_LOG_DIR, EVENT_FILE_NAME);
	
	public static final String SCRIPTS_FILE_NAME = "setBackgroundImages.acmescript";
	
	public static final File SCRIPT_FILE = new File(RUNTIME_LOG_DIR, SCRIPTS_FILE_NAME);
	
	private static final String NETWORK_TOKEN = "network";
	
	private static final String EVENTS_TOKEN = "events";
	
	private static final String SCRIPTS_TOKEN = "scripts";
	
	private static final String SHOW_LINKS_TOKEN = "showLinks";
	
	private static final String PLUGINS_TOKEN = "plugins";
	
	public static final String PLUGIN_CLASS_NAME = CleaningRobotsPlugin.class.getCanonicalName();
	
	private static final String PLUGINS_DIRS_TOKEN = "pluginsDirs";

	private static final File CURRENT_PROJECT_DIR = new File("target/classes/");
	
	private static final String FALSE_VALUE = "false";
	
	private static final String SEP = ";";
	
	private static final String ENDL = "\n";
	
	private static final String ENC = "UTF-8";
	
	public static void createConfigFile() throws IOException{
		createScriptFile();
		CONFIG_FILE.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(CONFIG_FILE);
		StringBuilder builder = new StringBuilder();
		
		builder.append(NETWORK_TOKEN).append(SEP)
			.append(MAP_FILE.getAbsolutePath()).append(SEP)
			.append(ENC).append(ENDL);
		builder.append(EVENTS_TOKEN).append(SEP)
			.append(EVENT_FILE.getAbsolutePath()).append(SEP)
			.append(ENC).append(ENDL);
		builder.append(SCRIPTS_TOKEN).append(SEP)
		.append(SCRIPT_FILE.getAbsolutePath()).append(SEP)
		.append(ENDL);
		builder.append(SHOW_LINKS_TOKEN).append(SEP)
		.append(FALSE_VALUE).append(SEP)
		.append(ENDL);
		builder.append(PLUGINS_DIRS_TOKEN).append(SEP)
		.append(CURRENT_PROJECT_DIR.getAbsolutePath()).append(SEP)
		.append(ENDL);
		builder.append(PLUGINS_TOKEN).append(SEP)
		.append(PLUGIN_CLASS_NAME).append(SEP)
		.append(ENDL);
		
		writer.write(builder.toString());
		writer.close();
	}

	private static void createScriptFile() throws IOException {
		// ensure the target directory exists
		RUNTIME_LOG_DIR.mkdirs();
		FileWriter writer = new FileWriter(SCRIPT_FILE);
		StringBuilder builder = new StringBuilder();

		builder.append("general.setAdditionalResourcesPath(\"").append(revertBackSlashes(RESOURCES_DIR.getAbsolutePath())).append("\")")
				.append(ENDL).append("general.setPersonImage(\"turtlebot.png\")").append(ENDL)
				.append("general.setNodeImage(\"tile.png\")");
		writer.write(builder.toString());
		writer.close();
	}
	
	private static String revertBackSlashes(String str) {
		StringTokenizer strTok = new StringTokenizer(str, File.separator);
		StringBuilder builder = new StringBuilder();
		while (strTok.hasMoreTokens()) {
			builder.append(strTok.nextToken()).append("/");
		}
		return builder.toString();
	}
	
}

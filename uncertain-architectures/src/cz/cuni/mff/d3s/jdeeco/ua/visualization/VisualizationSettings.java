package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VisualizationSettings {

	private static final File RUNTIME_LOG_DIR = new File("logs/runtime");
	
	private static final String CONFIG_FILE_NAME = "config.txt";
	
	public static final File CONFIG_FILE = new File(RUNTIME_LOG_DIR, CONFIG_FILE_NAME);
	
	/**
	 * The location where the map definition file is written. 
	 */
	private static final String MAP_FILE_NAME = "network.xml";
	
	public static final File MAP_FILE = new File(RUNTIME_LOG_DIR, MAP_FILE_NAME);
	
	private static final String EVENT_FILE_NAME = "runtimeData.xml";
	
	public static final File EVENT_FILE = new File(RUNTIME_LOG_DIR, EVENT_FILE_NAME);
	
	public static final File[] PLUGIN_DIRS = {}; //{RUNTIME_LOG_DIR};
	
	private static final String NETWORK_TOKEN = "network";
	
	private static final String EVENTS_TOKEN = "events";
	
	private static final String PLUGIN_DIRS_TOKEN = "plugins";
	
	private static final String SEP = ";";
	
	private static final String ENDL = "\n";
	
	private static final String ENC = "UTF-8";
	
	
	public static void createConfigFile() throws IOException{
		CONFIG_FILE.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(CONFIG_FILE);
		StringBuilder builder = new StringBuilder();
		
		builder.append(NETWORK_TOKEN).append(SEP)
			.append(MAP_FILE.getAbsolutePath()).append(SEP)
			.append(ENC).append(ENDL);
		builder.append(EVENTS_TOKEN).append(SEP)
			.append(EVENT_FILE.getAbsolutePath()).append(SEP)
			.append(ENC).append(ENDL);
		if(PLUGIN_DIRS.length > 0){
			builder.append(PLUGIN_DIRS_TOKEN);
			for(File plugin_dir : PLUGIN_DIRS){
				builder.append(SEP).append(plugin_dir.getAbsolutePath());
			}
			builder.append(ENDL);
		}
		
		writer.write(builder.toString());
		writer.close();
	}
	
	
}

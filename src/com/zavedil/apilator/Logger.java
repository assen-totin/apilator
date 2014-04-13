package com.zavedil.apilator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

public class Logger {
	/**
	 * Log levels:
	 * 
	 * 0 - LOG_NONE - never log anything: don't ever use!
	 * 1 - LOG_CRITICAL - log critical events: app cannot run due to external reasons
	 * 2 - LOG_ERROR - log errors: app cannot run due to internal reasons
	 * 3 - LOG_WARNING - log warnings: erroneous, but recoverable situations
	 * 4 - LOG_NOTICE - log notices: normal operation events outside main loop (start-up, shutdown)
	 * 5 - LOG_TRACE -  log traces: normal operation events inside main loop
	 * 6 - LOG_DEBUG - log all messages
	 * 
	 */
	
	private static final Hashtable<Integer,String> ErrorLevels = new Hashtable<Integer,String>() {{
			put(0, "NONE");
			put(1, "CRITICAL");
			put(2, "ERROR");
			put(3, "WARNING");
			put(4, "NOTICE");
			put(5, "TRACE");
			put(6, "DEBUG");
	}};
		
	private static void log(String className, String input, int level) {
		SimpleDateFormat format;
		int current_log_level;
		String line, system_name, level_name, date;
		
		current_log_level = Config.getLogLevel();
		//system_name = Config.getSystemName();
		
		if (level > current_log_level)
			return;

		level_name = ErrorLevels.get(level);
		
		format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
		date = format.format(new Date());
				
		line = "[" +  date + "][" + level_name + "][" + className + "] " + input;
		
		switch (level) {
			case 1:
			case 2:
				System.err.println(line);
			default:
				System.out.println(line);
		}
	}
	
	public static void none(String className, String input) {
		log(className, input, 0);
	}
	
	public static void critical(String className, String input) {
		log(className, input, 1);
	}

	public static void error(String className, String input) {
		log(className, input, 2);
	}

	public static void warning(String className, String input) {
		log(className, input, 3);
	}

	public static void notice(String className, String input) {
		log(className, input, 4);
	}

	public static void trace(String className, String input) {
		log(className, input, 5);
	}

	public static void debug(String className, String input) {
		log(className, input, 6);
	}

}

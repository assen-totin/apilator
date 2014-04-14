package com.zavedil.apilator;

import java.io.FileOutputStream;
import java.io.IOException;
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
	
	// Log levels
	private static final Hashtable<Integer,String> ErrorLevels = new Hashtable<Integer,String>() {{
			put(0, "NONE");
			put(1, "CRITICAL");
			put(2, "ERROR");
			put(3, "WARNING");
			put(4, "NOTICE");
			put(5, "TRACE");
			put(6, "DEBUG");
	}};
	
	public static void log_access(String host, String username, String request, int http_resp_status, int http_resp_body_len) {
		SimpleDateFormat format;
		String line, date, ident="-", space=" ";
		
		format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
		date = format.format(new Date());
				
		if (username.length() == 0)
			username = "-";
		
		line = host + space + ident + space + username + space + "[" +  date + "]" + space + request + space + http_resp_status + space + http_resp_body_len + "\n";

		try {
			FileOutputStream fout = new FileOutputStream(Config.getAccessLog());
			fout.write(line.getBytes());
			fout.close();
		}
		catch ( IOException e) {
			System.err.println("Unable to write to access log file: " + Config.getAccessLog());
		}
	}
	
	/**
	 * Actual logging function for events
	 * @param className The name of the class in which the event occurred
	 * @param input Log message
	 * @param level Event level
	 */
	private static void log_event(String className, String input, int level) {
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
	
	// Public function for level LOG_NONE
	public static void none(String className, String input) {
		log_event(className, input, 0);
	}

	// Public function for level LOG_CRITICAL
	public static void critical(String className, String input) {
		log_event(className, input, 1);
	}

	// Public function for level LOG_ERROR
	public static void error(String className, String input) {
		log_event(className, input, 2);
	}

	// Public function for level LOG_WARNING
	public static void warning(String className, String input) {
		log_event(className, input, 3);
	}

	// Public function for level LOG_NOTICE
	public static void notice(String className, String input) {
		log_event(className, input, 4);
	}

	// Public function for level LOG_TRACE
	public static void trace(String className, String input) {
		log_event(className, input, 5);
	}

	// Public function for level LOG_DEBUG
	public static void debug(String className, String input) {
		log_event(className, input, 6);
	}
}

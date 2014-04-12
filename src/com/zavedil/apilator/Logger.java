package com.zavedil.apilator;

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
	
	private static void log(String className, String input, int level) {
		int current_log_level = Config.getLogLevel();
		
		if (level > current_log_level)
			return;
		
		String line = className + input;
		
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

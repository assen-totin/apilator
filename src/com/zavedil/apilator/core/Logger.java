package com.zavedil.apilator.core;

/**
 * Logger class
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Created for the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import com.zavedil.apilator.app.*;

public class Logger {
	/**
	 * Log levels:
	 * 0 - LOG_NONE - never log anything: don't ever use!
	 * 1 - LOG_CRITICAL - log critical events: app cannot run due to external reasons
	 * 2 - LOG_ERROR - log errors: app cannot run due to internal reasons
	 * 3 - LOG_WARNING - log warnings: erroneous, but recoverable situations
	 * 4 - LOG_NOTICE - log notices: normal operation events outside main loop (start-up, shutdown)
	 * 5 - LOG_TRACE -  log traces: normal operation events inside main loop
	 * 6 - LOG_DEBUG - log all messages
	 */
	private static final Hashtable<Integer,String> ErrorLevels = new Hashtable<Integer,String>() {
		private static final long serialVersionUID = 1L;
		{
			put(0, "NONE");
			put(1, "CRITICAL");
			put(2, "ERROR");
			put(3, "WARNING");
			put(4, "NOTICE");
			put(5, "TRACE");
			put(6, "DEBUG");
		}
	};
	
	/**
	 * Method for writing to the access log.
	 * @param host String The IP address of the remote host
	 * @param username String The username of the logged user (we don't handle HTTP authentication, so it is always "-")
	 * @param request String The first line of the HTTP request (Method, URL, protocol)
	 * @param http_resp_status int HTTP status code returned to the client
	 * @param http_resp_body_len int NUmber of bytes in the HTTP body returned to the client
	 */
	public static void log_access(String host, String username, String request, int http_resp_status, int http_resp_body_len) {
		SimpleDateFormat format;
		String line, date, ident="-", space=" ";
		
		format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
		date = format.format(new Date());
				
		if (username.length() == 0)
			username = "-";
		
		line = host + space + ident + space + username + space + "[" +  date + "]" + space + request + space + http_resp_status + space + http_resp_body_len + "\n";

		try {
			// Open the file for appending
			FileOutputStream fout = new FileOutputStream(Config.AccessLog, true);
			fout.write(line.getBytes());
			fout.close();
		}
		catch ( IOException e) {
			System.err.println("Unable to write to access log file: " + Config.AccessLog);
		}
	}
	
	/**
	 * Actual logging function for events
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 * @param level int Event logging level
	 */
	private static void log_event(String className, String input, int level) {
		SimpleDateFormat format;
		int current_log_level;
		String line, level_name, date;
		
		current_log_level = Config.LogLevel;
		
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
	
	/**
	 * Public function to log with level LOG_NONE
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void none(String className, String input) {
		log_event(className, input, 0);
	}

	/**
	 * Public function to log with level LOG_CRITICAL
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void critical(String className, String input) {
		log_event(className, input, 1);
	}

	/**
	 * Public function to log with level LOG_ERROR
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void error(String className, String input) {
		log_event(className, input, 2);
	}

	/**
	 * Public function to log with level LOG_WARNING
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void warning(String className, String input) {
		log_event(className, input, 3);
	}

	/**
	 * Public function to log with level LOG_NOTICE
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void notice(String className, String input) {
		log_event(className, input, 4);
	}

	/**
	 * Public function to log with level LOG_TRACE
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void trace(String className, String input) {
		log_event(className, input, 5);
	}

	/**
	 * Public function to log with level LOG_DEBUG
	 * @param className String The name of the class in which the event occurred
	 * @param input String Log message
	 */
	public static void debug(String className, String input) {
		log_event(className, input, 6);
	}
}

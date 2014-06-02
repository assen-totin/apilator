package com.zavedil.apilator.app;

import java.io.FileInputStream;
import java.util.Properties;

import com.zavedil.apilator.core.*;

/**
 * Configuration class.
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

public class Config {
	// These are internal defauts for the package.
	// They will be overridden at runtime by the values from /etc/apilator.ini, if such is present. 
	
	// System name
	public static String SystemName = "Apilator 1.0";
	
	// IP address to listen at. Leave empty for all
	public static String IpAddress = "";
	
	// TCP port to listen at
	public static int TcpPort = 8080;

	// Number of workers for serving HTTP; set to (approx.) the number of CPU cores available
	public static int NumWorkersHttp = 4;

	// Number of workers for Session Manager; set to (approx.) the number of the servers in the cluster
	public static int NumWorkersSm = 5;
	
	// Filename for access log
	public static String AccessLog = "/tmp/apilator.log";
	
	// DocumentRoot for serving static content
	public static String DocumentRoot = "/var/www/html";
		
	// Log level
	public static int LogLevel = 5; // LOG_TRACE
	
	// Sessoin size in bytes
	public static int SessionSize = 8192;
	
	// Session cookie name; set to empty to disable auto-search and auto-dispatch of session ID as cookie.
	public static String SessionCookie = "apilator";
	
	// Session cookie expiration time: (for example, 30 days). Time is in milliseconds and should be long.
	// If the number is positive, the session cookie will every time be set with this TTL as offset from current time 
	// (meaning if a valid session is provided, it will be extended with this amount of time)
	// If the number is positive, the session cookie will every time be set with this TTL as offset from session creation time
	// (meaning if a valid session is provided, its original TTL will be retained)
	// If the number is 0, the session cookie will be sent without any TTL.
	public static long SessionCookieExpire = 30 * 24 * 60 * 60 * 1000L;
	
	// Multicast address for Session Manager
	public static String SessionManagerMulticastIp = "234.234.234.234";
	public static int SessionManagerMulticastPort = 12345;

	// Unicast TCP Port for Session Manager
	public static int SessionManagerTcpPort = 54321;
	
	// Timeout waiting for peers to respond to 'who has' multicast query, milliseconds
	public static int SessionManagerTimeout = 100;
	
	// Time interval to run the Session Manager clean-upper thread, in milliseconds (1 minute by default)
	public static int SessionManagerCleanupperInterval = 60000;
	
	// File to periodically dump the session storage for faster restore on server restart
	public static String SessionManagerDiskCache = "/tmp/apilator.cache";

	static {
	    try {
	    	// Load overrides from external config file
	        Properties p = new Properties();
	        p.load(new FileInputStream("/etc/apilator.ini"));
	        
	        if (p.containsKey("SystemName"))
	        	SystemName = p.getProperty("SystemName");
	        
	        if (p.containsKey("IpAddress"))
	        	IpAddress = p.getProperty("IpAddress");
	        
	        if (p.containsKey("TcpPort"))
	        	TcpPort = Integer.parseInt(p.getProperty("TcpPort"));

	        if (p.containsKey("NumWorkersHttp"))
	        	NumWorkersHttp = Integer.parseInt(p.getProperty("NumWorkersHttp"));
	        
	        if (p.containsKey("NumWorkersSm"))
	        	NumWorkersSm = Integer.parseInt(p.getProperty("NumWorkersSm"));
	        
	        if (p.containsKey("AccessLog"))
	        	AccessLog = p.getProperty("AccessLog");
	        
	        if (p.containsKey("DocumentRoot"))
	        	DocumentRoot = p.getProperty("DocumentRoot");
	        
	        if (p.containsKey("LogLevel"))
	        	LogLevel = Integer.parseInt(p.getProperty("LogLevel"));
	        
	        if (p.containsKey("SessionSize"))
	        	SessionSize = Integer.parseInt(p.getProperty("SessionSize"));
	        
	        if (p.containsKey("SessionCookie"))
	        	SessionCookie = p.getProperty("SessionCookie");
	        
	        if (p.containsKey("SessionCookieExpire"))
	        	SessionCookieExpire = Long.parseLong(p.getProperty("SessionCookieExpire"));
	        
	        if (p.containsKey("SessionManagerMulticastIp"))
	        	SessionManagerMulticastIp = p.getProperty("SessionManagerMulticastIp");
	        
	        if (p.containsKey("SessionManagerMulticastPort"))
	        	SessionManagerMulticastPort = Integer.parseInt(p.getProperty("SessionManagerMulticastPort"));

	        if (p.containsKey("SessionManagerTcpPort"))
	        	SessionManagerTcpPort = Integer.parseInt(p.getProperty("SessionManagerTcpPort"));
	        
	        if (p.containsKey("SessionManagerTimeout"))
	        	SessionManagerTimeout = Integer.parseInt(p.getProperty("SessionManagerTimeout"));
	        
	        if (p.containsKey("SessionManagerCleanupperInterval"))
	        	SessionManagerCleanupperInterval = Integer.parseInt(p.getProperty("SessionManagerCleanupperInterval"));
	        
	        if (p.containsKey("SessionManagerDiskCache"))
	        	SessionManagerDiskCache = p.getProperty("SessionManagerDiskCache");
	        
	        Logger.notice("Config", "Loaded external config file: /etc/apilator.ini");
        }
	    catch (Exception e) {
	    	Logger.warning("Config", "Could not load external config file: /etc/apilator.ini");
		}
	}
}

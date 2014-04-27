package com.zavedil.apilator.app;

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
	// System name
	public static final String SystemName = "Apilator";
	
	// IP address to listen at. Leave empty for all
	public static final String IpAddress = "";
	
	// TCP port to listen at
	public static final int TcpPort = 8080;

	// Maximum number of workers for serving HTTP, set to 0 to disable upper limit
	public static final int MaxWorkersHttp = 100;

	// Maximum number of workers for Session Manager, set to 0 to disable upper limit
	// As many as the servers in the cluster is enough
	public static final int MaxWorkersSm = 10;
	
	// Filename for access log
	public static final String AccessLog = "/tmp/apilator.log";
	
	// DocumentRoot for serving static content
	//public static final String DocumentRoot = "/Users/assen/Desktop/L";
	public static final String DocumentRoot = "/root/Desktop/_Pics/L";
		
	// Log level
	public static final int LogLevel = 6; // LOG_DEBUG
	
	// Sessoin size in bytes
	public static final int SessionSize = 8192;
	
	// Session cookie name; set to empty to disable auto-search and auto-dispatch of session ID as cookie.
	public static final String SessionCookie = "apilator";
	
	// Session cookie expiration time: (for example, 30 days). Time is in milliseconds and should be long.
	// If the number is positive, the session cookie will every time be set with this TTL as offset from current time 
	// (meaning if a valid session is provided, it will be extended with this amount of time)
	// If the number is positive, the session cookie will every time be set with this TTL as offset from session creation time
	// (meaning if a valid session is provided, its original TTL will be retained)
	// If the number is 0, the session cookie will be sent without any TTL.
	public static final long SessionCookieExpire = 30 * 24 * 60 * 60 * 1000L;
	
	// Multicast address for Session Manager
	public static final String SessionManagerMulticastIp = "234.234.234.234";
	public static final int SessionManagerMulticastPort = 12345;

	// Unicast TCP Port for Session Manager
	public static final int SessionManagerTcpPort = 54321;
	
	// Unicast UDP Port for Session Manager
	public static final int SessionManagerUdpPort = 54322;
	
	// Timeout waiting for peers to respond to 'who has' multicast query, milliseconds
	public static final int SessionManagerTimeout = 10;
	
	// Time interval to run the Session Manager clean-upper thread, in milliseconds (1 minute by default)
	public static final int SessionManagerCleanupperInterval = 60000;
	
	// File to periodically dump the session storage for faster restore on server restart
	public static final String SessionManagerDiskCache = "/tmp/apilator.cache";
}

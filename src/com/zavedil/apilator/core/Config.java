package com.zavedil.apilator.core;

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
	
	// TCP port to listen at
	public static final int TcpPort = 8080;

	// Filename for access log
	public static final String AccessLog = "/tmp/apilator-access.log";
	
	// Location for static content. If the local part of the URL begins with this string, 
	// the remainder of the local part will be mapped to the filesystem starting at DocumentRoot (see below)
	// and the requested file, if found, will be served. 
	public static final String StaticLocation = "/static";
	
	// DocumentRoot for serving static content
	//private static final String DocumentRoot = "/Users/assen/Desktop/L";
	public static final String DocumentRoot = "/root/Desktop/_Pics/L";
		
	// Log level
	public static final int LogLevel = 6; // LOG_DEBUG
	
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
	public static final int SessionManagerTcpPort = 12345;
	
	// Timeout waiting for peers to respond to 'who has' multicast query, milliseconds
	public static final int SessionManagerTimeout = 10;
	
	/*
	// URL for object retrieval via Session Manager
	public static final String SessionManagerLocation = "/sessman";

	// Pre-shared key for object retrieval via Session Manager.
	// Because session objects are retrieved using standard HTTP request, it is important to make sure
	// only authorised hosts have access to them. Default behaviour is to only allow queries from hosts on the same subnet.
	// To keep this behaviour, leave the pre-shared key blank. Only set it to something else if you really need to allow
	// queries from other network segments. 
	public static final String SessionManagerPsk = "";
	*/
}

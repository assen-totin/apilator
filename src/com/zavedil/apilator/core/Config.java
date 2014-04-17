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
	
	// DocumentRoot for serving static content
	//private static final String DocumentRoot = "/Users/assen/Desktop/L";
	public static final String DocumentRoot = "/root/Desktop/_Pics/L";
		
	// Log level
	public static final int LogLevel = 6; // LOG_DEBUG
	
	// Session cookie name
	public static final String SessionCookie = "apilator";
	
	// Multicast address for Session Manager
	public static final String SessionManagerIp = "234.234.234.234";
	public static final int SessionManagerPort = 12345;
}

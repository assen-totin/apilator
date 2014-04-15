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
	private static final String SystemName = "Apilator";
	
	// TCP port to listen at
	private static final int TcpPort = 8080;

	// Filename for access log
	private static final String AccessLog = "/tmp/apilator-access.log";
	
	// DocumentRoot for serving static content
	//private static final String DocumentRoot = "/Users/assen/Desktop/L";
	private static final String DocumentRoot = "/root/Desktop/_Pics/L";
		
	// Log level
	private static final int LogLevel = 6; // LOG_DEBUG
	
	/**
	 * Getter for TCP port value.
	 * @return int The TCP port to listen at.
	 */
	public static int getTcpPort() {
		return TcpPort;
	}
	
	/**
	 * Getter for DocumentRoot
	 * @return String The DocumentRoot
	 */
	public static String getDocumentRoot() {
		// Skip the trailing "/", if any
		if (DocumentRoot.charAt(DocumentRoot.length()-1) == '/')
			return DocumentRoot.substring(0, DocumentRoot.length() - 1);
		return DocumentRoot;
	}
	
	/**
	 * Getter for the log level
	 * @return int Current log level ID
	 */
	public static int getLogLevel() {
		return LogLevel;
	}
	
	/**
	 * Getter for the system's name
	 * @return String The system's name
	 */
	public static String getSystemName() {
		return SystemName;
	}
	
	/**
	 * Getter for the access log path
	 * @return String Path and file name of the access log
	 */
	public static String getAccessLog() {
		return AccessLog;
	}
}

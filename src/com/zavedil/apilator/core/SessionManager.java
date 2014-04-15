package com.zavedil.apilator.core;

/**
 * Session manager class. 
 * Updates the local session storage from network.
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

public class SessionManager implements Runnable {
	private final String className;
	public static final int MSG_UPDATE = 1;		// Used when multicasting an update
	public static final int MSG_WHOHAS = 2;		// Used when asking for key
	public static final int MSG_ISAT = 3;		// Used when sending a reply to WHO HAS	
	
	public SessionManager() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		
		//TODO: Add multicast listener code here
    }
		
	/**
	 * Store a sessionID and its corresponding Object in storage. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	private void put(String key, Object value) {
		SessionStorage.put(key, value);
	}	
}

package com.zavedil.apilator.core;

/**
 * Session storage class. 
 * Maintains a local session storage.
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

import java.util.concurrent.ConcurrentHashMap;

public class SessionStorage implements Runnable {
	ConcurrentHashMap<String, Object> session_storage = null;
	private final String className;
	
	public SessionStorage() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		// Create initial storage for 1000 sessions, expand when 90% full and use only 1 shard
    	session_storage = new ConcurrentHashMap<String, Object>(1000, 0.9f, 1);	
	}
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
    }
		
	/**
	 * Store a sessionID and its corresponding Object in storage. If key exists, record will be updated
	 * @param key byte[] Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	public void put(String key, Object value) {
		session_storage.put(key, value);
	}
	
	/**
	 * Retrieve an object based on the session ID (or null if key does not exists)
	 * @param key byte[] The key to search for.
	 * @return Object The Object found in the storage or null if not found.
	 */
	public Object get(String key) {
		return session_storage.get(key);
	}
	
}

package com.zavedil.apilator.core;

/**
 * Session storage class. 
 * Defines a local session storage.
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

public class SessionStorage {
	// Create initial storage for 1000 sessions, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<String, Object> storage = new ConcurrentHashMap<String, Object>(1000, 0.9f, 1);	
	
	/**
	 * Store a sessionID and its corresponding Object in storage. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	public static void put(String key, Object value) {
		storage.put(key, value);
		
		//TODO: tell the session manager about the update so that he can feed it over the network
	}
	
	/**
	 * Retrieve an object based on the session ID (or null if key does not exists)
	 * @param key String The key to search for.
	 * @return Object The Object found in the storage or null if not found.
	 */
	public static Object get(String key) {
		return storage.get(key);
	}
	
	public static void del(String key) {
		storage.remove(key);
	}
}

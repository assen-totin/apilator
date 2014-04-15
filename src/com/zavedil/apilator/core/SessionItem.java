package com.zavedil.apilator.core;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session item class. 
 * Describes an object stored in the Session Storage against a partifular Session ID.
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

public class SessionItem implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final String className;
	private HashMap<String, Object> session_item = null;
	// Action will be set when sending the object over the network
	private int action = 0;
	// UNIX timestamps of creation, update and TTL
	private final int created;
	private int updated=0;
	private int ttl=0;
	// Session ID for this object
	private final String session_id;
	
	public SessionItem() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		// Creation timestamp
		created = (int) System.currentTimeMillis()/1000;
		session_id = Session.getNewSessionId();
	}
	
	/**
	 * Store a key and a value in this object. If key exists, record will be updated
	 * @param key String Key
	 * @param value Object Value
	 */
	public void put(String key, Object value) {
		session_item.put(key, value);
		updated = (int) System.currentTimeMillis()/1000;
	}
	
	/**
	 * Retrieve a value from this object based on its key (or null if key does not exists)
	 * @param key String The key to search for.
	 * @return Object The value found in the storage or null if key not found.
	 */
	public Object get(String key) {
		return session_item.get(key);
	}
	
	/**
	 * Remove a key/value pair
	 * @param key The key to remove
	 */
	public void del(String key) {
		session_item.remove(key);
		updated = (int) System.currentTimeMillis()/1000;
	}
}

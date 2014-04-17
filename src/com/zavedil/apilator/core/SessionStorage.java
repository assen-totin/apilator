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
	public static ConcurrentHashMap<String, Session> storage = new ConcurrentHashMap<String, Session>(1000, 0.9f, 1);	
	
	// Create internal queue for network updates: 100 objects, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<String, Session> queue = new ConcurrentHashMap<String, Session>(100, 0.9f, 1);	
	
	/**
	 * Store a sessionID and its corresponding Object in storage. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	public static void put(String session_id, Session session) {
		// Only save the session if it does not exists or  
		// if the 'updated' field of the supplied session is newer than the 'updated' field in the existing session  
		if (SessionStorage.saveSession(session)) {
			// Store locally
			storage.put(session_id, session);
			
			// Add to network queue; set the action to ACTION_STORE so that the peers update themselves
			session.setAction(SessionManager.ACTION_STORE);
			queue.put(session_id, session);		
		}
	}
	
	/**
	 * Retrieve an object based on the session ID (or null if key does not exists)
	 * @param key String The key to search for.
	 * @return Object The Object found in the storage or null if not found.
	 */
	public static Session get(String session_id) {
		//FIXME: add network query here if key not found
		return storage.get(session_id);
	}
	
	public static void del(String session_id) {
		Session session;
		
		// Add to network queue; set the action to ACTION_STORE so that the peers delete it too
		session = storage.get(session_id);
		session.setAction(SessionManager.ACTION_DELETE);
		queue.put(session_id, session);
		
		// Remove locally
		storage.remove(session_id);
	}
	
	public static boolean saveSession(Session session) {
		Session session_old;
		session_old = SessionStorage.get(session.getSessionId());
		// If the session does not exists, we should save it - return true
		if (session_old == null)
			return true;
		// If the 'updated' field of the supplied session is newer than the same field from stored session, we should update it - return true
		if (session.getUpdated() > session_old.getUpdated())
			return true;
		
		return false;
	}
}

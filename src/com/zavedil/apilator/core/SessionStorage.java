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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zavedil.apilator.app.*;

public class SessionStorage {
	// Create initial storage for 1000 sessions, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<String, Session> storage = new ConcurrentHashMap<String, Session>(1000, 0.9f, 1);	
		
	public static final String className = "SessionStorage";
		
	/**
	 * Init the session storage from a local cache upon start-up
	 */
	public static void init() {
		// Load the storage from disk
		try {
			InputStream fis = new FileInputStream(Config.SessionManagerDiskCache);
	        ObjectInputStream ois = new ObjectInputStream(fis);			        
	        ConcurrentHashMap<String, Session> readObject = (ConcurrentHashMap<String, Session>) ois.readObject();
			// Copy values to our hash map to preserve the partitioning and density
	        for (Map.Entry<String, Session> entry : readObject.entrySet()) 
	        	storage.put(entry.getKey(), entry.getValue());
			fis.close();
		}
		catch (IOException e) {
			Logger.warning(className, "Could not read disk cache.");
		}
		catch (ClassNotFoundException e) {
			Logger.warning(className, "Disk cache entry could not be read.");
		}
	}
	
	/**
	 * Store a locally generated session Object in storage. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	public static void put(String session_id, Session session) {
		// Update TTL for locally generated/modified sessions
		long now = System.currentTimeMillis();

		// Update session TTL?
		if (Config.SessionCookieExpire > 0)
			// Extend the cookie life with the specified TTL
			session.setTtl(now + Config.SessionCookieExpire);
		else if (Config.SessionCookieExpire < 0) 
			// Set the cookie life to the specified TTL
			session.setTtl(session.getCreated() + (-1 * Config.SessionCookieExpire));
		// else do not touch the TTL
		
		// Changes to save?
		if (saveSession(session_id, session.getUpdated())) {
			// Store locally
			storage.put(session_id, session);
			
			// Add to network queue
			SessionMessage session_message = new SessionMessage(session_id, SessionMessage.ACT_STORE);
			session_message.updated = session.getUpdated();
			session_message.session = session;
			SessionManagerSend.queue_multicast.add(session_message);
		}
	}
	
	/**
	 * Store in storage a session Object received from the network. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	public static void putFromNetwork(Session session) {
		// Only save the session if it does not exists or  
		// if the 'updated' field of the supplied session is newer than the 'updated' field in the existing session  
		if (SessionStorage.saveSession(session.getSessionId(), session.getUpdated()))
			// Store locally
			storage.put(session.getSessionId(), session);
	}
	
	/**
	 * Retrieve an object based on the session ID (or null if key does not exists)
	 * @param key String The key to search for.
	 * @return Object The Object found in the storage or null if not found.
	 */
	public static Session get(String session_id) {
		Session session;
		
		session = storage.get(session_id);
		if (session == null) {
			// Query the network if key not found
			SessionMessage session_message = new SessionMessage(session_id, SessionMessage.ACT_WHOHAS);
			//queue_multicast.put(session_id, session_message);
			SessionManagerSend.queue_multicast.add(session_message);
			
			try {
				Logger.debug(className, "GOING TO SLEEP...");
				Thread.sleep(Config.SessionManagerTimeout);
			} 
			catch (InterruptedException e) {
				// There's little we can if our sleep was interrupted - just go on
				Logger.debug(className, "SLEEP INTERRUPTED!...");
				;
			}
			
			Logger.debug(className, "AWAIKENING...");
			
			// See if we received the object
			session = storage.get(session_id);
		}

		return session;
	}
	
	/**
	 * Delete object from the session storage by its session ID
	 * @param session_id String The session ID to delete. 
	 */
	public static void del(String session_id) {	
		// Add to network queue; set the action to ACTION_STORE so that the peers delete it too
		SessionMessage session_message = new SessionMessage(session_id, SessionMessage.ACT_DELETE);
		//queue_multicast.put(session_id, session_message);
		SessionManagerSend.queue_multicast.add(session_message);
		
		// Remove locally
		storage.remove(session_id);
	}
	
	/**
	 * Check if a session ID exists in the storage.
	 * @param session_id String The session ID to check.
	 * @return boolean TRUE if key exists, FALSE otherwise. 
	 */
	public static boolean exists(String session_id) {
		Session session = SessionStorage.get(session_id);
		if (session == null)
			return false;
		return true;
	}
	
	/** 
	 * Check if a session needs to be added to the storage (i.e. is not present or is older version)
	 * @param session The session to check.
	 * @return TRUE if object should be added, FALSE otherwise.
	 */
	public static boolean saveSession(String session_id, long updated) {
		Session stored_session;
		stored_session = SessionStorage.storage.get(session_id);
		// If the session does not exists, we should save it - return true
		if (stored_session == null)
			return true;
		// If the 'updated' field of the supplied session is newer than the same field from stored session, we should update it - return true
		if (updated > stored_session.getUpdated())
			return true;
		
		return false;
	}
}

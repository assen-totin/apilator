package com.zavedil.apilator.core;

import java.nio.ByteBuffer;
import java.util.HashMap;

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

public class Session implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final String className;
	// Key-value storage
	private HashMap<String, Object> session_item = new HashMap<String, Object>();
	// UNIX timestamps of creation, update and TTL
	private final long created = System.currentTimeMillis();
	private long updated=0;
	private long ttl=0;
	// Session ID for this object
	private final String session_id;
	
	public Session() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		session_id = Session.getNewSessionId();
		updated = created;
	}
	
	/**
	 * Store a key and a value in this object. If key exists, record will be updated
	 * @param key String Key
	 * @param value Object Value
	 */
	public void put(String key, Object value) {
		session_item.put(key, value);
		updated = System.currentTimeMillis();
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
		updated = System.currentTimeMillis();
	}
	
	/**
	 * Update the internal timestamp of the item
	 */
	public void update() {
		updated = System.currentTimeMillis();
	}

	/**
	 * Getter for session_id property
	 * @return int The value of the session_id property
	 */
	public String getSessionId() {
		return session_id;
	}
	
	/**
	 * Getter for created property
	 * @return long The value of the created property
	 */
	public long getCreated() {
		return created;
	}
	
	/**
	 * Getter for updated property
	 * @return long The value of the updated property
	 */
	public long getUpdated() {
		return updated;
	}
	
	/**
	 * Getter for ttl property
	 * @return long The value of the ttl property
	 */
	public long getTtl() {
		return ttl;
	}

	/**
	 * Setter for ttl property
	 * @param act long The value of TTL to set
	 */
	public void setTtl(long new_ttl) {
		ttl = new_ttl;
	}
	
	/**
	 * Create new session ID.
	 * @return byte[] The new session ID
	 * 
	 * The session ID consists of 12 bytes: 4 bytes of a local IP address and 8 bytes of creation timestamp.
	 * The timestamp is in milliseconds and the last 3 bytes of it (which are most random) 
	 * will be used as first-level key when storing the session in-memory
	 */
	public static String getNewSessionId() {
		ByteBuffer curr_time_buffer;
		byte[] curr_time_array=null, ip=null, session_id_bytes=null;
		
		// Get time in milliseconds, convert to byte array
		long curr_time_millis = System.currentTimeMillis();
	    curr_time_buffer = ByteBuffer.allocate(Long.SIZE/8);
	    curr_time_buffer.putLong(curr_time_millis);
	    curr_time_array = curr_time_buffer.array();
		
	    // Get the local IP address as byte array
	    ip = ConfigAuto.ip.getAddress();
		
		// Append time to IP address
		session_id_bytes = new byte[curr_time_array.length + 4];
		System.arraycopy(ip, 0, session_id_bytes, 0, ip.length);
		System.arraycopy(curr_time_array, 0, session_id_bytes, ip.length,curr_time_array.length);
		
		return bytesToHex(session_id_bytes);
	}
	
	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}

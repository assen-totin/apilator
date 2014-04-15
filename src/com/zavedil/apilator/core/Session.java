package com.zavedil.apilator.core;

/**
 * Session class. 
 * Extend it from the class which corresponds to an API entry point. 
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Session {

	/**
	 * Create new session ID.
	 * @return byte[] The new session ID
	 * 
	 * The session ID consists of 12 bytes: 4 bytes of a local IP address and 8 bytes of creation timestamp.
	 * The timestamp is in milliseconds and the last 3 bytes of it (which are most random) 
	 * will be used as first-level key when storing the session in-memory
	 */
	public static byte[] getNewSessionId() {
		String session=null;
		ByteBuffer curr_time_buffer;
		byte[] curr_time_array=null, ip=null, session_id=null;
		
		// Get time in milliseconds, convert to byte array
		long curr_time_millis = System.currentTimeMillis();
	    curr_time_buffer = ByteBuffer.allocate(Long.SIZE);
	    curr_time_buffer.putLong(curr_time_millis);
	    curr_time_array = curr_time_buffer.array();
		
	    // Get the local IP address, convert to bytes array
		try {
			ip = InetAddress.getLocalHost().getAddress();
		} 
		catch (UnknownHostException e) {
			//FIXME: generate some random address?
			//ip = InetAddress.getByAddress("apilator.zavedil.com", "127.0.0.1");
		}
		
		// Append time to IP address
		session_id = new byte[curr_time_array.length + 4];
		System.arraycopy(session_id, 0, ip, 0, ip.length);
		System.arraycopy(session_id, ip.length, curr_time_array, 0, curr_time_array.length);
		
		return session_id;
	}
	
	/**
	 * Convert session ID from byte array to String
	 * @param session_id byte[] The session ID as byte array
	 * @return String The session ID as string
	 */
	public static String sessionIdToString(byte[] session_id) {
		return new String(session_id);
	}
	
	/**
	 * Convert session ID from String to byte array
	 * @param session_id String the session ID as String 
	 * @return byte[] The session ID as byte array
	 */
	public static byte[] sessionIdToBytes(String session_id) {
		return session_id.getBytes();
	}
}

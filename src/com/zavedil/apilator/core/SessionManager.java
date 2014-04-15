package com.zavedil.apilator.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
	public static final int ACTION_UPDATE = 1;	// Used when multicasting an update
	public static final int ACTION_DELETE = 2;	// Used when multicasting a deletion
	public static final int ACTION_WHOHAS = 3;	// Used when asking for the value of the specified key
	public static final int ACTION_ISAT = 4;	// Used when sending a reply to WHO HAS
	
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
	
	/*
      try
      {
         FileOutputStream fileOut = new FileOutputStream("/tmp/employee.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(e);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in /tmp/employee.ser");
      }
      catch(IOException e) {
          i.printStackTrace();
      }
	 */
	
	/*
		try {
			 String msg = "Hello";
			 
			 InetAddress multicast_group = InetAddress.getByName(Config.SessionManagerIp);
			 MulticastSocket multicast_socket = new MulticastSocket(Config.SessionManagerPort);
			 multicast_socket.joinGroup(multicast_group);
			 
			 multicast_socket_out = new ObjectOutputStream(multicast_socket.getOutputStream());
			 multicast_socket_out.writeObject(x);
			 
			 DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), multicast_group, Config.SessionManagerPort);
			 multicast_socket.send(hi);
			 multicast_socket.close();
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to send multicast update");
		}
	 */
}

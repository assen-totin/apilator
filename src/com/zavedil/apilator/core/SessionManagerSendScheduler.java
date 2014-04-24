package com.zavedil.apilator.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import com.zavedil.apilator.app.*;

/**
 * Session manager multicast sending scheduler.  
 * Binds a multicast group and runs the sending as a scheduled task each 10 ms.
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

//public class SessionManagerSendScheduler implements Runnable {
public class SessionManagerSendScheduler {
	private static final String className = "SessionManagerSendScheduler";
	public static final int MAX_PACKET_SIZE= 1500; // Try to fit in single Ethernet packet	
	
	/*
	public SessionManagerSendScheduler() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	*/
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	//public void run() {
	public static void init() {
		Logger.trace(className, "Running new as a new thread.");
		
		InetAddress multicast_group;
		MulticastSocket multicast_socket;
		
		try {
			multicast_group = InetAddress.getByName(Config.SessionManagerMulticastIp);
			multicast_socket = new MulticastSocket(Config.SessionManagerMulticastPort);
			multicast_socket.joinGroup(multicast_group);
			
			Timer time = new Timer();
			SessionManagerSendTask smst = new SessionManagerSendTask(multicast_group, multicast_socket);
			time.schedule(smst, 0, 10);
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to join multicast group");
		}
    }
}

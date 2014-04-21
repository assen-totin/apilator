package com.zavedil.apilator.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;

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

public class SessionManagerSend implements Runnable {
	private final String className;
	public final int MAX_PACKET_SIZE= 1500; // Try to fit in single Ethernet packet	
	
	public SessionManagerSend() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		
		InetAddress multicast_group;
		MulticastSocket multicast_socket;
		DatagramPacket packet;
		byte[] send_buffer;
		
		try {
			multicast_group = InetAddress.getByName(Config.SessionManagerMulticastIp);
			multicast_socket = new MulticastSocket(Config.SessionManagerMulticastPort);
			multicast_socket.joinGroup(multicast_group);

			//while (true) {
				// Check if there are pending outgoing, serialize and send
				for (Map.Entry<String,SessionMessage> pair : SessionStorage.queue_multicast.entrySet()) {
					Logger.debug(className, "Sending multicast...");
			        ByteArrayOutputStream os = new ByteArrayOutputStream(MAX_PACKET_SIZE);
			        ObjectOutputStream oos = new ObjectOutputStream(os);			        
					oos.writeObject(pair.getValue());
					
					send_buffer = os.toByteArray();
					packet = new DatagramPacket(send_buffer, send_buffer.length, multicast_group, Config.SessionManagerMulticastPort);
					multicast_socket.send(packet);
					
			        // Remove from queue
			        SessionStorage.queue_multicast.remove(pair.getKey());
			        
			        // Sleep 10 ms to avoid too high CPU usage
			        Thread.sleep(100);
			    }
			//} 
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to process multicast packet");
		}
		catch (InterruptedException e) {
			Logger.warning(className, "Interrupted thread sleep");
		}

    }
}

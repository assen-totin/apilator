package com.zavedil.apilator.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.zavedil.apilator.app.Config;

/**
 * Multicast queue and sender for Session Storage. 
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

public class SessionManagerSend implements Runnable {
	private final String className;
	private SessionMessage session_message;
	private byte[] send_buffer;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	private InetAddress multicast_group;
	private MulticastSocket socket = null;
	private DatagramPacket packet;
	
	// We prefer LinkedBlockingQueue because it blocks the read until element is available, 
	// thus relieving us from the need to periodically check for new elements or implement notifications.
	public static LinkedBlockingQueue<SessionMessage> queue = new LinkedBlockingQueue<SessionMessage>();
	
	public SessionManagerSend() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		try {
			multicast_group = InetAddress.getByName(Config.SessionManagerMulticastIp);
			socket = new MulticastSocket(Config.SessionManagerMulticastPort);
			socket.joinGroup(multicast_group);
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to join multicast group");
		}
	}
	
	/**
	 * Method to send a Session Message over multicast
	 * @param session_message SessionMessage The message to send
	 */
	public void run() {
		while(true) {
			try {
				session_message = queue.take();
			}
			catch (InterruptedException e) {
				;
			}
			
			if (socket == null)
				continue;

			Logger.debug(className, "SENDING MULTICAST: " + session_message.updated); 
			
			try {
				// Check if there are pending outgoing, serialize and send
		        ObjectOutputStream oos = new ObjectOutputStream(baos);			        
				oos.writeObject(session_message);
				
				send_buffer = baos.toByteArray();
				packet = new DatagramPacket(send_buffer, send_buffer.length, multicast_group, Config.SessionManagerMulticastPort);
				socket.send(packet);       
			}
			catch(IOException e) {
				Logger.warning(className, "Unable to send multicast packet");
			}
		}
	}
}

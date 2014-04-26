package com.zavedil.apilator.core;

/**
 * Session client class. 
 * Connects to another server and retrieves a session object.
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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.zavedil.apilator.app.*;

public class SessionClientUdp implements Runnable {
	private final String className;
	private SessionMessage session_message;
	private byte[] send_buffer;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DatagramSocket socket = null;
	
	// We prefer LinkedBlockingQueue because it blocks the read until element is available, 
	// thus relieving us from the need to periodically check for new elements or implement notifications.
	public static Queue<SessionMessage> queue_get = new LinkedBlockingQueue<SessionMessage>();
	public static Queue<String> queue_isat = new LinkedBlockingQueue<String>();
	
	public SessionClientUdp() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		try {
			socket = new DatagramSocket();
		} 
		catch (SocketException e) {
			Logger.warning(className, "Could not create UDP socket.");
		}
	}
	
	public void run() {	
		Logger.debug(className, "Running in a new thread.");
		try {
			session_message = queue_get.remove();
			
			if (socket == null)
				return;
			
			ObjectOutputStream oos = new ObjectOutputStream(baos);	
			oos.writeObject(session_message);
			
			send_buffer = baos.toByteArray();
			DatagramPacket packet = new DatagramPacket(send_buffer, send_buffer.length, session_message.ip, Config.SessionManagerTcpPort);
			
			socket.send(packet);
			
			Logger.debug(className, "SENDING UNCIAST: " + session_message.type);
		}
		catch (IOException e) {
			Logger.warning(className, "Failed to send session message to peer: " + session_message.ip.toString());
		}
	}
}

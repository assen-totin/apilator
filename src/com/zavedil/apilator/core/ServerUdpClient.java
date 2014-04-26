package com.zavedil.apilator.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A UDP client class.
 * Implemented as a mediator which queues events and pushes them to be send by the UDP server,
 * so that the responses come back to the same channel.
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

public class ServerUdpClient implements Runnable{
	private final String className;
	private final ServerUdp server;
	private SessionMessage session_message;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// We prefer LinkedBlockingQueue because it blocks the read until element is available, 
	// thus relieving us from the need to periodically check for new elements or implement notifications.
	public static LinkedBlockingQueue<SessionMessage> queue = new LinkedBlockingQueue<SessionMessage>();
	
	public ServerUdpClient(ServerUdp server) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		this.server = server;
	}
	
	public void run() {
		Logger.debug(className, "Running in a new thread.");
		
		while(true) {
			// Wait here until a message arrives
			try {
				session_message = queue.take();
			} 
			catch (InterruptedException e1) {
				;
			}
			
			// Serialize the object 
			try {
				ObjectOutputStream oos = new ObjectOutputStream(baos);	
				oos.writeObject(session_message);
			}
			catch (IOException e) {
				Logger.warning(className, "Failed to serialize session message.");
				continue;
			}
			
			server.clientSend (session_message.ip, baos.toByteArray());
		}
	}
}

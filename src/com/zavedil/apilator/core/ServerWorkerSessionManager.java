package com.zavedil.apilator.core;

/**
 * Worker class for the NIO TCP server - Session Manager.
 * @author James Greenfield nio@flat502.com
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original copyright (C) James Greenfield.
 * Modified by the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class ServerWorkerSessionManager implements Runnable {
	private List<ServerDataEvent> queue = new LinkedList<ServerDataEvent>();
	private final String className;
	private boolean busy = false;
	
	/**
	 * Constructor. 
	 * @param sst Thread Handler to the thread that manages the session storage
	 */
	public ServerWorkerSessionManager() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Main function to be called when packet(s) arrive over a SocketChannel
	 * @param server Server The server which originated the packets 
	 * @param socketChannel SocketChannel The SocketChannel (NIO socket) which originated the packets
	 * @param data byte[] The data from the packets
	 * @param count int The number of bytes received
	 * @throws IOException
	 */
	public void processData(Server server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
		busy = true;
		Logger.debug(className, "Entering function processData.");
		
		byte[] response = new byte[]{(byte)0xFF};
		
		InputStream is = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(is);
		SessionMessage msg = null;
		try {
			msg = (SessionMessage)ois.readObject();
		} 
		catch (ClassNotFoundException e) {
			// If we got no object (deserialisation failed), just do nothing
			Logger.warning(className, "Received empty or broken session retrieval request.");
		}	
		
		if (SessionStorage.exists(msg.session_id)) {
			Session session = SessionStorage.get(msg.session_id);
			
			// Serialize session and send back
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(os);			        
			oos.writeObject(session);
			response = os.toByteArray();
		}
	
    	// Push response back
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socketChannel, response));
			queue.notify();
		}
						
		// Ready for new task
		busy = false;
	}
	
	/**
	 * The main loop of the worker thread
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		ServerDataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} 
					catch (InterruptedException e) {
					}
				}
				dataEvent = (ServerDataEvent) queue.remove(0);
			}
			
			// Return to sender
			dataEvent.server.send(dataEvent.socket, dataEvent.data);
		}
	}
	
	/**
	 * Getter for the 'busy' property
	 * @return
	 */
	public boolean isBusy() {
		return busy;
	}
}
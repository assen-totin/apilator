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
import java.io.StreamCorruptedException;
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
	private final long created = System.currentTimeMillis();
	private boolean busy = false;
	private long exec_time = 0;
	private long requests = 0;
	
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
		long run_start_time = System.currentTimeMillis();
		Logger.debug(className, "Entering function processData.");
		
		byte[] response;		
		InputStream is = new ByteArrayInputStream(data);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		ObjectInputStream ois = new ObjectInputStream(is);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		SessionMessage sm_in = null;
		
		try {
			sm_in = (SessionMessage)ois.readObject();
		} 
		catch (ClassNotFoundException e) {
			// If we got no object (deserialisation failed), just do nothing
			Logger.warning(className, "Received empty or broken session retrieval request.");
		}
		catch (StreamCorruptedException e) {
			// This exception means there is more data yet to come, so just return for now.
			return;
		}
		
		Logger.debug(className, "GOT UNICAST WITH TYPE: " + sm_in.type);
		
		// Process a STORE request for an object
		if (sm_in.type == SessionMessage.ACT_POST) {
			if (SessionStorage.saveSession(sm_in.session.getSessionId(), sm_in.session.getUpdated()))
				SessionStorage.putFromNetwork(sm_in.session);
			
			SessionMessage sm_noop = new SessionMessage(sm_in.session_id, SessionMessage.ACT_NOOP);
			// Serialize session and send back		        
			oos.writeObject(sm_noop);
		}
	
		// Process a GET request for an object ID, return the object if found
		else if ((sm_in.type == SessionMessage.ACT_GET) && SessionStorage.exists(sm_in.session_id)) {
			SessionMessage sm_store = new SessionMessage(sm_in.session_id, SessionMessage.ACT_POST);
			sm_store.session = SessionStorage.get(sm_in.session_id);
			// Serialize session and send back		        
			oos.writeObject(sm_store);
		}
	
		/*
		// Process ISAT response to a multicast WHOHAS - ask the originator to send us the session using ACT_GET
		else if (sm_in.type == SessionMessage.ACT_ISAT) {
			SessionMessage sm_get = new SessionMessage(sm_in.session_id, SessionMessage.ACT_GET);
			oos.writeObject(sm_get);
		}
		*/
		
    	// Push response back
		response = os.toByteArray();
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socketChannel, response));
			queue.notify();
		}

		// Stats
		if (ServerStats.sm_requests.containsKey(created)) {
			requests = ServerStats.sm_requests.get(created);
			requests ++;			
		}
		ServerStats.sm_requests.put(created, requests);
		
		exec_time = System.currentTimeMillis() - run_start_time;
		if (ServerStats.http_exec.containsKey(created))
			exec_time += ServerStats.http_exec.get(created);
		ServerStats.sm_exec.put(created, exec_time);
				
		// Ready for new task
		busy = false;
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
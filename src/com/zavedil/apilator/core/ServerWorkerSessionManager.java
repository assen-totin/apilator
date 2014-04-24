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
		ObjectInputStream ois = new ObjectInputStream(is);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		SessionMessage msg = null;
		
		try {
			msg = (SessionMessage)ois.readObject();
		} 
		catch (ClassNotFoundException e) {
			// If we got no object (deserialisation failed), just do nothing
			Logger.warning(className, "Received empty or broken session retrieval request.");
		}
		catch (StreamCorruptedException e) {
			// This exception means there is more data yet to come, so just return for now.
			return;
		}
		
		Logger.debug(className, "GOT UNICAST WITH TYPE: " + msg.type);
		
		// Process a GET request for an object ID, return the object if found
		if ((msg.type == SessionMessage.ACT_GET) && SessionStorage.exists(msg.session_id)) {
			Session session = SessionStorage.get(msg.session_id);
			// Serialize session and send back		        
			oos.writeObject(session);
		}
		
		// Process ISAT response to a multicast WHOHAS, fetch the object from the originator using GET, then return ACT_NOOP
		if (msg.type == SessionMessage.ACT_ISAT) {
			SessionMessage msg_out = new SessionMessage(msg.session_id, SessionMessage.ACT_GET);
			SessionClient sc = new SessionClient(msg.ip, msg_out);
			// Send the SessionMessage and expect a Session back
			if (sc.send(SessionClient.MSG_TYPE_SESSION)) {
				Session new_session = sc.getSession();
				// Make sure we still don't have the session (or it is a lower revision), then save it directly
				SessionStorage.putFromNetwork(new_session);
			}
			
			SessionMessage msg_out2 = new SessionMessage(msg.session_id, SessionMessage.ACT_NOOP);		        
			oos.writeObject(msg_out2);
		}
	
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
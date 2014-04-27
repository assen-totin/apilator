package com.zavedil.apilator.core;

/**
 * Worker class for the NIO TCP server.
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
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import com.zavedil.apilator.app.*;

public class ServerTcpWorkerSm implements Runnable {
	private List<ServerTcpDataEvent> queue = new LinkedList<ServerTcpDataEvent>();
	private final String className;
	private final long created = System.currentTimeMillis();
	private long exec_time = 0;
	private long requests = 1;
	
	/**
	 * Constructor. 
	 * @param sst Thread Handler to the thread that manages the session storage
	 */
	public ServerTcpWorkerSm() {
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
	public void queueData(ServerTcp server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
	    byte[] dataCopy = new byte[count];
	    System.arraycopy(data, 0, dataCopy, 0, count);
		
		synchronized(queue) {
			queue.add(new ServerTcpDataEvent(server, socketChannel, dataCopy));
			queue.notify();
		}
	}
		
	private byte[] processData(byte[] data, String ip) {		
		long run_start_time = System.currentTimeMillis();
		
		Logger.debug(className, "Entering function processData.");
		
		byte[] response = null;	
		SessionMessage session_message = null;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = null;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		
		try {
			ois = new ObjectInputStream(bais);
			session_message = (SessionMessage)ois.readObject();
		}
        catch (StreamCorruptedException e) {
            // This exception means there is more data yet to come, so just return for now.
            return null;
        }
		catch (IOException e) {
			Logger.warning(className, "Unable to read TCP packet");
			return null;
		}
		catch (ClassNotFoundException e) {
			Logger.warning(className, "Unable to process inbound TCP packet");
			return null;
		}
		
		// Process the message
		if (session_message.type == SessionMessage.ACT_GET) {
			Session session = SessionStorage.storage.get(session_message.session_id);
			if (session != null) {
				session_message.type = SessionMessage.ACT_POST;
				session_message.session = session;
				
				try {
					oos = new ObjectOutputStream(baos);
					oos.writeObject(session_message);
				} 
				catch (IOException e) {
					Logger.warning(className, "Unable to serialize object to TCP.");
					return null;
				}
				
				response = baos.toByteArray();
			}	
		}
	
		// Stats
		if (ServerStats.sm_requests.containsKey(created)) {
			requests = ServerStats.sm_requests.get(created);
			requests ++;			
		}
		ServerStats.sm_requests.put(created, requests);
		
		exec_time = System.currentTimeMillis() - run_start_time;
		if (ServerStats.sm_exec.containsKey(created))
			exec_time += ServerStats.sm_exec.get(created);
		ServerStats.sm_exec.put(created, exec_time);
		
		return response;
	}
	
	/**
	 * The main loop of the worker thread
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		ServerTcpDataEvent dataEvent;
		
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
				dataEvent = (ServerTcpDataEvent) queue.remove(0);
			}
			
			// Process and return to sender
			byte[] res = processData(dataEvent.data, dataEvent.socket.socket().getInetAddress().getHostAddress());
			if (res != null)
				dataEvent.server.send(dataEvent.socket, res);
		}
	}
		
	/**
	 * Getter for the size of the queue
	 * @return int The size of the queue
	 */
	public int getQueueSize() {
		return queue.size();
	}
}
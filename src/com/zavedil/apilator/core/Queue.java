package com.zavedil.apilator.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * Queue class.
 * Instantiated for HTTP and SessionManager queues.
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

public class Queue {
	private final String className;
	private LinkedList<ServerTcpDataEvent> queue = new LinkedList<ServerTcpDataEvent>();
	
	public Queue() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Enqueue function to be called when packet(s) arrive over a SocketChannel
	 * @param server Server The server which originated the packets 
	 * @param socketChannel SocketChannel The SocketChannel (NIO socket) which originated the packets
	 * @param data byte[] The data from the packets
	 * @param count int The number of bytes received
	 * @throws IOException
	 */
	public void enqueue(ServerTcp server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
	    byte[] dataCopy = new byte[count];
	    System.arraycopy(data, 0, dataCopy, 0, count);
	
	    // Add the job to the queue, notify worker threads about it
		synchronized(this) {
			queue.add(new ServerTcpDataEvent(server, socketChannel, dataCopy));
			this.notify();
		}	    	    
	}
	
	/**
	 * Get one event from the queue
	 * @param serverType int The type of the queue to use (HTTP or SessionManager)
	 * @return ServerTcpDataEvent The event retrieved or null if queue is empty
	 */
	
	public ServerTcpDataEvent dequeue() {
		ServerTcpDataEvent event = null;
		
		synchronized(this) {
			if (! queue.isEmpty())
				event = queue.remove(0);	
		}

		return event;
	}
	
	
	/**
	 * Return the size of the queue
	 * @return int The size of the queue
	 */
	
	public boolean isEmpty() {
		synchronized(this) {
			return queue.isEmpty();
		}
	}
}

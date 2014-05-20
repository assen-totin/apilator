package com.zavedil.apilator.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

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

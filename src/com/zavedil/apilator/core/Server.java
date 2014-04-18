package com.zavedil.apilator.core;

/**
 * TCP server class using NIO.
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

public class Server implements Runnable {
	private String className;
	
	// The host:port combination to listen on
	private InetAddress hostAddress;
	private int port;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private int byteBufSize = 8192;

	// Arrays of workers (for HTTP and SessionManager)
	private List<ServerWorkerHttp> workers_http = new LinkedList<ServerWorkerHttp>();
	private List<ServerWorkerSessionManager> workers_sm = new LinkedList<ServerWorkerSessionManager>();

	// A list of PendingChange instances
	private List<ServerChangeRequest> pendingChanges = new LinkedList<ServerChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();
	
	// Operaton modes
	private final static int MODE_NONE = 0;
	private final static int MODE_HTTP = 1;
	private final static int MODE_SESSION_MANAGER = 2;
	private final int mode;

	/**
	 * Constructor for HTTP server
	 * @param hostAddress InetAddress Network address to bind to.
	 * @param port int TCP port to bind to.
	 * @param worker ServerWorkerHttp Initial worker to add to the pool
	 * @throws IOException
	 */
	public Server(int mode, InetAddress hostAddress, int port, ServerWorkerHttp worker) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.mode = mode;
		this.selector = this.initSelector();
		workers_http.add(worker);
	}

	// Declare the server for HTTP
	public Server(int mode, InetAddress hostAddress, int port, ServerWorkerSessionManager worker) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.mode = mode;
		this.selector = this.initSelector();
		workers_sm.add(worker);
	}
	
	/**
	 * Method to send data (reponse) to the remote client
	 * @param socket SocketChannel The SocketChannel (NIO socket) to write to 
	 * @param data byte[] The data to send
	 */
	public void send(SocketChannel socket, byte[] data) {
		Logger.debug(className, "Entering function send");
		
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ServerChangeRequest(socket, ServerChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List queue = (List) this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}

	/**
	 * The main loop for the server
	 */
	public void run() {
		className = this.getClass().getSimpleName();
		
		while (true) {
			try {
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ServerChangeRequest change = (ServerChangeRequest) changes.next();
						switch (change.type) {
						case ServerChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.accept(key);
					} 
					else if (key.isReadable()) {
						this.read(key);
					} 
					else if (key.isWritable()) {
						this.write(key);
					}
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method to accept a new inbound connection
	 * @param key SelectionKey The key, associated with the connection
	 * @throws IOException
	 */
	private void accept(SelectionKey key) throws IOException {
		Logger.debug(className, "Entering function accept");
		
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		//Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ, ByteBuffer.allocate(byteBufSize));
	}

	/**
	 * Method to read data form an incoming connection
	 * @param key  SelectionKey The key, associated with the connection
	 * @throws IOException
	 */
	private void read(SelectionKey key) throws IOException {
		Logger.debug(className, "Entering function read");
		
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		// Allocate a new buffer for this read
		ByteBuffer newBuffer = ByteBuffer.allocate(byteBufSize);

		// Attempt to read off the channel
		int numRead;
		try {
			//numRead = socketChannel.read(this.readBuffer);
			numRead = socketChannel.read(newBuffer);
		} 
		catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}
	
		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Fetch the stored buffer
		ByteBuffer readBuffer = (ByteBuffer) key.attachment();
		
		// See if need to expand the buffer
		ByteBuffer tmpBuffer = ByteBuffer.allocate(readBuffer.position() + byteBufSize);
		readBuffer.flip();
		tmpBuffer.put(readBuffer);
		newBuffer.flip();
		tmpBuffer.put(newBuffer);
		int buffer_pos = tmpBuffer.position();
		key.attach(tmpBuffer);
		tmpBuffer.flip();
		
		// Hand the data off to a worker thread
		boolean got_worker = false;

		// Worker threads for HTTP
		if (this.mode == Server.MODE_HTTP) {
			for (ServerWorkerHttp entry : workers_http) {
				if (!entry.isBusy()) {
					got_worker = true;
					entry.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
				}
			}
			if (!got_worker) {
				// Span a new worker thread, add it to the pool
				ServerWorkerHttp new_worker = new ServerWorkerHttp();
				new Thread(new_worker).start();
				workers_http.add(new_worker);
				new_worker.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
			}			
		}
		
		// Worker threads for Session Manager
		if (this.mode == Server.MODE_SESSION_MANAGER) {
			for (ServerWorkerSessionManager entry : workers_sm) {
				if (!entry.isBusy()) {
					got_worker = true;
					entry.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
				}
			}
			if (!got_worker) {
				// Span a new worker thread, add it to the pool
				ServerWorkerSessionManager new_worker = new ServerWorkerSessionManager();
				new Thread(new_worker).start();
				workers_sm.add(new_worker);
				new_worker.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
			}			
		}
		
		
		//this.worker.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
		
		// Restore the tmpBuffer to its original position (because it is now attached to the key)
		tmpBuffer.position(buffer_pos);
	}

	/**
	 * Method to write data (response) to a connection 
	 * @param key  SelectionKey The key, associated with the connection
	 * @throws IOException
	 */
	private void write(SelectionKey key) throws IOException {
		Logger.debug(className, "Entering function write");
		
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				try {
					socketChannel.write(buf);					
				}
				catch(IOException e) {
					queue.remove(0);
					Logger.error(className, "OOOPS while writing back to socket! This should not happen.");
				}

				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
				
				// Remove the attachment.
				key.attachment();
			}
		}
	}

	/**
	 * Selector initialization method
	 * @return Selector The socket selector to use 
	 * @throws IOException
	 */
	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	/**
	 * Main entry point for the server
	 * @param args String[] Command-line arguments, if any
	 */
	public static void main(String[] args) {
		try {		
			// Start the session storage manager thread
			SessionManager session_manager = new SessionManager();
			Thread session_manager_t = new Thread(session_manager);
			session_manager_t.start();
			
			// Start one worker for HTTP requests
			ServerWorkerHttp worker = new ServerWorkerHttp();
			new Thread(worker).start();
			
			// Start the HTTP server
			new Thread(new Server(Server.MODE_HTTP, null, Config.TcpPort, worker)).start();
			
			// Start one worker for Session Manager requests
			ServerWorkerSessionManager worker_sm = new ServerWorkerSessionManager();
			new Thread(worker_sm).start();
			
			// Start the Session Manager server 
			new Thread(new Server(Server.MODE_SESSION_MANAGER, null, Config.SessionManagerTcpPort, worker_sm)).start();

			//Stats: uptime
			ServerStats.server_boottime = System.currentTimeMillis();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
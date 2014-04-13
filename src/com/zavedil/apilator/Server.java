package com.zavedil.apilator;

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
	//private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	private int byteBufSize = 8192;

	private Worker worker;

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();

	public Server(InetAddress hostAddress, int port, Worker worker) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.worker = worker;
	}

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

	private void accept(SelectionKey key) throws IOException {
		Logger.debug(className, "Entering function accept");
		
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ, ByteBuffer.allocate(byteBufSize));
	}

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
		
		// Hand the data off to our worker thread
		tmpBuffer.flip();
		this.worker.processData(this, socketChannel, tmpBuffer.array(), buffer_pos);
		tmpBuffer.position(buffer_pos);
	}

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

	public static void main(String[] args) {
		try {
			Worker worker = new Worker();
			new Thread(worker).start();
			new Thread(new Server(null, 8080, worker)).start();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
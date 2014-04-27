package com.zavedil.apilator.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.zavedil.apilator.app.Config;

/**
 * Multicast queue and sender for Session Storage. 
 * Defines a local session storage.
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

public class ClientTcp implements Runnable {
	private final String className;
	private SessionMessage sm_in, sm_out;
	private Socket socket = null;
	
	// We prefer LinkedBlockingQueue because it blocks the read until element is available, 
	// thus relieving us from the need to periodically check for new elements or implement notifications.
	public static LinkedBlockingQueue<SessionMessage> queue = new LinkedBlockingQueue<SessionMessage>();
	
	public ClientTcp() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Method to send a Session Message over multicast
	 * @param session_message SessionMessage The message to send
	 */
	public void run() {
		while(true) {
			try {
				sm_out = queue.take();
			}
			catch (InterruptedException e) {
				continue;
			}
			
			try {
				socket = new Socket(sm_out.ip.toString(), Config.SessionManagerTcpPort);
			}
			catch (IOException e) {
				Logger.warning(className, "Unable to create TCP socket");
				continue;
			}
			
			Logger.debug(className, "SENDING TCP GET: " + sm_out.updated); 
			
			try {
				// Serialize and send
		        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());			        
				oos.writeObject(sm_out);
			}
			catch(IOException e) {
				Logger.warning(className, "Unable to send TCP packet");
				continue;
			}
			
			// Fetch a response
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				sm_in = (SessionMessage) ois.readObject();
				
				Logger.debug(className, "RECEIVED TCP POST: " + sm_in.updated); 
				
				if (sm_in.type == SessionMessage.ACT_POST)
					SessionStorage.putFromNetwork(sm_in.session);
			}
			catch (IOException e) {
				Logger.warning(className, "Unable to receive TCP packet");
				continue;
			}
			catch (ClassNotFoundException e) {
				Logger.warning(className, "Unable to deserialize TCP packet");
				continue;
			}
		}
	}
}

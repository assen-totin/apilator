package com.zavedil.apilator.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;

/**
 * Session manager class. 
 * Updates the local session storage from network.
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

public class SessionManager implements Runnable {
	private final String className;
	public final int MAX_PACKET_SIZE= 1500; // Try to fit in single Ethernet packet	
	
	public SessionManager() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		
		InetAddress multicast_group;
		MulticastSocket multicast_socket;
		DatagramPacket packet;
		byte[] receive_buffer, send_buffer;
		
		try {
			multicast_group = InetAddress.getByName(Config.SessionManagerMulticastIp);
			multicast_socket = new MulticastSocket(Config.SessionManagerMulticastPort);
			multicast_socket.joinGroup(multicast_group);
			
			while (true) {
				// Read and unserialize incoming packets
				receive_buffer = new byte[MAX_PACKET_SIZE];
				packet = new DatagramPacket(receive_buffer, receive_buffer.length);
				
				multicast_socket.receive(packet);
				InputStream is = new ByteArrayInputStream(packet.getData());
				ObjectInputStream ois = new ObjectInputStream(is);
				SessionMessage msg = (SessionMessage)ois.readObject();	
				processIncoming(msg);
				
				// Check if there are pending outgoing, serialize and send
				for (Map.Entry<String,SessionMessage> pair : SessionStorage.queue_multicast.entrySet()) {
			        ByteArrayOutputStream os = new ByteArrayOutputStream(MAX_PACKET_SIZE);
			        ObjectOutputStream oos = new ObjectOutputStream(os);			        
					oos.writeObject(pair.getValue());
					
					send_buffer = os.toByteArray();
					packet = new DatagramPacket(send_buffer, send_buffer.length);
					multicast_socket.send(packet);
					
			        // Remove from queue
			        SessionStorage.queue_multicast.remove((String) pair.getKey());
			    }
			} 
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to process multicast packet");
		}
		catch (ClassNotFoundException e) {
			Logger.warning(className, "Unable to process inbound multicast packet");
		}
    }
	
	private void processIncoming(SessionMessage message) {
		switch(message.type) {
			case SessionMessage.MSG_STORE:
			case SessionMessage.MSG_ISAT:
				//FIXME: add code to retrieve the specified object from the peer
				//Provide the session ID to ask for and the IP object of the peer
				//SessionStorage.putFromNetwork(message.session_id, session);
				break;
			case SessionMessage.MSG_DELETE:
				SessionStorage.del(message.session_id);
				break;
			case SessionMessage.MSG_WHOHAS:
				if (SessionStorage.exists(message.session_id)) {
					SessionMessage response = new SessionMessage(message.session_id, SessionMessage.MSG_ISAT);
					SessionStorage.queue_multicast.put(message.session_id, response);
				}
				break;
		}
	}
}

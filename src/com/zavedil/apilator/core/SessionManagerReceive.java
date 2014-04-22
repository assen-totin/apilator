package com.zavedil.apilator.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import com.zavedil.apilator.app.*;

/**
 * Session manager receiving class. 
 * Listens at multicast address for inbound messages; 
 * retrieves updates for session storage using unciast.
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

public class SessionManagerReceive implements Runnable {
	private final String className;
	public final int MAX_PACKET_SIZE= 1500; // Try to fit in single Ethernet packet	
	
	public SessionManagerReceive() {
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
		byte[] receive_buffer;
		
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
				
			} 
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to bind to multicast packet");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			Logger.warning(className, "Unable to process inbound multicast packet");
		}
    }
	
	private void processIncoming(SessionMessage message) {
		SessionMessage msg_out;
		SessionClient sc;
		
		switch(message.type) {
			case SessionMessage.ACT_STORE:
				// First check if we already have this or later version before requesting
				if (SessionStorage.saveSession(message.session_id, message.updated)) {
					// Fetch the session from the peer using unicast
					msg_out = new SessionMessage(message.session_id, SessionMessage.ACT_GET);
					sc = new SessionClient(message.ip, msg_out);
					// Send the SessionMessage and expect a Session back
					if (sc.send(SessionClient.MSG_TYPE_SESSION)) {
						Session new_session = sc.getSession();
						SessionStorage.putFromNetwork(new_session.getSessionId(), new_session);
					}					
				}
				break;
			case SessionMessage.ACT_DELETE:
				// Delete the session from local storage
				SessionStorage.del(message.session_id);
				break;
			case SessionMessage.ACT_WHOHAS:
				// If we have this session, send back a unicast reply that we have it
				if (SessionStorage.exists(message.session_id)) {
					msg_out = new SessionMessage(message.session_id, SessionMessage.ACT_ISAT);
					sc = new SessionClient(message.ip, msg_out);
					// Send the SessionMessage and expect a SessionMessage back
					// (we don't care for it so won't fetch it)
					sc.send(SessionClient.MSG_TYPE_SESSION_MESSAGE);
				}
				break;
		}
	}
}

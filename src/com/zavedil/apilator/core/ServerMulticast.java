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

public class ServerMulticast implements Runnable {
	private final String className;
	
	private final SessionStorage sessionStorage;
	
	private InetAddress multicast_group;
	private MulticastSocket socket;
	private byte[] receive_buffer = new byte[Config.SessionSize];
	private DatagramPacket packet = new DatagramPacket(receive_buffer, receive_buffer.length);	
	
	public ServerMulticast(SessionStorage ss) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		sessionStorage = ss;
		
		try {
			multicast_group = InetAddress.getByName(Config.SessionManagerMulticastIp);
			socket = new MulticastSocket(Config.SessionManagerMulticastPort);
			socket.joinGroup(multicast_group);
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to join multicast group");
		}
	}
	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		
		while(true) {
			try {
				// Prepare to read and unserialize incoming packets: we can reuse these
				socket.receive(packet);
				InputStream is = new ByteArrayInputStream(packet.getData());
				
				// We need new ObjectInputStream for each datagram				
				ObjectInputStream ois = new ObjectInputStream(is);
				SessionMessage session_message = (SessionMessage)ois.readObject();	
				processIncoming(session_message);
			}
			catch (IOException e) {
				Logger.warning(className, "Unable to bind to multicast packet");
				//e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				Logger.warning(className, "Unable to process inbound multicast packet");
			}
		}
    }
	
	private void processIncoming(SessionMessage message) {	
		// If we are the sender, just return
		if (message.ip.equals(ConfigAuto.ip))
			return;
		
		Logger.debug(className, "GOT MULTICAST WITH TYPE: " + message.type);
		Logger.debug(className, "GOT MULTICAST WITH SESSION_ID: " + message.session_id);
		
		switch(message.type) {
			case SessionMessage.ACT_AVAIL:
				// Queue a message to retrieve this session
				message.ip_remote = message.ip;
				message.ip = ConfigAuto.ip;
				message.type = SessionMessage.ACT_GET;
				ClientTcp.queue.add(message);
				break;
			case SessionMessage.ACT_DELETE:
				// Delete the session from local storage
				sessionStorage.del(message.session_id);
				break;
			case SessionMessage.ACT_WHOHAS:
				// If we have this session, queue a unicast response that we have it (adding its updated timestamp)
				if (sessionStorage.exists(message.session_id)) {
					message.ip_remote = message.ip; 
					message.ip = ConfigAuto.ip;
					message.type = SessionMessage.ACT_ISAT;
					ClientMulticast.queue.add(message);
				}
				break;
			case SessionMessage.ACT_ISAT:
				if (sessionStorage.saveSession(message.session_id, message.updated)) {
					message.ip_remote = message.ip;
					message.ip = ConfigAuto.ip;
					message.type = SessionMessage.ACT_GET;
					ClientTcp.queue.add(message);
				}
		}
	}
}

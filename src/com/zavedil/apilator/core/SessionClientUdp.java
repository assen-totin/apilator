package com.zavedil.apilator.core;

/**
 * Session client class. 
 * Connects to another server and retrieves a session object.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.zavedil.apilator.app.*;

public class SessionClientUdp {
	private final String className;
	private final InetAddress ip;
	private SessionMessage session_message;
	
	public SessionClientUdp(InetAddress ipaddr, SessionMessage msg) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		ip = ipaddr;
		session_message = msg;
	}
	
	public boolean send() {
		boolean res = true;
		
		try {
			byte[] send_buffer;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);	
			oos.writeObject(session_message);
			
			send_buffer = baos.toByteArray();
			DatagramPacket packet = new DatagramPacket(send_buffer, send_buffer.length, ConfigAuto.ip, Config.SessionManagerTcpPort);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();
			 
			Logger.debug(className, "SENDING UNCIAST: " + session_message.type);
		}
		catch (IOException e) {
			Logger.warning(className, "Failed to send session message to peer: " + ip.toString());
			res = false;
		}
		
		return res;
	}
}

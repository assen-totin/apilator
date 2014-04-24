package com.zavedil.apilator.core;

/**
 * Session manager multicast sending task.  
 * The task is run by the multicast sending scheduler each 10 ms.
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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.TimerTask;
import com.zavedil.apilator.app.*;

public class SessionManagerSendTask extends TimerTask {
	private final String className;
	
	InetAddress multicast_group;
	MulticastSocket multicast_socket;
	DatagramPacket packet;
	byte[] send_buffer;
	
	public SessionManagerSendTask(InetAddress mg, MulticastSocket ms) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		multicast_group = mg;
		multicast_socket = ms;
	}
	
	public void run() {
		try {
			// Check if there are pending outgoing, serialize and send
			for (Map.Entry<String,SessionMessage> pair : SessionStorage.queue_multicast.entrySet()) {
		        ByteArrayOutputStream os = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream(os);			        
				oos.writeObject(pair.getValue());
				
				send_buffer = os.toByteArray();
				packet = new DatagramPacket(send_buffer, send_buffer.length, multicast_group, Config.SessionManagerMulticastPort);
				multicast_socket.send(packet);
				
				Logger.debug(className, "SENDING MULTICAST: " + pair.getKey());
				
		        // Remove from queue
		        SessionStorage.queue_multicast.remove(pair.getKey());	        
		    }
		}
		catch(IOException e) {
			Logger.warning(className, "Unable to send multicast packet");
		}
	}
}

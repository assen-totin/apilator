package com.zavedil.apilator.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Iterator;
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
	public final int MAX_PACKET_SIZE= 8192;		
	public static final int ACTION_STORE = 1;	// Used when multicasting an update
	public static final int ACTION_DELETE = 2;	// Used when multicasting a deletion
	public static final int ACTION_WHOHAS = 3;	// Used when asking for the value of the specified key
	public static final int ACTION_ISAT = 4;	// Used when sending a reply to WHO HAS
	
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
			multicast_group = InetAddress.getByName(Config.SessionManagerIp);
			multicast_socket = new MulticastSocket(Config.SessionManagerPort);
			multicast_socket.joinGroup(multicast_group);
			
			while (true) {
				// Read and unserialize incoming packets
				receive_buffer = new byte[MAX_PACKET_SIZE];
				packet = new DatagramPacket(receive_buffer, receive_buffer.length);
				
				multicast_socket.receive(packet);
				InputStream is = new ByteArrayInputStream(packet.getData());
				ObjectInputStream ois = new ObjectInputStream(is);
				Session obj = (Session)ois.readObject();	
				processIncoming(obj);
				
				// Check if there are pending outgoing, serialize and send
			    Iterator iterator = SessionStorage.queue.entrySet().iterator();
			    while (iterator.hasNext()) {
			        Map.Entry pair = (Map.Entry)iterator.next();
			        iterator.remove(); // avoids a ConcurrentModificationException
			        			
			        ByteArrayOutputStream os = new ByteArrayOutputStream(MAX_PACKET_SIZE);
			        ObjectOutputStream oos = new ObjectOutputStream(os);			        
					oos.writeObject(pair.getValue());
					
					send_buffer = os.toByteArray();
					packet = new DatagramPacket(send_buffer, send_buffer.length);
					multicast_socket.send(packet);
					
			        // Remove from queue
			        SessionStorage.queue.remove((String) pair.getKey());
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
	
	private void processIncoming(Session obj) {
		switch(obj.getAction()) {
			case ACTION_STORE:
				put(obj.getSessionId(), obj);
				break;
			case ACTION_DELETE:
				del(obj.getSessionId());
				break;
			case ACTION_WHOHAS:
				//TODO: send back the requested object (via Unicast?) 
				break;
		}
	}
	
	/**
	 * Store a sessionID and its corresponding Object in storage. If key exists, record will be updated
	 * @param key String Session ID, used as key
	 * @param value Object The Object to store associated with the key
	 */
	private void put(String key, Session value) {
		SessionStorage.put(key, value);
	}
	
	/**
	 * Delete the specified item
	 * @param key String Session ID, used as key
	 */
	private void del(String key) {
		SessionStorage.del(key);
	}
	
	/*
      try
      {
         FileOutputStream fileOut = new FileOutputStream("/tmp/employee.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(e);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in /tmp/employee.ser");
      }
      catch(IOException e) {
          i.printStackTrace();
      }
	 */
	
	/*
		try {
			 String msg = "Hello";
		 
			 multicast_socket_out = new ObjectOutputStream(multicast_socket.getOutputStream());
			 multicast_socket_out.writeObject(x);
			 
			 DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), multicast_group, Config.SessionManagerPort);
			 multicast_socket.send(hi);
			 multicast_socket.close();
		}
		catch (IOException e) {
			Logger.warning(className, "Unable to send multicast update");
		}
	 */
}

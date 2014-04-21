package com.zavedil.apilator.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.TimerTask;

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
				
		        // Remove from queue
		        SessionStorage.queue_multicast.remove(pair.getKey());	        
		    }
		}
		catch(IOException e) {
			Logger.warning(className, "Unable to send multicast packet");
		}
	}
}

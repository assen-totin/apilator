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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import com.zavedil.apilator.app.*;

public class SessionClient {
	private final String className;
	private final InetAddress ip;
	private SessionMessage sm_out, sm_in;
	private Session session;

	public SessionClient(InetAddress ipaddr, SessionMessage msg) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		ip = ipaddr;
		sm_out = msg;
	}
	
	public boolean send() {
		boolean res = true;
		
		try {
			  Socket socket = new Socket(ip, Config.SessionManagerTcpPort);
			  ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			  oos.writeObject(sm_out);
			  
			  Logger.debug(className, "SENDING UNCIAST: " + sm_out.type);
			  
			  ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			  try {
				  sm_in = (SessionMessage)ois.readObject();
			  }
			  catch(ClassNotFoundException e) {
				  Logger.warning(className, "Incorrect session received from peer: " + ip.toString());
				  res = false;
			  }

			  switch(sm_in.type) {
			  case SessionMessage.ACT_STORE:
				  // We have received a session, make it available downstream
				  session = sm_in.session;
				  break;
			/*
			  case SessionMessage.ACT_GET:
				  // We have received invitation to get the session
				  break;
			*/
			  }
			  
			  socket.close();			
		}
		catch (IOException e) {
			Logger.warning(className, "Failed to obtain session from peer: " + ip.toString());
			res = false;
		}
		
		return res;
	}
	
	public Session getSession() {
		return session;
	}
	
	public SessionMessage getSessionMessage() {
		return sm_in;
	}
}

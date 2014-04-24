package com.zavedil.apilator.core;

/**
 * Main entry point for Apilator.
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
import com.zavedil.apilator.app.Config;

public class Main {
	
	/**
	 * Main entry point for the Apliator
	 * @param args String[] Command-line arguments, if any
	 */
	public static void main(String[] args) {
		try {
			// Automatic configuration
			new ConfigAuto();
			
			// Init the session storage (load form disk cache)
			SessionStorage.init();
			
			// Start Session Manager Clean-upper thread
			/*
			SessionManagerCleanup smcs = new SessionManagerCleanup();
			Thread smcs_t = new Thread (smcs);
			smcs_t.start();
			*/
			SessionManagerCleanup.init();
			
			// Start the session storage manager thread for sending
			/*
			SessionManagerSendScheduler sm_send = new SessionManagerSendScheduler();
			Thread sm_send_t = new Thread(sm_send);
			sm_send_t.start();
			*/
			SessionManagerSendScheduler.init();
					
			// Start the session storage manager thread for receiving
			SessionManagerReceive sm_receive = new SessionManagerReceive();
			Thread sm_receive_t = new Thread(sm_receive);
			sm_receive_t.start();
								
			// Start the Session Manager server 
			//new Thread(new Server(Server.MODE_SESSION_MANAGER, null, Config.SessionManagerTcpPort, worker_sm)).start();
			new Thread(new Server(Server.MODE_SESSION_MANAGER, null, Config.SessionManagerTcpPort)).start();
					
			// Start the HTTP server
			//new Thread(new Server(Server.MODE_HTTP, null, Config.TcpPort, worker)).start();
			new Thread(new Server(Server.MODE_HTTP, null, Config.TcpPort)).start();
			
			// Start statistics gathering thread
			ServerStats.init();
			
			//Stats: uptime
			ServerStats.server_boottime = System.currentTimeMillis();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

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
import java.net.InetAddress;

import com.zavedil.apilator.app.Config;

public class Main {
	
	/**
	 * Main entry point for the Apliator
	 * @param args String[] Command-line arguments, if any
	 */
	public static void main(String[] args) {
		try {
			// Load external configuration
			new Config(); 
			
			// Automatic configuration
			new ConfigAuto();
			InetAddress ip = null;
			if (! Config.IpAddress.equals(""))
				ip = InetAddress.getByName(Config.IpAddress);
				
			// Start the queues
			Queue queueHttp = new Queue();
			Queue queueSm = new Queue();
			
			// Init the session storage (load form disk cache)
			SessionStorage sessionStorage = new SessionStorage();
			
			// Start Session Manager Clean-upper timer
			new SessionStorageCleanup(sessionStorage);
							
			// Start the Session Manager multicast server thread
			ServerMulticast sm_receive = new ServerMulticast(sessionStorage);
			Thread sm_receive_t = new Thread(sm_receive);
			sm_receive_t.start();
			
			// Start the Session Manager multicast client thread
			ClientMulticast cm = new ClientMulticast();
			Thread cm_t = new Thread(cm);
			cm_t.start();
	
			// Start the Session Manager TCP client thread
			ClientTcp ct = new ClientTcp(sessionStorage);
			Thread ct_t = new Thread(ct);
			ct_t.start();
			
			// Start the Session Manager Worker threads
			for (int i=0; i<Config.NumWorkersSm; i++)
				new Thread(new ServerTcpWorkerSm(queueSm, sessionStorage)).start();
			
			// Start the Session Manager TCP server thread
			new Thread(new ServerTcp(ServerTcp.SERVER_TYPE_SM , ip, queueSm)).start();
			
			// Start the HTTP Worker threads
			for (int i=0; i<Config.NumWorkersHttp; i++)
				new Thread(new ServerTcpWorkerHttp(queueHttp, sessionStorage)).start();
			
			// Start the HTTP server
			new Thread(new ServerTcp(ServerTcp.SERVER_TYPE_HTTP, ip, queueHttp)).start();
			
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

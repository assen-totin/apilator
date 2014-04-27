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
			// Automatic configuration
			new ConfigAuto();
			InetAddress ip = null;
			if (! Config.IpAddress.equals(""))
				ip = InetAddress.getByName(Config.IpAddress);
			
			// Init the session storage (load form disk cache)
			SessionStorage.init();
			
			// Start Session Manager Clean-upper thread
			SessionStorageCleanup.init();
							
			// Start the session storage manager thread for receiving
			ServerMulticast sm_receive = new ServerMulticast();
			Thread sm_receive_t = new Thread(sm_receive);
			sm_receive_t.start();
			
			// Start the session storage manager thread for sending
			ClientMulticast sm_send = new ClientMulticast();
			Thread sm_send_t = new Thread(sm_send);
			sm_send_t.start();
								
			// Start the Session Manager server
			ServerUdp su = new ServerUdp(ip);
			Thread su_t = new Thread(su);
			su_t.start();
			
			// Start the Session Manager Client
			ServerUdpClient suc = new ServerUdpClient(su);
			Thread suc_t = new Thread(suc);
			suc_t.start();
					
			// Start the HTTP server
			new Thread(new ServerTcp(ip)).start();
			
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

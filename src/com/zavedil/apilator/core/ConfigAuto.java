package com.zavedil.apilator.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Automatic configuration class.
 * Settings stored upon start-up. 
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

public class ConfigAuto {
	public static final InetAddress ip;
	//public static final short netmask;
	
	// Get a local IP address
	static {	
		InetAddress ip_tmp = null;
		try {
			ip_tmp = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			Logger.critical("ConfigAuto", "Unable to obtain local IP address. Exiting");
			System.exit(255);
		}
		ip = ip_tmp;
	}
	
	/*
	// Get a local subnet mask
	static {
		NetworkInterface iface_tmp = null;
		try {
			iface_tmp = NetworkInterface.getByInetAddress(ip);		
		}
		catch (SocketException e){
			Logger.critical("ConfigAuto", "Unable to obtain interface for IP address "+ ip.toString() + " . Exiting");
			System.exit(255);
		}
		netmask = iface_tmp.getInterfaceAddresses().get(0).getNetworkPrefixLength();	
	}
	*/
}

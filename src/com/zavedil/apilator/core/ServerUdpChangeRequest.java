package com.zavedil.apilator.core;

/**
 * SocketChannel status change helper class for the NIO TCP server.
 * @author James Greenfield nio@flat502.com
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original copyright (C) James Greenfield.
 * Modified by the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
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

import java.nio.channels.DatagramChannel;

public class ServerUdpChangeRequest {
	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	
	public DatagramChannel socket;
	public int type;
	public int ops;
	
	/**
	 * Set the requested ops to a SocketChannel
	 * @param socket SocketChannel The SocketChannel which to change status of
	 * @param type int The request type
	 * @param ops int The request ops
	 */
	public ServerUdpChangeRequest(DatagramChannel socket, int type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}
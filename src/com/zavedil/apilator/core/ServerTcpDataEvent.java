package com.zavedil.apilator.core;

/**
 * Data event helper class for the NIO TCP server.
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

import java.nio.channels.SocketChannel;

class ServerTcpDataEvent {
	public ServerTcp server;
	public SocketChannel socket;
	public byte[] data;
	public boolean close;
	
	/**
	 * Queue data to be sent back to the client
	 * @param server Server The server for this event
	 * @param socket SocketChannel The SocketChannel to use
	 * @param data byte[] The data to write to the channel
	 * @param boolean close Whether to close the channel after sending back the request
	 */
	public ServerTcpDataEvent(ServerTcp server, SocketChannel socket, byte[] data, boolean close) {
		this.server = server;
		this.socket = socket;
		this.data = data;
		this.close = close;
	}

	/**
	 * Alternative constructor.
	 */
	public ServerTcpDataEvent(ServerTcp server, SocketChannel socket, byte[] data) {
		this(server, socket, data, false);
	}

}
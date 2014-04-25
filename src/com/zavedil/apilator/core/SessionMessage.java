package com.zavedil.apilator.core;

import java.net.InetAddress;

/**
 * Session message class. 
 * Sent over multicast to inform peers about session storage update.
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

public class SessionMessage implements java.io.Serializable {
	private final String className;
	// Network actions
	public static final int ACT_NOOP = 0;	// Take no action
	public static final int ACT_AVAIL = 1;	// Used when multicasting an available update
	public static final int ACT_DELETE = 2;	// Used when multicasting a deletion
	public static final int ACT_WHOHAS = 3;	// Used when multicasting a question asking who has a specified key
	public static final int ACT_ISAT = 4;	// Used when unicasting key availability
	public static final int ACT_GET = 5;	// Used when unicasting session retrieval request
	public static final int ACT_POST = 6;	// Used when unicasting a sending a session
	
	private static final long serialVersionUID = 1L;
	public final int type;
	public final String session_id;
	public final InetAddress ip;
	public long updated=0;
	public Session session=null;
	
	public SessionMessage(String session_id, int type) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		this.session_id = session_id;
		this.type = type;	
		this.ip = ConfigAuto.ip;
	}
}

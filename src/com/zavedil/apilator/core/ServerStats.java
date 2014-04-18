package com.zavedil.apilator.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Gather and store server statistics. 
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

public class ServerStats {
	// Server absolute uptime
	public static long server_boottime = 0;
	
	// Create initial storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> threads_uptime = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);
	
	// Create initial storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> requests = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);	
	
	// Create initial storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> exec_time = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);
}

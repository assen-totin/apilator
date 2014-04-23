package com.zavedil.apilator.core;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scheduler to gather and store server statistics. 
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

public class ServerStatsScheduler implements Runnable {
	private String className;
	
	// Server absolute uptime
	public static long server_boottime = 0;
		
	// HTTP requests for the last minute - storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> http_requests = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);	
	
	// HTTP exec time for the last minute - storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> http_exec = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);
	
	// HTTP requests aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> http_requests_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);

	// HTTP exec time aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> http_exec_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);

	// HTTP threads aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> http_threads_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);	
	
	// Session Manager requests for the last minute - storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> sm_requests = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);	
	
	// Session Manager exec time for the last minute - storage for 1000 threads, expand when 90% full and use only 1 shard
	public static ConcurrentHashMap<Long, Long> sm_exec = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);
	
	// Session Manager requests aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> sm_requests_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);

	// Session Manager exec time aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> sm_exec_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);

	// Session Manager threads aggregated per minute for the last 15 minutes
	public static ConcurrentHashMap<Long, Long> sm_threads_aggr = new ConcurrentHashMap<Long, Long>(1000, 0.9f, 1);	

	
	/**
	 * Runnable. 
	 * Create initial storage. 
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
			
		Timer time = new Timer();
		ServerStatsTask sst = new ServerStatsTask();
		time.schedule(sst, 0, 60000);
    }
}

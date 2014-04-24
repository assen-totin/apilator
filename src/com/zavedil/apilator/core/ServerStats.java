package com.zavedil.apilator.core;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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

public class ServerStats {
	public static final String className = "ServerStats";
	
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
	//public void run() {
	public static void init() {
		Logger.trace(className, "Initializing.");
			
		Timer time = new Timer();
		time.schedule(
			new TimerTask(){
				public void run() {
					long http_requests = 0;
					long http_threads = 0;
					long http_exec = 0;
					
					long sm_requests = 0;
					long sm_threads = 0;
					long sm_exec = 0;
					
					// HTTP number of requests for the last minute
					for (Map.Entry<Long, Long> pair : ServerStats.http_requests.entrySet()) {
						http_requests += pair.getValue();
						http_threads++;
				    }
					
					// HTTP exec time for last minute
					for (Map.Entry<Long, Long> pair : ServerStats.http_exec.entrySet())
						http_exec += pair.getValue();
					
					// Session Manager number of requests for the last minute
					for (Map.Entry<Long, Long> pair : ServerStats.sm_requests.entrySet()) {
						sm_requests += pair.getValue();
						sm_threads++;
				    }
					
					// Session Manager exec time for last minute
					for (Map.Entry<Long, Long> pair : ServerStats.sm_exec.entrySet())
						sm_exec += pair.getValue();	
					
					long now = System.currentTimeMillis();
					
					// Save results - HTTP
					ServerStats.http_requests_aggr.put(now, http_requests);
					ServerStats.http_exec_aggr.put(now, http_exec);
					ServerStats.http_threads_aggr.put(now, http_threads);
					// Save results - SM
					ServerStats.sm_requests_aggr.put(now, sm_requests);
					ServerStats.sm_exec_aggr.put(now, sm_exec);
					ServerStats.sm_threads_aggr.put(now, sm_threads);
					
					// Cleanup - HTTP
					ServerStats.http_requests.clear();
					ServerStats.http_exec.clear();
					// Cleanup - SM
					ServerStats.sm_requests.clear();
					ServerStats.sm_exec.clear();
					// Cleanup - aggregated - remove everything that is older than 15 minutes
					long offset = (15 + 1) * 60 * 1000; 

					for (Map.Entry<Long, Long> pair : ServerStats.http_requests_aggr.entrySet())
						if ((now - pair.getKey()) > offset)
							ServerStats.http_requests_aggr.remove(pair.getKey());
					for (Map.Entry<Long, Long> pair : ServerStats.http_exec_aggr.entrySet())
						if ((now - pair.getKey()) > offset)
							ServerStats.http_exec_aggr.remove(pair.getKey());
					for (Map.Entry<Long, Long> pair : ServerStats.sm_requests_aggr.entrySet())
						if ((now - pair.getKey()) > offset)
							ServerStats.sm_requests_aggr.remove(pair.getKey());
					for (Map.Entry<Long, Long> pair : ServerStats.sm_exec_aggr.entrySet())
						if ((now - pair.getKey()) > offset)
							ServerStats.sm_exec_aggr.remove(pair.getKey());
				}
			}, 
			0, 60000);
    }
}

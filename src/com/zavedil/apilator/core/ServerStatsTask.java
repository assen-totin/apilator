package com.zavedil.apilator.core;

/**
 * Scheduled task to gather and store server statistics.
 * Runs every minute.  
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

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStatsTask extends TimerTask {
	private final String className;
	
	public ServerStatsTask() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	public void run() {
		long http_requests = 0;
		long http_threads = 0;
		long http_exec = 0;
		
		long sm_requests = 0;
		long sm_threads = 0;
		long sm_exec = 0;
		
		// HTTP number of requests for the last minute
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.http_requests.entrySet()) {
			http_requests += pair.getValue();
			http_threads++;
	    }
		
		// HTTP exec time for last minute
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.http_exec.entrySet())
			http_exec += pair.getValue();
		
		// Session Manager number of requests for the last minute
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.sm_requests.entrySet()) {
			sm_requests += pair.getValue();
			sm_threads++;
	    }
		
		// Session Manager exec time for last minute
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.sm_exec.entrySet())
			sm_exec += pair.getValue();	
		
		long now = System.currentTimeMillis();
		
		// Save results - HTTP
		ServerStatsScheduler.http_requests_aggr.put(now, http_requests);
		ServerStatsScheduler.http_exec_aggr.put(now, http_exec);
		ServerStatsScheduler.http_threads_aggr.put(now, http_threads);
		// Save results - SM
		ServerStatsScheduler.sm_requests_aggr.put(now, sm_requests);
		ServerStatsScheduler.sm_exec_aggr.put(now, sm_exec);
		ServerStatsScheduler.sm_threads_aggr.put(now, sm_threads);
		
		// Cleanup - HTTP
		ServerStatsScheduler.http_requests.clear();
		ServerStatsScheduler.http_exec.clear();
		// Cleanup - SM
		ServerStatsScheduler.sm_requests.clear();
		ServerStatsScheduler.sm_exec.clear();
		// Cleanup - aggregated - remove everything that is older than 15 minutes
		long offset = (15 + 1) * 60 * 1000; 

		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.http_requests_aggr.entrySet())
			if ((now - pair.getKey()) > offset)
				ServerStatsScheduler.http_requests_aggr.remove(pair.getKey());
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.http_exec_aggr.entrySet())
			if ((now - pair.getKey()) > offset)
				ServerStatsScheduler.http_exec_aggr.remove(pair.getKey());
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.sm_requests_aggr.entrySet())
			if ((now - pair.getKey()) > offset)
				ServerStatsScheduler.sm_requests_aggr.remove(pair.getKey());
		for (Map.Entry<Long, Long> pair : ServerStatsScheduler.sm_exec_aggr.entrySet())
			if ((now - pair.getKey()) > offset)
				ServerStatsScheduler.sm_exec_aggr.remove(pair.getKey());
	}
}

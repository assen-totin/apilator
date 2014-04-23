package com.zavedil.apilator.app;

/**
 * Dedicated API Endpoint to serve statistics. 
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

import com.eclipsesource.json.JsonObject;
import com.zavedil.apilator.core.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stats extends Endpoint {
	private final String className;
	private long now = System.currentTimeMillis();
	
	/**
	 * Constructor method
	 */
	public Stats(TaskInput api_task) {
		super(api_task);
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating a new instance.");
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	@Override
	public void get() {
		Logger.debug(className, "Entering function get.");
		super.get();

		// Gather statistics: server_uptime, total_threads, total_requests, avg_exec_time
		long server_uptime=0, total_threads=0, total_requests=0, avg_exec=0, total_exec=0, avg_busy=0;
		
		// What time offset should we report for? Default is 5 minutes.
		int offset = 5;
		if (input.data.containsKey("offset")) 
			offset = Integer.parseInt(input.data.get("offset").toString());
		
		// Should we report for HTTP or for Session Manager?
		if (input.data.containsKey("source") && input.data.get("source").toString().equals("sm")) {
			// Report for Session Manager 
			total_requests = sum(ServerStatsScheduler.sm_requests_aggr, offset);
			total_threads = sum(ServerStatsScheduler.sm_threads_aggr, offset) / offset;
			total_exec = sum(ServerStatsScheduler.sm_exec_aggr, offset);
			avg_exec = total_exec / total_requests;
			avg_busy = 100 * total_exec / (total_threads * offset * 60 * 1000);	// Percentage average per thread
		}
		else {
			// Report for HTTP
			total_requests = sum(ServerStatsScheduler.http_requests_aggr, offset);
			total_threads = sum(ServerStatsScheduler.http_threads_aggr, offset) / offset;
			total_exec = sum(ServerStatsScheduler.http_exec_aggr, offset);
			avg_exec = total_exec / total_requests;
			avg_busy = 100 * total_exec / (total_threads * offset * 60 * 1000);	// Percentage average per thread
		}
		
		server_uptime = System.currentTimeMillis() - ServerStatsScheduler.server_boottime;		
		
		// Should we send in plain text or in JSON?
		String resp;
		if (input.data.containsKey("format") && input.data.get("format").toString().equals("json")) {
			// Send JSON
			JsonObject jsonObject = new JsonObject().add( "server_uptime", server_uptime );
			jsonObject.add("total_requests", total_requests);
			jsonObject.add("avg_threads", total_threads);
			jsonObject.add("avg_exec_time", avg_exec);
			jsonObject.add("avg_busy_percent", avg_busy);
			resp = jsonObject.toString();
		}
		else {
			// Send plain text
			resp = "server_uptime:" + server_uptime + "\n";
			resp += "total_requests:" + total_requests + "\n";
			resp += "avg_threads:" + total_threads + "\n";
			resp += "avg_exec_time:" + avg_exec + "\n";
			resp += "avg_busy_percent:" + avg_busy + "\n";
		}
		output.data = resp.getBytes();
	}
	
	private int sum(ConcurrentHashMap<Long,Long> map, int offset) {
		int res = 0;
		long offset_ms = offset * 60 * 1000;
		
		for(Map.Entry<Long,Long> entry : map.entrySet())
			if ((now - entry.getKey()) < offset_ms)
				res += entry.getValue();
		
		return res;
	}
}

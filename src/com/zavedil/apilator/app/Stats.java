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

public class Stats extends Endpoint {
	private final String className;
	
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
		long server_uptime=0, total_threads=0, total_requests=0, total_exec_time=0, avg_exec_time;
		
		/*
		for(Map.Entry<Long,Long> entry : ServerStats.threads_uptime.entrySet()) {
			if (entry.getValue() > server_uptime)
				server_uptime = entry.getValue(); 
		}
		*/
		server_uptime = System.currentTimeMillis() - ServerStats.server_boottime;
		
		for(Map.Entry<Long,Long> entry : ServerStats.requests.entrySet()) {
			total_requests += entry.getValue();
			total_threads++;
		}

		for(Map.Entry<Long,Long> entry : ServerStats.exec_time.entrySet())
			total_exec_time += entry.getValue();
		avg_exec_time = total_exec_time / total_requests;

		
		// Should we send in plain text or in JSON?
		String resp;
		if (input.data.containsKey("format") && input.data.get("format").toString().equals("json")) {
			// Send JSON
			JsonObject jsonObject = new JsonObject().add( "server_uptime", server_uptime );
			jsonObject.add("total_threads", total_threads);
			jsonObject.add("total_requests", total_requests);
			jsonObject.add("avg_exec_time", avg_exec_time);
			resp = jsonObject.toString();
		}
		else {
			// Send plain text
			resp = "server_uptime:" + server_uptime + "\n";
			resp += "total_threads:" + total_threads + "\n";
			resp += "total_requests:" + total_requests + "\n";
			resp += "avg_exec_time:" + avg_exec_time + "\n";
		}
		output.data = resp.getBytes();
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		Logger.debug(className, "Entering function get.");
		super.post();
		// No POST requests here		
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	@Override
	public void put() {
		Logger.debug(className, "Entering function get.");
		super.put();
		// No PUT requests here  
	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	@Override
	public void delete() {
		Logger.debug(className, "Entering function get.");
		super.delete();
		// No DELETE requests here  
	}
}

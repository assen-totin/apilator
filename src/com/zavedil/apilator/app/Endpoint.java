package com.zavedil.apilator.app;

/**
 * API Endpoint base class. 
 * Extends the Api class from core package.
 * Implements common processing for all enpoints.
 * Use as base class for actual endpoints. 
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

import java.util.Date;
import com.eclipsesource.json.JsonObject;
import com.zavedil.apilator.core.*;

public abstract class Endpoint {
	// Input data
	protected final TaskInput input;

	// Session Storage
	private final SessionStorage sessionStorage;
	
	// Session
	protected Session session;
	
	// Output data
	protected TaskOutput output;
	
	/**
	 * Constructor method
	 */
	public Endpoint(TaskInput api_task, SessionStorage ss) {
		input = api_task;
		sessionStorage = ss;
		output = new TaskOutput();
		output.mime_type = "application/json";
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	public void get() {
		onLoad();
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	public void post() {
		onLoad();	
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	public void put() {
		onLoad();		
	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	public void delete() {
		onLoad();
	}
	
	/**
	 * Method run for each new request.
	 * Put here what you want done each time. e.g. session cookie processing. 
	 */
	private void onLoad() {
		String session_id;
		long created, valid_to, now = System.currentTimeMillis();
		
		// If session cookies are disabled, we have nothing to do here
		if (Config.SessionCookie.equals(""))
			return;
		
		// Check for session cookie
		if (input.cookies != null) {
			if (input.cookies.containsKey(Config.SessionCookie)) {
				// You may need to further decode the cookie value if it is, say, JSON. We assume it is plain.
				session_id = input.cookies.get(Config.SessionCookie);
				
				// Get the session; null will be returned on session not found
				session = sessionStorage.get(session_id);
				
				// Create session if missing
				if (session == null) {
					session = new Session();
					populateNewSession();
				}
			}
		}
		else {
			// No cookie, so create a new session.
			session = new Session();
			populateNewSession();
		}
		
		// Prepare the session cookie
		output.cookies_data.put(Config.SessionCookie, session.getSessionId());

		// Add optional expiration date for a cookie		
		if (Config.SessionCookieExpire > 0) {
			// Extend the cookie life with the specified TTL
			valid_to = now + Config.SessionCookieExpire;
			output.cookies_expire.put(Config.SessionCookie, valid_to);
		}
		
		else if (Config.SessionCookieExpire < 0) {
			// Set the cookie life to the specified TTL
			created = session.getCreated();
			valid_to = created + (-1 * Config.SessionCookieExpire);
			output.cookies_expire.put(Config.SessionCookie, valid_to);
		}
	}
	
	/**
	 * Method to be called when processing of the request completes. 
	 */
	public TaskOutput onCompletion() {
		// If session cookies are disabled, we have nothing to do here
		if (!Config.SessionCookie.equals(""))
			// Send the session to the SessionStorage
			sessionStorage.put(session.getSessionId(), session);
		
		// Additional headers that we always want to have:
		output.headers.put("Access-Control-Allow-Origin", "*");
		
		return output;
	}
	
	/**
	 * Placeholder method to conveniently populate a new session with data.
	 * Fill-in with your desired code.
	 */
	private void populateNewSession() {
		// Add your code here.
		// Example: put a key-value pair to session object:
		//session.put("key", "value");
	}
	
	protected String stripEndpointFromLocation() {
		String output = "";
		String[] parts = input.location.split("/");
		
		// Because we have a leading slash, the first element is empty and the second is the endpoint - skip both of them
		for (int i=2; i<parts.length; i++)
			output += "/" + parts[i];
		
		return output;
	}
}

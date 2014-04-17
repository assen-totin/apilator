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

import com.eclipsesource.json.JsonObject;
import com.zavedil.apilator.core.*;

public class Endpoint extends Api {
	
	/**
	 * Constructor method
	 */
	public Endpoint(TaskInput api_task) {
		super(api_task);
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	@Override
	public void get() {
		super.get();
		onLoad();
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		super.post();
		onLoad();	
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	@Override
	public void put() {
		super.put();
		onLoad();		
	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	@Override
	public void delete() {
		super.delete();
		onLoad();
	}
	
	/**
	 * Method run for each new request.
	 * Put here what you want done each time. e.g. session cookie processing. 
	 */
	private void onLoad() {
		String session_id;
		
		// Check for session cookie
		if (input.cookies != null) {
			if (input.cookies.containsKey(Config.SessionCookie)) {
				// You may need to further decode the cookie value if it is, say, JSON. We assume it is plain.
				session_id = input.cookies.get(Config.SessionCookie);
				
				// Get the session; null will be returned on session not found
				session = SessionStorage.get(session_id);
				
				// Create session if missing
				if (session == null) 
					session = new Session();
			}
		}
	}
}

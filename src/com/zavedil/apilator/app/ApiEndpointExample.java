package com.zavedil.apilator.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.eclipsesource.json.JsonObject;
import com.zavedil.apilator.core.*;

/**
 * Example API Endpoint class. 
 * Use as skeleton for actual development. 
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

/*
// Example code to get a new session ID:
String session_id = Session.getNewSessionId();
*/

/*
//Example code to save an uploaded file:
//if the form field with the file was named "myfile", then the filename will be available in "myfile_fn"
if (task.http_input.containsKey("myfile") && task.http_input.containsKey("myfile_fn")) {			
	byte[] myfile = (byte []) http_input.get("myfile");
	String myfile_fn = http_input.get("myfile_fn").toString();
	String dir = "/tmp";
	try {
		FileOutputStream fout = new FileOutputStream(dir + "/" + myfile_fn);
		fout.write(myfile);
		fout.close();
	} 
	catch (IOException e) {
		output_http_status = 500;
	}
}
else
	output_http_status = 404;
*/


public class ApiEndpointExample extends Api {
	private final String className;
	
	/**
	 * Constructor method
	 */
	public ApiEndpointExample(ApiTask api_task) {
		super(api_task);
		className = this.getClass().getSimpleName();
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	@Override
	public void get() {
		Logger.debug(className, "Entering function get.");
		super.get();
		
		// Add your code below
		String session_id = Session.getNewSessionId();
		SessionItem session_item = new SessionItem();
		session_item.put("some_key", "some_value");
		SessionStorage.put(session_id, session_item);
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		Logger.debug(className, "Entering function get.");
		super.post();
		
		// Add your code below
		
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	@Override
	public void put() {
		Logger.debug(className, "Entering function get.");
		super.put();
		
		// Add your code below

	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	@Override
	public void delete() {
		Logger.debug(className, "Entering function get.");
		super.delete();
		
		// Add your code below

	}
}

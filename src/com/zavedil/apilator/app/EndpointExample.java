package com.zavedil.apilator.app;

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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.zavedil.apilator.core.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

/*
// Example code to work with session objects
////////////////////////////////////////////

// Get a new session object 
// Note that with Automated Session Management (ASM) sessions are automatically created;
// if you do not use ASM, you can manually create a session (it will be automatically stored):  
Session session = new Session();

// Get a session object from storage by its ID (NULL is returned on missing ID)
// Note that with ASM session is automatically loaded for you from the session cookie; 
// if you do not use ASM, you can manually load a session:
Session session = SessionStorage.get(session_id);

// Get the session_id from the session object:
String session_id = session.getSessoinId();

// Check if the session is a blank one (helpful when using ASM)
if (session.getUpdated() == session.getCreated()) {
	// This is a blank session, just created
}

// Put a key-value pair to session object:
session.put("key", "value");

// Get a value from a session object by a given key:
// You need to cast the returned object to its proper type!
if ((session != null) && session.containsKey("key")) {
	Object value = session.get("key");
}

// Delete a key-value pair from session object:
session.del("key");


// Example code to work with cookies
////////////////////////////////////////////

// Get cookies as a Hashtable
Hashtable cookies = input.cookies;

// Get a cookie value by name
if (input.cookies != null) {
	if (input.cookies.containsKey("cookie_name")) {
		String value = input.cookies.get("cookie_name");
		// You may need to further decode the cookie value if it is, say, JSON.
	}
}

// Add cookie to output table (the output table is empty by default): name and value should be String
// You may need to encode the cookie value beforehand, e.g. to JSON
output.cookies_data.put("cookie_name", "cookie_value");

// Add optional expiration date for a cookie: 
// name should existing cookie name and value should be expiration timestamp in milliseconds (long)
Date date = new Date();
long now = date.getTime();
now += 30 * 24 * 3600 * 1000L;
output.cookies_expire.put("cookie_name", now);

// Copy all input cookies as output cookies
output.cookies_data = input.cookies;


// Example code to deal with JSON:
////////////////////////////////////////////

// Read the value for key "key" from incoming JSON string:
JsonObject jsonObject = JsonObject.readFrom( string );
String value = jsonObject.get( "key" ).asString();

// Same but for index 0 of an array:
JsonArray jsonArray = JsonArray.readFrom( string );
String value = jsonArray.get( 0 ).asString();

// Getting a nested array from key "key":
JsonArray nestedArray = jsonObject.get( "key" ).asArray();

// Iterating over JSON array or object:
for( String key : jsonObject.names() ) {
  JsonValue value = jsonObject.get( key );
  ...
}

// Create JSON array or object:
JsonObject jsonObject = new JsonObject().add( "key", "value" ).add( "key_int", 42 );
JsonObject jsonArray = new JsonArray().add( "value" ).add( 42 );

// Write JSON from array to string:
String json = jsonArray.toString();
String json = jsonObject.toString();


// Example code to save an uploaded file
////////////////////////////////////////////

// If the form field with the file was named "myfile", then the filename will be available in "myfile_fn"
if (input.data.containsKey("myfile") && input.data.containsKey("myfile_fn")) {			
	byte[] myfile = (byte []) input.data.get("myfile");
	String myfile_fn = input.data.get("myfile_fn").toString();
	String dir = "/tmp";
	try {
		FileOutputStream fout = new FileOutputStream(dir + "/" + myfile_fn);
		fout.write(myfile);
		fout.close();
	} 
	catch (IOException e) {
		output.http_status = 500;
	}
}
else
	output.http_status = 404;
	

// Example code to send back some data
////////////////////////////////////////////

output.data = "Keep walking, dude!".getBytes();


*/


public class EndpointExample extends Endpoint {
	private final String className;
	
	/**
	 * Constructor method
	 * @param api_task TaskInput The input data from the HTTP request
	 */
	public EndpointExample(TaskInput api_task) {
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
		
		// Add your code here		
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		Logger.debug(className, "Entering function get.");
		super.post();

		// Return if the CSRF check has failed
		if (!csrf)
			return;
		
		// Add your code here
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	@Override
	public void put() {
		Logger.debug(className, "Entering function get.");
		super.put();
		
		// Return if the CSRF check has failed
		if (!csrf)
			return;

		
		// Add your code here
	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	@Override
	public void delete() {
		Logger.debug(className, "Entering function get.");
		super.delete();
		
		// Return if the CSRF check has failed
		if (!csrf)
			return;

		
		// Add your code here
	}
	
	/**
	 * Method invoked whenever an OPTIONS request is received.
	 */
	@Override
	public void options() {
		Logger.debug(className, "Entering function options.");
		super.options();
		
		// Add your code here
	}
}

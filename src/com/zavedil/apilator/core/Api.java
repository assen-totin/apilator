package com.zavedil.apilator.core;

/**
 * API base class. 
 * Extend it from the class which corresponds to an API entry point. 
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class Api {
	// Input data
	protected final ApiTask task;							// Input data 
	
	// Output data
	protected byte[] output_data=null;						// Output data
	protected Hashtable<String,String> output_cookies_data = new Hashtable<String,String>();	// Output cookies name/values (optional)
	protected Hashtable<String, Long> output_cookies_expire = new Hashtable<String,Long>();		// Output cookies name/expiration (optional)
	protected int output_http_status=200; 					// Output HTTP status from processing the request (optional)
	protected String output_mime_type="application/json";	// Output MIME type (optional)
	
	/**
	 * Constructor method
	 */
	protected Api(ApiTask api_task) {
		task = api_task;
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	public void get() {
		
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	public void post() {
		
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	public void put() {
		
	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	public void delete() {
		
	}
	
	/**
	 * Getter for 'output_data' property
	 */
	public byte[] getOutputData() {
		return output_data;
	}

	/**
	 * Getter for 'output_cookies' property
	 */
	public String getOutputCookies() {
		String cookies=null;
		Map.Entry pair=null;
		SimpleDateFormat format;
		Date cookie_date;

		format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		
	    Iterator iterator = output_cookies_data.entrySet().iterator();
	    while (iterator.hasNext()) {
	    	pair = (Map.Entry)iterator.next();
	    	iterator.remove();
	    	cookies += "Set-Cookie: " + pair.getKey().toString() + "=" +pair.getValue().toString();
	    	if (output_cookies_expire.containsKey(pair.getKey())) {
	    		cookie_date = new Date();
	    		cookie_date.setTime((long) output_cookies_expire.get(pair.getKey()));
	    		cookies += "; Expires=" + format.format(cookie_date) + " GMT\n";
	    	}
	    	cookies += "\n";
	    }
		return cookies;
	}
	
	/**
	 * Getter for 'output_http_status' property
	 */
	public int getOutputHttpStatus() {
		return output_http_status;
	}

	/**
	 * Getter for 'output_mime_type' property
	 */
	public String getOutputMimeType() {
		return output_mime_type;
	}
}

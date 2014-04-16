package com.zavedil.apilator.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * API task definition class. 
 * Each request is passed to the API as an instance of this class. 
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

public class TaskOutput {
	private final String className;
	public byte[] data=null;						// Output data
	public int http_status=200; 					// Default HTTP status from processing
	public String mime_type="text/plain";		// Default output MIME type
	public Hashtable<String,String> cookies_data = new Hashtable<String,String>();	// Output cookies name/values (optional)
	public Hashtable<String, Long> cookies_expire = new Hashtable<String,Long>();	// Output cookies name/expiration (optional)
	public String cookies="";						// Output HTTP headers for cookies (optional)
	
	public TaskOutput() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Cookie builder
	 */
	public void buildCookies() {
		SimpleDateFormat format;
		Date cookie_date;

		format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));

		for (Map.Entry<String,String> pair : cookies_data.entrySet()) {
	    	cookies += "Set-Cookie: " + pair.getKey().toString() + "=" +pair.getValue().toString();
	    	if (cookies_expire.containsKey(pair.getKey())) {
	    		cookie_date = new Date();
	    		cookie_date.setTime((long) cookies_expire.get(pair.getKey()));
	    		cookies += "; Expires=" + format.format(cookie_date) + " GMT\n";
	    	}
	    	cookies += "\n";
		}
		
	}
}

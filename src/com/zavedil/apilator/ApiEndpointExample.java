package com.zavedil.apilator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import com.eclipsesource.json.JsonObject;

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

public class ApiEndpointExample extends Api {
	private final String className;
	
	/**
	 * Constructor method
	 */
	public ApiEndpointExample(Hashtable params) {
		super(params);
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
		
	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		Logger.debug(className, "Entering function get.");
		super.post();
		
		// Add your code below
		
		if (input_params.containsKey("myfile") && input_params.containsKey("myfile_fn")) {			
			byte[] myfile = (byte []) input_params.get("myfile");
			String myfile_fn = input_params.get("myfile_fn").toString();
			try {
				FileOutputStream fout = new FileOutputStream("/tmp/" + myfile_fn);
				fout.write(myfile);
				fout.close();
				
			} 
			catch (IOException e) {
				output_http_status = 500;
			}
		}
		else
			output_http_status = 404;
		
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

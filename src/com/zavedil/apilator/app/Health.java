package com.zavedil.apilator.app;

/**
 * A class to be used as health-check endpoint (e.g., by load-balancers).
 * Will return 200 OK on every request. 
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

import com.zavedil.apilator.core.*;

public class Health extends Endpoint {
	private String className;
	
	/**
	 * Constructor method
	 * @param api_task TaskInput The input data from the HTTP request
	 */
	public Health(TaskInput api_task) {
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
	}
}

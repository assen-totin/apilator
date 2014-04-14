package com.zavedil.apilator;

import java.util.Hashtable;

/**
 * Example API class. 
 * Use as skeleton for actual development. 
 * 
 * @author Assen Totin assen.totin@gmail.com
 */

public class ApiEndpointExample extends Api {
	/**
	 * Constructor method
	 */
	public ApiEndpointExample(Hashtable params) {
		super(params);
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	@Override
	public void get() {
		super.get();
		// Add your code below

	}
	
	/**
	 * Method invoked whenever a POST request is received.
	 */
	@Override
	public void post() {
		super.post();
		// Add your code below
		
	}
	
	/**
	 * Method invoked whenever a PUT request is received.
	 */
	@Override
	public void put() {
		super.put();
		// Add your code below

	}
	
	/**
	 * Method invoked whenever a DELETE request is received.
	 */
	@Override
	public void delete() {
		super.delete();
		// Add your code below

	}
}

package com.zavedil.apilator;

import java.util.Hashtable;

/**
 * API base class. 
 * Extend it from the class which corresponds to an API entry point. 
 * 
 * @author Assen Totin assen.totin@gmail.com
 *
 */

public abstract class Api {
	// Input data
	protected final Hashtable input_params;			// Input data (key/values from HTTP request)
	
	// Output data
	protected byte[] output=null;					// Output data
	protected int output_len=0;						// Output data size in bytes (optional)
	protected int output_http_status=200; 			// Output HTTP status from processing the request (optional)
	protected String output_mime_type="text/plain";	// Output MIME type (optional)
	
	/**
	 * Constructor method
	 */
	protected Api(Hashtable params) {
		input_params = params;
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
	 * Getter for 'output' property
	 */
	public byte[] getOutput() {
		return output;
	}
	
	/**
	 * Getter for 'output_len' property
	 */
	public int getOutputLen() {
		return output_len;
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

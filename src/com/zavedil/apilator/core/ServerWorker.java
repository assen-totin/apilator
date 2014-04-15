package com.zavedil.apilator.core;

/**
 * Worker class for the NIO TCP server.
 * @author James Greenfield nio@flat502.com
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original copyright (C) James Greenfield.
 * Modified by the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class ServerWorker implements Runnable {
	private List queue = new LinkedList();
	private final String className;
	private Thread session_storage;
	
	/**
	 * Constructor. 
	 * @param sst Thread Handler to the thread that manages the session storage
	 */
	public ServerWorker(Thread sst) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		
		// Store the handler to the session storage thread
		session_storage = sst;
	}
	
	/**
	 * Main function to be called when packet(s) arrive over a SocketChannel
	 * @param server Server The server which originated the packets 
	 * @param socketChannel SocketChannel The SocketChannel (NIO socket) which originated the packets
	 * @param data byte[] The data from the packets
	 * @param count int The number of bytes received
	 * @throws IOException
	 */
	public void processData(Server server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
		HttpParser http_parser=null;
		String headers=null, output_mime_type="text/plain";
		int headers_len=0, output_http_status=0, output_data_len=0;
		byte[] output_data=null, response;
		
		Logger.debug(className, "Entering function processData.");
		boolean headers_ok = false;
		
		try {
			http_parser = new HttpParser(data, count);
			output_http_status = http_parser.parseRequest();
			
			if (output_http_status == 0)
				return;
			else if (output_http_status == 200)
				headers_ok = true;
		}
		catch (IOException e) {
			output_http_status = 500;
			headers_ok = false;
		}
		
		if (headers_ok) {
			String location = http_parser.getLocation();
			
			if (serveStatic(location)) {
				// Call the static content class
				StaticContent static_content = new StaticContent(location);
				output_http_status = static_content.getOutputHttpStatus();
				if (output_http_status == 200) {
					output_data = static_content.getOutputData();
					output_mime_type = static_content.getOutputMimeType();
				}
			}
			
			else {
				/**
				 * We call the API here; we/it should set:
				 * - output_http_status (if not 200)
				 * - output_data
				 * - output_mime_type (if different from default text/plain)
				 */				
				
				// Construct new task
				ApiTask api_task = new ApiTask();
				api_task.http_input = http_parser.getParams();;
				
				// API call using reflection
				String endpoint = getEndpoint(location);
				try {
					Class api_class = Class.forName(getPackageName() + "." + endpoint);
					Constructor api_constr = api_class.getConstructor(Hashtable.class);
					Object api_obj = api_constr.newInstance(api_task);
					
					String method = http_parser.getMethod();
					Method api_method = api_obj.getClass().getMethod(method.toLowerCase());
					api_method.invoke(api_obj);
					
					Method api_method_get_http_status = api_obj.getClass().getMethod("getOutputHttpStatus");
					output_http_status = (int) api_method_get_http_status.invoke(api_obj);
					
					if (output_http_status == 200) {
						Method api_method_get_output = api_obj.getClass().getMethod("getOutputData");
						output_data = (byte[]) api_method_get_output.invoke(api_obj);
						
						Method api_method_get_mime_type = api_obj.getClass().getMethod("getOutputMimeType");
						output_mime_type = (String) api_method_get_mime_type.invoke(api_obj);						
					}
				}
				catch (Exception e) {
					output_http_status = 404;
				}
			} // End API call here
		}
		
		// Prepare body
		if (output_data == null)
			output_data = http_parser.getHttpMessageForCode(output_http_status).getBytes();
		output_data_len = output_data.length;		
		
		// Log the request
		// Note: we don't handle authentication, hence user is always "-"
		// Who's there?
		Logger.log_access(socketChannel.socket().getInetAddress().getHostAddress(), "-", http_parser.getFirstLine(), output_http_status, output_data_len);
		
		// Prepare headers
		headers = http_parser.getHttpReplyHeaders(output_http_status, output_mime_type);
		headers += "Content-Length: " + output_data_len + "\n";
		headers += "\n";
		headers_len = headers.length();
			
		// Combine headers and body
		response = new byte[headers_len + output_data_len];
    	System.arraycopy(headers.getBytes(), 0, response, 0, headers_len);
    	System.arraycopy(output_data, 0, response, headers_len, output_data_len);
	
    	// Push response back
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socketChannel, response));
			queue.notify();
		}
	}
	
	/**
	 * The main loop of the worker thread
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		ServerDataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} 
					catch (InterruptedException e) {
					}
				}
				dataEvent = (ServerDataEvent) queue.remove(0);
			}
			
			// Return to sender
			dataEvent.server.send(dataEvent.socket, dataEvent.data);
		}
	}
	
	/**
	 * Helper method to check whether the worker should serve a static content or call the API
	 * @param location String The local part of the original URL
	 * @return boolean TRUE if the worker should serve static content, FALSE if the worker should call the API
	 */
	private boolean serveStatic(String location) {
		int idx = location.indexOf("/api");
		if (idx == 0)
			return false;
		return true;
	}
	
	/**
	 * Helper method to derive the name of the API endpoint from the original URL
	 * @param location String The local part of the original URL
	 * @return String The name of the API endpoint
	 */
	private String getEndpoint(String location) {
		String[] parts = location.split("/");
		return parts[2];
	}
	
	private String getPackageName() {
		String[] parts = this.getClass().getPackage().toString().split(" ");
		return parts[1];
	}
	
}
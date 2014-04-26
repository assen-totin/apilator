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
import java.util.LinkedList;
import java.util.List;
import com.zavedil.apilator.app.*;

public class CopyOfServerTcpWorker implements Runnable {
	private List<ServerTcpDataEvent> queue = new LinkedList<ServerTcpDataEvent>();
	private final String className;
	private final long created = System.currentTimeMillis();
	private boolean busy = false;
	private long exec_time = 0;
	private long requests = 1;
	
	/**
	 * Constructor. 
	 * @param sst Thread Handler to the thread that manages the session storage
	 */
	public CopyOfServerTcpWorker() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	/**
	 * Main function to be called when packet(s) arrive over a SocketChannel
	 * @param server Server The server which originated the packets 
	 * @param socketChannel SocketChannel The SocketChannel (NIO socket) which originated the packets
	 * @param data byte[] The data from the packets
	 * @param count int The number of bytes received
	 * @throws IOException
	 */
	public void processData(ServerTcp server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
		busy = true;
		long run_start_time = System.currentTimeMillis();
		
		Logger.debug(className, "Entering function processData.");
		
		HttpParser http_parser=null;
		String headers=null;
		int headers_len=0;
		byte[] response;
		TaskOutput output = new TaskOutput();
		TaskInput input = new TaskInput();		
		
		boolean headers_ok = false;
		
		try {
			http_parser = new HttpParser(data, count);
			output.http_status = http_parser.parseRequest();
			
			if (output.http_status == 0)
				return;
			else if (output.http_status == 200)
				headers_ok = true;
		}
		catch (IOException e) {
			output.http_status = 500;
			headers_ok = false;
		}
		
		if (headers_ok) {
			/**
			 * We call the API here; we/it should set:
			 * - output_http_status (if not 200)
			 * - output_data
			 * - output_mime_type (if different from default text/plain)
			 */				
				
			// Construct new task
			input.data = http_parser.getParams();
			input.cookies = http_parser.getCookies();
			input.location = http_parser.getLocation();
			
			// API call using reflection
			String endpoint = getEndpoint(input.location);
			try {
				Class api_class = Class.forName(getPackageName() + ".app." + endpoint);
				Constructor api_constr = api_class.getConstructor(TaskInput.class);
				Object api_obj = api_constr.newInstance(input);
					
				// Call the method which corresponds to the HTTP method
				String method = http_parser.getMethod();
				Method api_method = api_obj.getClass().getMethod(method.toLowerCase());
				api_method.invoke(api_obj);
					
				// Call the method to get back the output
				Method api_method_get_output_data = api_obj.getClass().getMethod("onCompletion");
				output = (TaskOutput) api_method_get_output_data.invoke(api_obj);					
			}
			catch (Exception e) {
				output.http_status = 404;
			}
		} // End API call here
		
		// Prepare body
		if (output.data == null)
			output.data = http_parser.getHttpMessageForCode(output.http_status).getBytes();	
		
		// Log the request
		// Note: we don't handle authentication, hence user is always "-"
		// Who's there?
		Logger.log_access(socketChannel.socket().getInetAddress().getHostAddress(), "-", http_parser.getFirstLine(), output.http_status, output.data.length);
		
		// Prepare headers
		headers = http_parser.getHttpReplyHeaders(output.http_status, output.mime_type);
		headers += "Content-Length: " + output.data.length + "\n";
		
		output.buildCookies();
		if (output.cookies != null)
			headers += output.cookies;

		headers += "\n";
		headers_len = headers.length();
			
		// Combine headers and body
		response = new byte[headers_len + output.data.length];
    	System.arraycopy(headers.getBytes(), 0, response, 0, headers_len);
    	System.arraycopy(output.data, 0, response, headers_len, output.data.length);
	
    	// Push response back
		synchronized(queue) {
			queue.add(new ServerTcpDataEvent(server, socketChannel, response));
			queue.notify();
		}
		
		// Stats
		if (ServerStats.http_requests.containsKey(created)) {
			requests = ServerStats.http_requests.get(created);
			requests ++;			
		}
		ServerStats.http_requests.put(created, requests);
		
		exec_time = System.currentTimeMillis() - run_start_time;
		if (ServerStats.http_exec.containsKey(created))
			exec_time += ServerStats.http_exec.get(created);
		ServerStats.http_exec.put(created, exec_time);
				
		// Ready for new task
		busy = false;
	}
	
	/**
	 * The main loop of the worker thread
	 */
	public void run() {
		Logger.trace(className, "Running new as a new thread.");
		ServerTcpDataEvent dataEvent;
		
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
				dataEvent = (ServerTcpDataEvent) queue.remove(0);
			}
			
			// Return to sender
			dataEvent.server.send(dataEvent.socket, dataEvent.data);
		}
	}
	
	/**
	 * Helper method to derive the name of the API endpoint from the original URL
	 * @param location String The local part of the original URL
	 * @return String The name of the API endpoint
	 */
	private String getEndpoint(String location) {
		String[] parts = location.split("/");
		if (parts.length < 2)
			return location;
		return parts[1];
	}
	
	private String getPackageName() {
		String[] parts = this.getClass().getPackage().toString().split(" ");
		String[] parts2 = parts[1].split("\\.");
		String ret = "";
		for (int i=0; i< parts2.length - 1; i++) {
			if (ret != "")
				ret += ".";
			ret += parts2[i];
		}
		return ret;
	}
		
	/**
	 * Getter for the 'busy' property
	 * @return
	 */
	public boolean isBusy() {
		return busy;
	}
}
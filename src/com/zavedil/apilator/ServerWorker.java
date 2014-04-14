package com.zavedil.apilator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class ServerWorker implements Runnable {
	private List queue = new LinkedList();
	private String className;
	private final String ERROR_MGS_500="There is something very, very wrong with your request. Or with me.";
	private final String ERROR_MGS_404="Sorry, dude. Not found.";
	
	public void processData(Server server, SocketChannel socketChannel, byte[] data, int count) throws IOException {
		HttpParser http_parser=null;
		String http_resp_head=null, http_resp_mime_type="text/plain";
		int http_resp_status, http_resp_head_len=0, http_resp_body_len=0;
		byte[] http_resp_body=null, http_resp;
		
		Logger.debug(className, "Entering function processData.");
		boolean headers_ok = false;
		
		try {
			http_parser = new HttpParser(data, count);
			http_resp_status = http_parser.parseRequest();
			
			if (http_resp_status == 0)
				return;
			else if (http_resp_status == 200)
				headers_ok = true;
			else
				http_resp_body = ERROR_MGS_500.getBytes();
		}
		catch (UnsupportedEncodingException e) {
			http_resp_status = 500;
			http_resp_body = ERROR_MGS_500.getBytes();
			headers_ok = false;
		}
		catch (IOException e) {
			http_resp_status = 500;
			http_resp_body = ERROR_MGS_500.getBytes();
			headers_ok = false;
		}
		
		if (headers_ok) {
			String location = http_parser.getLocation();
			if (serveStatic(location)) {
				// Call the static content class
				
				StaticContent static_content = new StaticContent(location);
				if (static_content.getError()) {
					http_resp_status = 404;
					http_resp_body = ERROR_MGS_404.getBytes();
				}
				else {
					http_resp_body = static_content.getFileContent();
					http_resp_mime_type = static_content.getMimeType();
				}
			}
			else {
				/**
				 * We call the API here; we/it should set:
				 * - http_resp_status (if not 200)
				 * - http_resp_body
				 * - http_resp_body_len (if not set, it will be calculated later as http_resp_body.length)
				 * - http_resp_mime_type (if different from default text/plain)
				 */				
				
				/*
				// API calling example:
				Hashtable params = http_parser.getParams();
				ApiEndpointExample api_endpoint_example = new ApiEndpointExample(params);
				String method = http_parser.getMethod();
				switch (method) {
					case "GET":
				 		api_endpoint_example.get();
				 		break;
				 	case "POST":
				 		api_endpoint_example.post();
				 		break;
				 	case "PUT":
				 		api_endpoint_example.put();
				 		break;
				 	case "DELETE":
				 		api_endpoint_example.delete();
				 		break;
				}
				http_resp_body = api_endpoint_example.getOutput();
				http_resp_body_len = api_endpoint_example.getOutputLen();
				http_resp_mime_type = api_endpoint_example.getOutputMimeType();
				http_resp_status = api_endpoint_example.getOutputHttpStatus();
				*/
				
				/*
				// API call example using reflection
				Hashtable params = http_parser.getParams();
				String endpoint = getEndpoint(location);
				try {
					Class api_class = Class.forName(endpoint);
					Constructor api_constr = api_class.getConstructor(Hashtable.class);
					Object api_obj = api_constr.newInstance(params);
					
					String method = http_parser.getMethod();
					Method api_method = api_obj.getClass().getMethod(method.toLowerCase(), (Class<?>) null);
					api_method.invoke(api_obj, (Object) null);
					
					Method api_method_get_output = api_obj.getClass().getMethod("getOutput", (Class<?>) null);
					http_resp_body = (byte[]) api_method_get_output.invoke(api_obj, (Object) null);
					
					Method api_method_get_output_len = api_obj.getClass().getMethod("getOutputLen", (Class<?>) null);
					http_resp_body_len = (int) api_method_get_output_len.invoke(api_obj, (Object) null);
					
					Method api_method_get_http_status = api_obj.getClass().getMethod("getOutputHttpStatus", (Class<?>) null);
					http_resp_status = (int) api_method_get_http_status.invoke(api_obj, (Object) null);
					
					Method api_method_get_mime_type = api_obj.getClass().getMethod("getOutputMimeType", (Class<?>) null);
					http_resp_mime_type = (String) api_method_get_mime_type.invoke(api_obj, (Object) null);
				}
				catch (ClassNotFoundException e) {
					http_resp_status = 404;
					http_resp_body = ERROR_MGS_404.getBytes();
				}
				*/
				
				
				Hashtable params = http_parser.getParams();
				if (params.containsKey("myfile") && params.containsKey("myfile_fn")) {			
					byte[] myfile = (byte []) params.get("myfile");
					String myfile_fn = params.get("myfile_fn").toString();
					FileOutputStream fout = new FileOutputStream("/tmp/" + myfile_fn);
					fout.write(myfile);
					fout.close();

					byte[] myfile2 = (byte []) params.get("myfile2");
					String myfile2_fn = params.get("myfile2_fn").toString();
					FileOutputStream fout2 = new FileOutputStream("/tmp/" + myfile2_fn);
					fout2.write(myfile2);
					fout2.close();
					
					http_resp_body = "Yeeeeeee!".getBytes();
				}
				else {
					http_resp_status = 404;
					http_resp_body = ERROR_MGS_404.getBytes();
				}
			
				/*
				// Let's say param 'filename' has the desired filename... and serve it statically
				Hashtable params = http_parser.getParams();
				if (params.containsKey("filename")) {
					//String location = params.get("filename").toString();
					String location = "4F2C1563.jpg";
					StaticContent static_content = new StaticContent("/" + location);
								
					if (static_content.getError()) {
						http_resp_status = 404;
						http_resp_body = ERROR_MGS_404.getBytes();
					}
					else {
						http_resp_body = static_content.getFileContent();
						http_resp_body_len = static_content.getFileSize();
						http_resp_mime_type = static_content.getMimeType();
					}
				}
				else {
					http_resp_status = 404;
					http_resp_body = ERROR_MGS_404.getBytes();
				}
				*/
				
				// End API call here
			}
		}
		
		// Log the request
		// Note: we don't handle authentication, hence user is always "-"
		// Who's there?
		Logger.log_access(socketChannel.socket().getInetAddress().getHostAddress(), "-", http_parser.getFirstLine(), http_resp_status, http_resp_body_len);
		
		// Prepare headers
		http_resp_head = http_parser.getHttpReplyHeaders(http_resp_status, http_resp_mime_type);
		http_resp_head += "Content-Length: " + http_resp_body_len + "\n";
		http_resp_head += "\n";
		http_resp_head_len = http_resp_head.length();
		
		// Prepare body
		if (http_resp_body_len == 0)
			http_resp_body_len = http_resp_body.length;
		
		// Combine headers and body
		http_resp = new byte[http_resp_head_len + http_resp_body_len];
    	System.arraycopy(http_resp_head.getBytes(), 0, http_resp, 0, http_resp_head_len);
    	System.arraycopy(http_resp_body, 0, http_resp, http_resp_head_len, http_resp_body_len);
	
    	// Push response back
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socketChannel, http_resp));
			queue.notify();
		}
	}
	
	public void run() {
		ServerDataEvent dataEvent;
		className = this.getClass().getSimpleName();
		
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
	
	private boolean serveStatic(String location) {
		int idx = location.indexOf("/api");
		if (idx == 0)
			return false;
		return true;
	}
	
	private String getEndpoint(String location) {
		String[] parts = location.split("/");
		return parts[1];
	}
}
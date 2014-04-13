package com.zavedil.apilator;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class Worker implements Runnable {
	private List queue = new LinkedList();
	private HttpParser http_parser;
	private String mime_type = "text/plain";
	private int http_resp_status;
	private String http_resp_head;
	private int http_resp_head_len;
	private byte[] http_resp_body;
	private int http_resp_body_len;
	private byte[] http_resp;
	private String className;
	
	public void processData(Server server, SocketChannel socket, byte[] data, int count) throws IOException {
		boolean headers_ok = false;
		
		try {
			http_parser = new HttpParser(data, count);
			http_resp_status = http_parser.parseRequest();
			
			if (http_resp_status == 0)
				return;
			if (http_resp_status == 200)
				headers_ok = true;
			else {
				http_resp_body = "There is something very, very wrong with your request. Or with me.".getBytes();
				http_resp_body_len = http_resp_body.length;
			}
		}
		catch (UnsupportedEncodingException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your request. Or with me.".getBytes();
			http_resp_body_len = http_resp_body.length;
			mime_type = "text/plain";
			headers_ok = false;
		}
		catch (IOException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your request. Or with me.".getBytes();
			http_resp_body_len = http_resp_body.length;
			mime_type = "text/plain";
			headers_ok = false;
		}
	
		if (headers_ok) {
			if (serveStatic(http_parser.getLocation())) {
				// Call the static content class
				String location = http_parser.getLocation();
				
				StaticContent static_content = new StaticContent(location);
				if (static_content.getError()) {
					http_resp_status = 404;
					http_resp_body = "Sorry, dude. Not found.".getBytes();
					http_resp_body_len = http_resp_body.length;
					mime_type = "text/plain";
				}
				else {
					http_resp_body = static_content.getFileContent();
					http_resp_body_len = static_content.getFileSize();
					mime_type = static_content.getMimeType();
				}
			}
			else {
				/**
				 * We call the API here; we/it should set:
				 * - http_resp_status (if not 200)
				 * - http_resp_body
				 * - http_resp_body_len
				 * - mime_type (if different from default text/plain)
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
					http_resp_body_len = http_resp_body.length;
				}
				else {
					http_resp_status = 404;
					http_resp_body = "Sorry, dude. Not found.".getBytes();
					http_resp_body_len = http_resp_body.length;
				}
			
				/*
				// Let's say param 'filename' has the desired filename... and serve it statically
				Hashtable params = http_parser.getParams();
				//if (params.containsKey("filename")) {
					//String location = params.get("filename").toString();
					String location = "4F2C1563.jpg";
					StaticContent static_content = new StaticContent("/" + location);
								
					if (static_content.getError()) {
						http_resp_status = 404;
						http_resp_body = "Sorry, dude. Not found.".getBytes();
						http_resp_body_len = http_resp_body.length;
						mime_type = "text/plain";
					}
					else {
						http_resp_body = static_content.getFileContent();
						http_resp_body_len = static_content.getFileSize();
						mime_type = static_content.getMimeType();
					}
				//}
				//else {
				//	http_resp_status = 404;
				//	http_resp_body = "Sorry, dude. Not found.".getBytes();
				//	http_resp_body_len = http_resp_body.length;
				//}
				*/
				
				// End API call here
			}
		}
		
		// Prepare headers
		http_resp_head = http_parser.getHttpReplyHeaders(http_resp_status, mime_type);
		http_resp_head += "Content-Length: " + http_resp_body_len + "\n";
		http_resp_head += "\n";
		http_resp_head_len = http_resp_head.length();
		
		// Prepare body
		http_resp = new byte[http_resp_head_len + http_resp_body_len];
    	System.arraycopy(http_resp_head.getBytes(), 0, http_resp, 0, http_resp_head_len);
    	System.arraycopy(http_resp_body, 0, http_resp, http_resp_head_len, http_resp_body_len);
	
    	// Push response back
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socket, http_resp));
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
}
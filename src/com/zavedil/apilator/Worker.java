package com.zavedil.apilator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class Worker implements Runnable {
	private List queue = new LinkedList();
	private HttpParser http_parser;
	private int http_resp_status;
	private String http_resp_head;
	private int http_resp_head_len;
	private byte[] http_resp_body;
	private int http_resp_body_len;
	private byte[] http_resp;
	
	public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
		int headers_ok = 1;
		
		try {
			http_parser = new HttpParser(data);
			http_resp_status = http_parser.parseRequest();
		}
		catch (UnsupportedEncodingException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your reauest. Or with me.".getBytes();
			headers_ok = 0;
		}
		catch (IOException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your reauest. Or with me.".getBytes();
			headers_ok = 0;
		}

		if (headers_ok == 1) {
			if (serveStatic(http_parser.getLocation())) {
				// Call the static content class
				String location = http_parser.getLocation();
				
				StaticContent static_content = new StaticContent(location);
				if (static_content.getError()) {
					http_resp_status = 404;
					http_resp_body = "Sorry, dude. Not found.".getBytes();
				}
				else {
					http_resp_body = static_content.getFileContent();
					http_resp_body_len = static_content.getFileSize();
				}
			}
			else {
				// We call the API here
				// Let's sat param 'filename' has the desired filename...
				Hashtable params = http_parser.getParams();
				if (params.get("filename") != null) {
					String location = params.get("filename").toString();
					StaticContent static_content = new StaticContent("/" + location);
					
					if (static_content.getError()) {
						http_resp_status = 404;
						http_resp_body = "Sorry, dude. Not found.".getBytes();
						http_resp_body_len = http_resp_body.length;
					}
					else {
						http_resp_body = static_content.getFileContent();
						http_resp_body_len = static_content.getFileSize();
					}
				}
				else {
					http_resp_status = 404;
					http_resp_body = "Sorry, dude. Not found.".getBytes();
					http_resp_body_len = http_resp_body.length;
				}
				
				//http_resp_body = "Lalala, some nice text!".getBytes();
				//http_resp_body_len = http_resp_body.length;
			}
		}
		
		// Prepare heders
		http_resp_head = http_parser.getHttpReplyHeaders(http_resp_status);
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
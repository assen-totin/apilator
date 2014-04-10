package com.zavedil.apilator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class Worker implements Runnable {
	private List queue = new LinkedList();
	private HttpParser http_parser;
	private int http_resp_status;
	private String http_resp_head;
	private String http_resp_body;
	private int http_resp_body_len;
	private String http_resp;
	
	public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
		int headers_ok = 1;
		
		try {
			http_parser = new HttpParser(data);
			http_resp_status = http_parser.parseRequest();
		}
		catch (UnsupportedEncodingException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your reauest. Or with me.";
			headers_ok = 0;
		}
		catch (IOException e) {
			http_resp_status = 500;
			http_resp_body = "There is something very, very wrong with your reauest. Or with me.";
			headers_ok = 0;
		}

		if (headers_ok == 1)
			http_resp_body = "Lalala, some nice text!";
		
		http_resp_body_len = http_resp_body.length();
		
		http_resp_head = http_parser.getHttpReplyHeaders(http_resp_status);
		http_resp_head += "Content-Length: " + http_resp_body_len + "\n";
		
		http_resp = http_resp_head + "\n" +  http_resp_body;
		
		byte[] data_resp = http_resp.getBytes();
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socket, data_resp));
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
}
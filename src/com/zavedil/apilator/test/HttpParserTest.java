package com.zavedil.apilator.test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.zavedil.apilator.core.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HttpParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEmptyInput() {
		final String input_s = "";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Empty request should return 0", 0, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLeadingSpace() {
		final String input_s = " GET / HTTP/1.0";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Leading space in request should return 400", 400, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIncorrectRequest() {
		final String input_s = "GET / HTTP/1.0 123";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Request with more or less that 3 space-separated segments should return 400", 400, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testProtocol() {
		final String input_s = "GET / FTP/1.0";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Request with protocol other than HTTP should return 400", 400, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testProtocolVersion() {
		final String input_s = "GET / HTTP/a.b";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Request with protocol other than N.M should return 400", 400, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMissingHeaders() {
		final String input_s = "GET / HTTP/1.0";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Missing headers in request should return 0", 0, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testHostHeader() {
		final String input_s = "GET / HTTP/1.1\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("Missing Host header when protocol is 1.1 should return 400", 400, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodTrace() {
		final String input_s = "TRACE / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("TRACE method should return 501", 501, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodConnect() {
		final String input_s = "CONNECT / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("CONNECT method should return 501", 501, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodGet() {
		final String input_s = "GET / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("GET method should return 200", 200, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodHead() {
		final String input_s = "HEAD / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("HEAD method should return 200", 200, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodDelete() {
		final String input_s = "DELETE / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("DELETE method should return 200", 200, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMethodOptions() {
		final String input_s = "OPTIONS / HTTP/1.1\r\nHost:blah.com\r\n\r\n";
		final byte[] input_b = input_s.getBytes();
		
		try {
			HttpParser parser = new HttpParser(input_b, input_b.length);

			assertEquals("OPTIONS method should return 200", 200, parser.parseRequest());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

package com.zavedil.apilator;

/**
 * HTTP parser class.
 * @author Juho Vaha-Herttua
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original copyright (C) 2004  Juho Vaha-Herttua.
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

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.URLDecoder;

public class HttpParser {
	private final String HttpServerName;
	private static final Hashtable<Integer,String> HttpReplies = new Hashtable<Integer,String>() {{
		put(100, "Continue");
		put(101, "Switching Protocols");
		put(200, "OK");
		put(201, "Created");
		put(202, "Accepted");
		put(203, "Non-Authoritative Information");
		put(204, "No Content");
		put(205, "Reset Content");
		put(206, "Partial Content");
		put(300, "Multiple Choices");
		put(301, "Moved Permanently");
		put(302, "Found");
		put(303, "See Other");
		put(304, "Not Modified");
		put(305, "Use Proxy");
		put(306, "(Unused)");
		put(307, "Temporary Redirect");
		put(400, "Bad Request");
		put(401, "Unauthorized");
		put(402, "Payment Required");
		put(403, "Forbidden");
		put(404, "Not Found");
		put(405, "Method Not Allowed");
		put(406, "Not Acceptable");
		put(407, "Proxy Authentication Required");
		put(408, "Request Timeout");
		put(409, "Conflict");
		put(410, "Gone");
		put(411, "Length Required");
		put(412, "Precondition Failed");
		put(413, "Request Entity Too Large");
		put(414, "Request-URI Too Long");
		put(415, "Unsupported Media Type");
		put(416, "Requested Range Not Satisfiable");
		put(417, "Expectation Failed");
		put(500, "Internal Server Error");
		put(501, "Not Implemented");
		put(502, "Bad Gateway");
		put(503, "Service Unavailable");
		put(504, "Gateway Timeout");
		put(505, "HTTP Version Not Supported");
	}};

	private BufferedReader reader;
	//private DataInputStream binary_reader;
	private byte[] request=null;
	private String first_line, method, url, location, post_data=null, boundary;
	private Hashtable headers=null, params=null;
	private int[] ver;
	private String className;
	private int header_bytes=0, received_bytes=0, content_length=0;

	/**
	 * Constructor
	 * @param data byte[] The input data
	 * @param count int The size of the input data
	 * @throws UnsupportedEncodingException
	 */
	public HttpParser(byte[] data, int count) throws UnsupportedEncodingException {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Entering function HttpParser");
		method = "";
		url = "";
		headers = new Hashtable();
		params = new Hashtable();
		ver = new int[2];		
		received_bytes = count;
		HttpServerName = Config.getSystemName();
		
		request = new byte[data.length];
		System.arraycopy(data, 0, request, 0, data.length);
		//binary_reader= new DataInputStream(new ByteArrayInputStream(request));
		
		reader = new BufferedReader(new StringReader(new String(data, "UTF-8")));
	}
  
	/**
	 * Main method used for parsing HTTP
	 * @return int The result code as HTTP code with 0 as special case when no enough data has arrived yet
	 * @throws IOException
	 */
	public int parseRequest() throws IOException {
		Logger.debug(className, "Entering function parseRequest");
	    String prms[]=null, cmd[], temp[];
	    int ret, idx, i;
	
	    ret = 200; // default is OK now
	    
	    // Parse first line and obtain method and protocol (URL will be retrieved later depending on method)
	    first_line = reader.readLine();

	    if (first_line == null || first_line.length() == 0) 
	    	return 0;
	    if (Character.isWhitespace(first_line.charAt(0))) {
	      	//starting whitespace, return bad request
	    	return 400;
	    }
	    
	    cmd = first_line.split("\\s");
	    if (cmd.length != 3)
	    	return 400;
	
	    method = cmd[0];
	    
	    if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
	    	temp = cmd[2].substring(5).split("\\.");
	    	try {
	    		ver[0] = Integer.parseInt(temp[0]);
	    		ver[1] = Integer.parseInt(temp[1]);
	    	} 
	    	catch (NumberFormatException nfe) {
	    		return 400;
	    	}
	    }
	    else
	    	return 400;
	    
	    header_bytes += first_line.length() + 2; // Don't forget the CRLF stripped by Java
	    
	    // Parse the rest of the headers
	    parseHeaders();
	    if (headers == null)
	    	return 0;

	    // HTTP/1.1 mandatory header check
	    if (ver[0] == 1 && ver[1] >= 1 && getHeader("host") == null) 
	    	return 400;

	    // We don't implement certain HTTP/1.1 methods
	    if ((ver[0] == 1 && ver[1] >= 1) && 
	    	(method.equals("OPTIONS") || method.equals("TRACE") || method.equals("CONNECT"))) 
	    		return 501; // not implemented
	    
	    // GET, HEAD, DELETE methods
	    else if (method.equals("GET") || method.equals("HEAD") || method.equals("DELETE")) {
	    	idx = cmd[1].indexOf('?');
	          	
	    	if (idx < 0) 
	    		url = cmd[1];
	    	else {
	    		url = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
	    		prms = cmd[1].substring(idx+1).split("&");
	    		parseGet(prms);
	    	}
	    }
	    
	    // POST, PUT Methods
	    else if (method.equals("POST") || method.equals("PUT")) {
			if (getHeader("content-length") == null)
				return 0;
				
			if (getHeader("content-type") == null)
				return 0;
	    	
		    // Check if we have received enough bytes from the non-blocking socket
	    	content_length = Integer.parseInt(getHeader("content-length"));
	    	if ((content_length + header_bytes) > received_bytes) {
	    		Logger.debug(className, "Not enough bytes yet, returning...");
	    		return 0;
	    	}
	    	
	    	url = cmd[1];
	    	
	    	String content_type = headers.get("content-type").toString();
	    	
	    	// Processing of URL-encoded data
	    	if (content_type.equals("application/x-www-form-urlencoded")) {
	    		parseBodyUrlencoded();
	    		
	    		if (post_data != null) {
    				prms = post_data.split("&");
	    			parseGet(prms);
	    		}
	    		else
	    			return 400;
	    	}
	    	// Processing of multipart-form data
	    	else if (content_type.indexOf("multipart/form-data") != -1) {
	        	if ((idx = content_type.indexOf("boundary=")) > 0)
	        		boundary = content_type.substring(idx + 9);
	        	else
	        		return 400;

	    		parseBodyMultipart();
	    	}
	    	// We don't implement other data encodings for POST/PUT
	    	else 
	    		return 501; // WTF? POST with no proper Content-Type?
	    }
	    
	    // No other methods are allowed by HTTP
	    else 
	    	return 400;
		
	    // Store the local part of the URL
	    parseLocation();
	    
		return ret;
	}

	/**
	 * HTTP header parsing method
	 * @throws IOException
	 */
	private void parseHeaders() throws IOException {
		Logger.debug(className, "Entering function parseHeaders");
		String line=null;
		int idx;
		
	    // that fscking rfc822 allows multiple lines, we don't care for now	
	    while (!(line = reader.readLine()).equals("")) {
	    	header_bytes += line.length() + 2;	// Don't forget the CRLF which was stripped by Java
			if ((idx = line.indexOf(':')) > 0)
				headers.put(line.substring(0, idx).toLowerCase(), line.substring(idx + 1).trim());
			// else we don't care about this line
	    }
	    header_bytes += 2; // Account for the last line of the header - the separator
	}
	
	/**
	 * HTTP request parameters parsing method for URL encoded pairs (GET and url-encoded POST) 
	 * @param prms String[] Name=Value pairs from the request
	 * @throws UnsupportedEncodingException
	 */
	private void parseGet(String prms[]) throws UnsupportedEncodingException {
		Logger.debug(className, "Entering function parseGet");
		int i;
		String temp[];
		  
		params = new Hashtable();
		for (i=0; i<prms.length; i++) {
			temp = prms[i].split("=");
	        if (temp.length == 2) {
	          // we use ISO-8859-1 as temporary charset and then
	          // String.getBytes("ISO-8859-1") to get the data
	          params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), URLDecoder.decode(temp[1], "ISO-8859-1"));
	        }
	        else if(temp.length == 1 && prms[i].indexOf('=') == prms[i].length()-1) {
	        	// handle empty string separately
	        	params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
	        }
		}
	}
  
	/**
	 * HTTP request body parsing method for URL encoded POST
	 * @throws IOException
	 */
	private void parseBodyUrlencoded() throws IOException {
		Logger.debug(className, "Entering function parseBodyUrlencoded");
		char[] post_chars;
		
		post_chars = new char[content_length];
		reader.read(post_chars, 0, content_length);
		post_data = new String(post_chars);
	}
	
	/**
	 * HTTP request body parsing method for multipart-form data POST
	 * @throws IOException
	 */
	private void parseBodyMultipart() throws IOException {
		Logger.debug(className, "Entering function parseBodyMultipart");
		Logger.debug(className, "Boundary is: " + boundary);
		String line=null, name=null, value=null, filename=null;
		String temp[], temp2[], temp3[];
		String encoding="binary"; // RFC 2045 says this should be 7bit, but FF and Chrome default to binary
		int idx, body_bytes=0;
		byte[] filedata=null, filedata_encoded=null, filedata_tmp=null;
		boolean read_data=false, count_body_bytes=true;
		
		// Loop through the body using text reader
    	while ((line = reader.readLine()) != null) {
    		//Logger.debug(className, "Got line!");
    		
    		// Empty lines (CRLF or NULL bytes)
    		if (line.equals("")) {
    			//Logger.debug(className, "Empty line!");
        		if(count_body_bytes) {
        			body_bytes += line.length() + 2; // Don't forget the CRLF that Java stripped
        			//Logger.debug(className, "body_bytes now is: " + body_bytes);
        		}

    			// Next line will be data
    			if (filename == null) {
    				// Regular field: store plain as this should be unencoded, single-line
    				//Logger.debug(className, "Reading text data next!");
    				//Logger.debug(className, "Storing plain!");
    				value = reader.readLine();
    				if(count_body_bytes) {
    					body_bytes += value.length() + 2;
    					Logger.debug(className, "body_bytes now is: " + body_bytes);
    				}
    			}
    			else if (read_data) {
    				// Read binary data
    				//Logger.debug(className, "Reading binary data next!");
    				//Logger.debug(className, "header_bytes : " + header_bytes);
    				//Logger.debug(className, "body_bytes : " + body_bytes);

        			// Get a binary decoder
    				HttpDecodeBinary decoder = new HttpDecodeBinary();
    				
    				// Copy the input data from current position to the end in a new chunk
    				byte[] binary_tmp = new byte[content_length];       				
        			System.arraycopy(request, header_bytes + body_bytes, binary_tmp, 0, content_length - body_bytes);  				
        				        				
        			// Seek the first occurrence of boundary and get it as offset
        			int offset = decoder.indexOf(binary_tmp, boundary.getBytes());
        			//Logger.debug(className, "offset_bytes : " + offset);
        			if (offset > -1) {
        				// Copy the input data from same starting position to the offset in a new chunk
        				filedata_tmp = new byte[offset];
        				System.arraycopy(request, header_bytes + body_bytes, filedata_tmp, 0, offset);       				 
        				
        				// The offset may include a trailing CRLF plus part of the boundary prefix;
        				// to remove them, seek the last occurrence of CRLF in the same chunk
        				int offset2 = decoder.indexOfLast(filedata_tmp, "\r\n".getBytes());
        				//Logger.debug(className, "offset2_bytes : " + offset2);
        				
        				if (offset2 > -1) {
        					// Copy the input data from the same starting position to the last CRLF (without it).
        					filedata_encoded = new byte[offset2];
        					System.arraycopy(filedata_tmp, 0, filedata_encoded, 0, offset2);
        					body_bytes += offset2 + 2; // Don't forget the CR/LF that was stripped
        				}
        				else {
        					filedata_encoded = filedata_tmp;
        					body_bytes += offset;
        				}        				
        				//Logger.debug(className, "body_bytes now is: " + body_bytes);
        			}
        			else
        				Logger.warning(className, "Could not extract uploaded file with name: " + filename);
        			
        			// Disable binary read
        			read_data = false;
        			
        			count_body_bytes = false;
    			}
    			continue;
    		}

    		// Lines which match the boundary
    		if (line.indexOf(boundary) != -1) {
    			body_bytes += line.length() + 2;
    			//Logger.debug(className, "body_bytes now is: " + body_bytes);
    			
    			//Logger.debug(className, "Boundary line!");
    			// Re-enable reading binary data after next empty line
    			read_data = true;
    			
    			// Re-enable counting bytes when an empty line is encountered
    			count_body_bytes = true;
    			
    			// Reached end of section - flush what we have so far
    			if ((filename == null) && (name != null) && (value != null)) {
        			// Regular (text) attributes
    				//Logger.debug(className, "Store regular! " + name + ":" + value);
    				params.put(name, value);
    				
    				// Reset temporary variables to defaults
    				name = null;
    				value = null;
    			}
    			else if ((filename != null) && (name != null) && (filedata_encoded != null)) {
    				// Uploaded files - some data needs decoding
    				//Logger.debug(className, "Store file!");
    				//Logger.debug(className, "Encoding is: " + encoding);
    				switch (encoding) {
    					case "base64": 
    						HttpDecodeBase64 decoder_b64 = new HttpDecodeBase64();
    						filedata = decoder_b64.decode(filedata_encoded);
    						break;
    					case "quoted-printable": 
    						HttpDecodeQuotedPrintable decoder_qp = new HttpDecodeQuotedPrintable();
    						filedata = decoder_qp.decode(filedata_encoded);
    						break;
    					case "7bit": 
    						HttpDecode7Bit decoder_7b = new HttpDecode7Bit();
    						filedata = decoder_7b.decode(filedata_encoded);
    						break;
    					case "8bit":
    					case "binary":
    						filedata = filedata_encoded;
    						break;
    				}
    				
    				params.put(name, filedata);
    				params.put(name + "_fn", filename);

    				// Reset temporary variables to defaults
    				name = null;
    				filedata = null;
    				filename = null;
    				encoding = "binary";
    			}
    			continue;
    		}
    		
    		// Lines which contain attributes
    		if ((idx = line.indexOf(":")) > 0) {
    			//Logger.debug(className, "Attribute line!");
    			// Only count the lines if we have not just read some binary data 
    			// (which may give false positives until the text reader gets to the next boundary)
    			if(count_body_bytes) {
    				body_bytes += line.length() + 2;
    				//Logger.debug(className, "body_bytes now is: " + body_bytes);
    			}
    			
    			// Retrieve and store the attributes we find interesting
    			temp = line.split(":");
    			if (temp[0].toLowerCase().equals("content-transfer-encoding")) {
    				//Logger.debug(className, "Encoding attribute!");
    				// Transfer encoding - used for files when not in default (binary) mode
    				encoding = line.substring(idx + 1).toLowerCase().trim();
    			}
    			else if (temp[0].toLowerCase().equals("content-disposition")) {
    				//Logger.debug(className, "Disposition attribute!");
    				// Content disposition - contains field name (and filename for files)
    				String right = line.substring(idx + 1).trim();
    				temp2 = right.split("\\s");
    				for (int i=0; i < temp2.length; i++) {
    					if (temp2[i].indexOf("=") > 0) {
    						temp3 = temp2[i].split("=");
    						if (temp3[0].equals("name")) {
    							name = cleanQuotes(temp3[1]);
    							//Logger.debug(className, "Name element: " + name);
    						}
    						else if (temp3[0].equals("filename")) {
    							filename = cleanQuotes(temp3[1]);
    							//Logger.debug(className, "Filename element: " + filename);
    						}
    						// else we don't care about this attribute
    					}
    				}
    			}
   				// else we don't care about this section header line
    		}
    	}
    	//Logger.debug(className, "TOTAL! body_bytes: " + body_bytes);
    }
	
	/**
	 * Helper method to retrieve the local part of the URL 
	 */
	private void parseLocation() {
		Logger.debug(className, "Entering function parseLocation");
		location = url;
		int idx;
		// The URL arrives stripped of any GET params, but may begin with a "http://host/..." 
		// or just with "//location/..." 
		// We need to strip those. 
		if ((idx = url.indexOf("//")) > 0) 
			location = url.substring(idx + 1).trim();
	}

	/**
	 * Getter method for HTTP method
	 * @return String The HTTP Method used
	 */
	public String getMethod() {
		return method;
	}
  
	/**
	 * Getter method for location
	 * @return String The local part of the URL
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Getter method for any HTTP header
	 * @param key String The HTTP header name
	 * @return String The HTTP header value
	 */
	public String getHeader(String key) {
		if (headers != null) {
			if (headers.containsKey(key.toLowerCase())) {
				return (String) headers.get(key.toLowerCase());
			}
		}
		return null;
	}

	/**
	 * Getter method for all HTTP headers
	 * @return Hashtable All HTTP headers
	 */
	public Hashtable getHeaders() {
		return headers;
	}

	/**
	 * Getter method for URL
	 * @return String The URL
	 */
	public String getRequestURL() {
		return url;
	}

	/**
	 * Getter method for GET or POST parameter
	 * @param key String Parameter name
	 * @return String Parameter value
	 */
	public String getParam(String key) {
		return (String) params.get(key);
	}

	/**
	 * Getter method for all GET or POST parameters
	 * @return Hashtable All GET or POST parameters
	 */
	public Hashtable getParams() {
		return params;
	}

	/**
	 * Getter method for HTTP version of the request
	 * @return String The HTTP version of the request
	 */
	public String getVersion() {
		return ver[0] + "." + ver[1];
	}

	/**
	 * Getter method for the first line of the HTTP request
	 * @return String The first line of the HTTP request
	 */
	public String getFirstLine() {
		return first_line;
	}
	
	/**
	 * Helper method to compare HTTP version from the request to a specified version
	 * @param int major The major part of the version to compare
	 * @param int minor The minor part of the version to compare
	 * @return int 0 if version matches, 1 if the specified version is higher than the one from the request or -1 if it is lower 
	 */
	public int compareVersion(int major, int minor) {
		if (major < ver[0]) return -1;
		else if (major > ver[0]) return 1;
		else if (minor < ver[1]) return -1;
		else if (minor > ver[1]) return 1;
		else return 0;
	}

	/**
	 * Helper method to generate HTTP response headers
	 * @param http_status int The HTTP status code to use in the response headers
	 * @param mime_type String The MIME type to use in the response headers
	 * @return String The generated HTTP response headers
	 */
	public String getHttpReplyHeaders(int http_status, String mime_type) {
		Logger.debug(className, "Entering function getHttpReplyHeaders");
		String ret;
		SimpleDateFormat format;

		ret = "HTTP/" + ver[0] + "." + ver[1] + http_status + HttpReplies.get(http_status).toString() + "\n";
		
		format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		ret += "Date: " + format.format(new Date()) + " GMT\n";
    
		ret += "Server: " + HttpServerName + "\n";
    
		ret += "Content-Type: " + mime_type + "\n";
    
		return ret;
	}
	
	/**
	 * Helper method to get the descriptive text for a given HTTP sttaus code
	 * @param http_status int HTTP status code
	 * @return String Descriptive text for the specified status code
	 */
	public String getHttpMessageForCode(int http_status) {
		return HttpReplies.get(http_status).toString();
	}
	
	/**
	 * Helped method to remove surrounding single or double quotes and trailing semicolon from the end of a parameter value
	 * @param input String The value which needs cleaning
	 * @return The same value with removed surrounding single or double quotes and trailing semicolon 
	 */
	private String cleanQuotes(String input) {
		String output = input;
		output = output.replaceAll(";$", "");
		output = output.replaceAll("^\"|\"$", "");
		output = output.replaceAll("^\'|\'$", "");
		return output;
	}
}

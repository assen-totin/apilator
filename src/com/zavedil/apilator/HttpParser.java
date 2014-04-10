package com.zavedil.apilator;

/**
Copyright (C) 2004  Juho Vaha-Herttua

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.URLDecoder;

public class HttpParser {
	private final String HttpServerName = "Apilator Server";
	private final String[][] HttpReplies = {{"100", "Continue"},
                                                 {"101", "Switching Protocols"},
                                                 {"200", "OK"},
                                                 {"201", "Created"},
                                                 {"202", "Accepted"},
                                                 {"203", "Non-Authoritative Information"},
                                                 {"204", "No Content"},
                                                 {"205", "Reset Content"},
                                                 {"206", "Partial Content"},
                                                 {"300", "Multiple Choices"},
                                                 {"301", "Moved Permanently"},
                                                 {"302", "Found"},
                                                 {"303", "See Other"},
                                                 {"304", "Not Modified"},
                                                 {"305", "Use Proxy"},
                                                 {"306", "(Unused)"},
                                                 {"307", "Temporary Redirect"},
                                                 {"400", "Bad Request"},
                                                 {"401", "Unauthorized"},
                                                 {"402", "Payment Required"},
                                                 {"403", "Forbidden"},
                                                 {"404", "Not Found"},
                                                 {"405", "Method Not Allowed"},
                                                 {"406", "Not Acceptable"},
                                                 {"407", "Proxy Authentication Required"},
                                                 {"408", "Request Timeout"},
                                                 {"409", "Conflict"},
                                                 {"410", "Gone"},
                                                 {"411", "Length Required"},
                                                 {"412", "Precondition Failed"},
                                                 {"413", "Request Entity Too Large"},
                                                 {"414", "Request-URI Too Long"},
                                                 {"415", "Unsupported Media Type"},
                                                 {"416", "Requested Range Not Satisfiable"},
                                                 {"417", "Expectation Failed"},
                                                 {"500", "Internal Server Error"},
                                                 {"501", "Not Implemented"},
                                                 {"502", "Bad Gateway"},
                                                 {"503", "Service Unavailable"},
                                                 {"504", "Gateway Timeout"},
                                                 {"505", "HTTP Version Not Supported"}};

  private BufferedReader reader;
  private String method, url;
  private Hashtable headers, params;
  private String post_data = null;
  private int[] ver;

 
  public HttpParser(byte[] data) throws UnsupportedEncodingException {
	  method = "";
	  url = "";
	  headers = new Hashtable();
	  params = new Hashtable();
	  ver = new int[2];
	  
	  reader = new BufferedReader(new StringReader(new String(data, "UTF-8")));
  }
  
  public int parseRequest() throws IOException {
    String initial, prms[], cmd[], temp[];
    int ret, idx, i;

    ret = 200; // default is OK now
    initial = reader.readLine();
    if (initial == null || initial.length() == 0) return 0;
    if (Character.isWhitespace(initial.charAt(0))) {
      // starting whitespace, return bad request
      return 400;
    }

    cmd = initial.split("\\s");
    if (cmd.length != 3) {
      return 400;
    }

    if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
      temp = cmd[2].substring(5).split("\\.");
      try {
        ver[0] = Integer.parseInt(temp[0]);
        ver[1] = Integer.parseInt(temp[1]);
      } catch (NumberFormatException nfe) {
        ret = 400;
      }
    }
    else ret = 400;

    method = cmd[0];
    
    parseHeaders();
    if (headers == null) 
  	  ret = 400;
    
    if (method.equals("GET") || method.equals("HEAD")) {
      idx = cmd[1].indexOf('?');
      
      if (idx < 0) 
    	  url = cmd[1];
      else {
        url = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
        prms = cmd[1].substring(idx+1).split("&");
        parseGet(prms);
      }
    }
    else if ((method.equals("POST")) || (method.equals("PUT"))) {
    	url = cmd[1];
    	
    	String content_type = params.get("content-type").toString();
    	//String content_length = params.get("Content-Type").toString();
    	
    	if (content_type.equals("application/x-www-form-urlencoded")) {
    		if (post_data.length() > 0) {
    			prms = post_data.split("&");
    			parseGet(prms);
    		}
    	}
    	else {
    		ret = 501; // form-multipart is not implemented
    	}
    }
    else if (ver[0] == 1 && ver[1] >= 1) {
      if (method.equals("OPTIONS") ||
    		  method.equals("DELETE") ||
    		  method.equals("TRACE") ||
    		  method.equals("CONNECT")) {
        ret = 501; // not implemented
      }
    }
    else {
      // meh not understand, bad request
      ret = 400;
    }

    if (ver[0] == 1 && ver[1] >= 1 && getHeader("Host") == null) {
      ret = 400;
    }

    return ret;
  }

  private void parseGet(String prms[]) throws UnsupportedEncodingException {
	  int i;
	  String temp[];
	  
      params = new Hashtable();
      for (i=0; i<prms.length; i++) {
        temp = prms[i].split("=");
        if (temp.length == 2) {
          // we use ISO-8859-1 as temporary charset and then
          // String.getBytes("ISO-8859-1") to get the data
          params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
                     URLDecoder.decode(temp[1], "ISO-8859-1"));
        }
        else if(temp.length == 1 && prms[i].indexOf('=') == prms[i].length()-1) {
          // handle empty string separately
          params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
        }
      }
  }
  
	private void parseHeaders() throws IOException {
		String line;
		int idx;
		int post_flag = 0;
	
	    // that fscking rfc822 allows multiple lines, we don't care now
	    line = reader.readLine();
		while (!line.equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				headers = null;
				break;
			}
			else {
				headers.put(line.substring(0, idx).toLowerCase(), line.substring(idx+1).trim());
	        	// Raise the flag for POST/PUT when we reach Content-Length
				if (((method.equals("POST") || method.equals("PUT"))) && 
	        		line.substring(0, idx).toLowerCase().equals("content-length")) {
					// The next line *should* be the www-urlencoded POST/PUT data
					post_flag = 1;
				}
			}
			line = reader.readLine();
			if (post_flag == 1) {
				// If we have reached Content-Length and have POST/PUT, next line should be the actual data.
				post_data = line;
			}
		}
	}

  public String getMethod() {
    return method;
  }

  public String getHeader(String key) {
    if (headers != null)
      return (String) headers.get(key.toLowerCase());
    else return null;
  }

  public Hashtable getHeaders() {
    return headers;
  }

  public String getRequestURL() {
    return url;
  }

  public String getParam(String key) {
    return (String) params.get(key);
  }

  public Hashtable getParams() {
    return params;
  }

  public String getVersion() {
    return ver[0] + "." + ver[1];
  }

  public int compareVersion(int major, int minor) {
    if (major < ver[0]) return -1;
    else if (major > ver[0]) return 1;
    else if (minor < ver[1]) return -1;
    else if (minor > ver[1]) return 1;
    else return 0;
  }

  public String getHttpReplyHeaders(int codevalue) {
    String key, ret;
    SimpleDateFormat format;
    int i;

    ret = "HTTP/" + ver[0] + "." + ver[1];
    
    key = "" + codevalue;
    for (i=0; i<HttpReplies.length; i++) {
      if (HttpReplies[i][0].equals(key)) {
        ret += " " + codevalue + " " + HttpReplies[i][1] + "\n";
        break;
      }
    }

    format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    ret += "Date: " + format.format(new Date()) + " GMT\n";
    
    ret += "Server: " + HttpServerName + "\n";
    
    ret += "Content-Type: text/html\n";
    
    return ret;
  }

}

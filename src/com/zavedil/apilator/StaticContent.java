package com.zavedil.apilator;

/**
 * A class to serve static content.
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Created for the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticContent {
	private int output_http_status = 200;
	private byte[] output_data = null;
	private String output_http_mime_type = "text/plain";
	private String className;
	
	public StaticContent(String location) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Entering function StaticContent");
		
		final int chunk_size = 1000;
		int curr_len = 0;
		String document_root = Config.getDocumentRoot();
		String file = document_root + location;				// The 'location' will begin with a slash 
		
		try {
            byte[] buffer = new byte[chunk_size];

            FileInputStream inputStream = new FileInputStream(file);

            int nRead = 0;
            while((nRead = inputStream.read(buffer)) != -1) {  
            	byte[] newbuf = new byte[curr_len + nRead];
            	
            	if (curr_len > 0)
            		System.arraycopy(output_data, 0, newbuf, 0, curr_len);
            	
            	System.arraycopy(buffer, 0, newbuf, curr_len, nRead);
            	
            	output_data = newbuf;
            	curr_len = output_data.length;
            }

            inputStream.close();
            
            String file_name = location.substring(1);
            Path path = FileSystems.getDefault().getPath(document_root, file_name);
            output_http_mime_type = Files.probeContentType(path);
        }
        catch(FileNotFoundException ex) {
        	output_http_status = 404;			
        }
        catch(IOException ex) {
        	output_http_status = 404;
        }
	}
	
	/**
	 * Getter for HTTP status code
	 * @return int HTTP status code
	 */
	public int getOutputHttpStatus() {
		return output_http_status;
	}
	
	/**
	 * Getter for output data
	 * @return byte[] Output data
	 */
	public byte[] getOutputData() {
		return output_data;
	}
	
	/**
	 * Getter for MIME type of the output data
	 * @return String MIME type
	 */
	public String getOutputMimeType() {
		return output_http_mime_type;
	}
}

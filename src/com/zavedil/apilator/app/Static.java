package com.zavedil.apilator.app;

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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.zavedil.apilator.core.*;

public class Static extends Endpoint {
	private String className;
	
	/**
	 * Constructor method
	 * @param api_task TaskInput The input data from the HTTP request
	 */
	public Static(TaskInput api_task) {
		super(api_task);
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating a new instance.");
	}
	
	/**
	 * Method invoked whenever a GET request is received.
	 */
	@Override
	public void get() {
		Logger.debug(className, "Entering function get.");
		super.get();
		
		final int chunk_size = 1000;
		int curr_len = 0;
		String document_root = Config.DocumentRoot;
		String fs_part = stripEndpointFromLocation();
		String file = document_root + fs_part;		// The stripped down 'location' will begin with a slash
		
		try {
            byte[] buffer = new byte[chunk_size];

            FileInputStream inputStream = new FileInputStream(file);

            int nRead = 0;
            while((nRead = inputStream.read(buffer)) != -1) {  
            	byte[] newbuf = new byte[curr_len + nRead];
            	
            	if (curr_len > 0)
            		System.arraycopy(output.data, 0, newbuf, 0, curr_len);
            	
            	System.arraycopy(buffer, 0, newbuf, curr_len, nRead);
            	
            	output.data = newbuf;
            	curr_len = output.data.length;
            }

            inputStream.close();
            
            // MIME type detection (not working on OS X, buggy Java)
            Path path = FileSystems.getDefault().getPath(file);
            output.mime_type = Files.probeContentType(path);
            if (output.mime_type == null)
            	output.mime_type = "application/octet-stream";
        }
        catch(IOException ex) {
        	output.http_status = 404;
        }
	}
	

}

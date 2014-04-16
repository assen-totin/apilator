package com.zavedil.apilator.core;

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
	private TaskOutput output;
	private String className;
	
	public StaticContent(String location) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Entering function StaticContent");
		
		final int chunk_size = 1000;
		int curr_len = 0;
		String document_root = Config.DocumentRoot;
		String file = document_root + location;				// The 'location' will begin with a slash
		
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
            
            String file_name = location.substring(1);
            Path path = FileSystems.getDefault().getPath(document_root, file_name);
            output.mime_type = Files.probeContentType(path);
        }
        catch(IOException ex) {
        	output.http_status = 404;
        }
	}
	
	/**
	 * Getter for output data object
	 * @return ApiTaskOutput Output data object
	 */
	public TaskOutput getOutput() {
		return output;
	}
}

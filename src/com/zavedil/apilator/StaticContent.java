package com.zavedil.apilator;

/**
 * A class to serve static content.
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
		
		// The 'location' will begin with a slash 
		String file = document_root + location;
		
		try {
            // Use this for reading the data.
            byte[] buffer = new byte[chunk_size];

            FileInputStream inputStream = new FileInputStream(file);

            // read() fills buffer with data and returns the number of bytes read  
            // (which may be less than the buffer size, but it will never be more).
            int nRead = 0;
            while((nRead = inputStream.read(buffer)) != -1) {  
            	byte[] newbuf = new byte[curr_len + nRead];
            	
            	if (curr_len > 0)
            		System.arraycopy(output_data, 0, newbuf, 0, curr_len);
            	
            	System.arraycopy(buffer, 0, newbuf, curr_len, nRead);
            	
            	output_data = newbuf;
            	curr_len = output_data.length;
            }

            // Always close files.
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
	 * @return
	 */
	public int getOutputHttpStatus() {
		return output_http_status;
	}
	
	public byte[] getOutputData() {
		return output_data;
	}
	
	public String getOutputMimeType() {
		return output_http_mime_type;
	}
}

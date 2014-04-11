package com.zavedil.apilator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StaticContent {
	private boolean error = false;
	private int file_size = 0;
	private byte[] file_content = null;
	private final int chunk_size = 1000;
	
	public StaticContent(String location) {
		int curr_len = 0;
		
		String document_root = Config.getDocumentRoot();
		
		// The 'location' will begin with a slash 
		String file = document_root + location;
		
		try {
            // Use this for reading the data.
            byte[] buffer = new byte[chunk_size];

            FileInputStream inputStream = new FileInputStream(file);

            // read fills buffer with data and returns the number of bytes read (which 
            // may be less than the buffer size, but it will never be more).
            int nRead = 0;
            while((nRead = inputStream.read(buffer)) != -1) {            	
            	byte[] newbuf = new byte[curr_len + nRead];
            	if (curr_len > 0)
            		System.arraycopy(file_content, 0, newbuf, 0, curr_len);
            	System.arraycopy(buffer, 0, newbuf, curr_len, nRead);
            	
            	file_content = newbuf;
            	curr_len = file_content.length;
                file_size += nRead;
            }	

            // Always close files.
            inputStream.close();		
        }
        catch(FileNotFoundException ex) {
            error = true;			
        }
        catch(IOException ex) {
        	error = true;
        }
	}
	
	public boolean getError() {
		return error;
	}
	
	public int getFileSize() {
		return file_size;
	}
	
	public byte[] getFileContent() {
		return file_content;
	}
}

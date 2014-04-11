package com.zavedil.apilator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StaticContent {
	private boolean error = false;
	private int file_size = 0;
	private byte[] file_content;
	private final int chunk_size = 1000;
	
	public StaticContent(String location) {
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
            	byte[] newbuf = new byte[file_content.length + nRead];
            	System.arraycopy(file_content, 0, newbuf, 0, file_content.length);
            	System.arraycopy(buffer, 0, newbuf, file_content.length, nRead);
            	
            	file_content = newbuf;
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

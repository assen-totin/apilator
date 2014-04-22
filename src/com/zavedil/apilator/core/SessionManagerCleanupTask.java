package com.zavedil.apilator.core;

/**
 * Session manager clean-upper and disk dumper task.  
 * The task is run by the clean-upper scheduler each minute.
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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TimerTask;

import com.zavedil.apilator.app.*;

public class SessionManagerCleanupTask extends TimerTask {
	private final String className;
	
	public SessionManagerCleanupTask() {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
	}
	
	public void run() {
		long now = System.currentTimeMillis();
		
		// Check if there are sessions which have expired: delete and send a notification to peers
		for (Map.Entry<String,Session> pair : SessionStorage.storage.entrySet()) {
			if (pair.getValue().getTtl() < now) {
				// Add to multicast queue a DELETE message
				SessionMessage sm = new SessionMessage(pair.getKey(), SessionMessage.MSG_DELETE);
				SessionStorage.queue_multicast.put(pair.getKey(), sm);
					
				// Delete session from storage
				SessionStorage.storage.remove(pair.getKey());
			}			
	    }
		
		// Dump the storage to disk
		try {
			OutputStream fos = new FileOutputStream(Config.SessionManagerDiskCache);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);			        
			oos.writeObject(SessionStorage.storage);
			fos.close();
		}
		catch (IOException e) {
			Logger.warning(className, "Could not write disk cache.");
		}
	}
}

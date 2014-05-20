package com.zavedil.apilator.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.zavedil.apilator.app.*;

/**
 * Session manager clean-upper for expired sessions and disk dumper scheduler.  
 * Runs a cleanup and disk dump task every minute.
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

public class SessionStorageCleanup {
	private final String className;
	private final SessionStorage sessionStorage;	
	
	public SessionStorageCleanup(SessionStorage ss) {
		className = this.getClass().getSimpleName();
		Logger.debug(className, "Creating new instance of the class.");
		sessionStorage = ss;
		
		Timer time = new Timer();

		time.schedule(
			new TimerTask(){
				public void run() {
					long now = System.currentTimeMillis();
					
					// Check if there are sessions which have expired: delete and send a notification to peers
					for (Map.Entry<String,Session> pair : sessionStorage.storage.entrySet()) {
						if (pair.getValue().getTtl() < now) 				
							// Delete session from storage (will also send a DELETE message over multicast)
							sessionStorage.del(pair.getKey());	
				    }
					
					// Dump the storage to disk
					try {
						OutputStream fos = new FileOutputStream(Config.SessionManagerDiskCache);
				        ObjectOutputStream oos = new ObjectOutputStream(fos);			        
						oos.writeObject(sessionStorage.storage);
						fos.close();
					}
					catch (IOException e) {
						Logger.warning(className, "Could not write disk cache.");
					}
				}
			}, 
			0, Config.SessionManagerCleanupperInterval
		);
	}
}

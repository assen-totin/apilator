package com.zavedil.apilator;

/**
 * Class implementing Knuth-Morris-Pratt Algorithm for Pattern Matching
 * @author unknown
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original author and date of work unknown.
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

public class HttpDecodeBinary {
	/**
	 * Finds the first occurrence of the pattern in the text
	 * @param data byte[] The haystack
	 * @param pattern byte[] The needle
	 * @return int The starting position (offset) of teh first occurrence of the needle in the haystack
	 */
	public int indexOf(byte[] data, byte[] pattern) {   	
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0) return -1;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) { j++; }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

	/**
	 * Computes the failure function using a boot-strapping process, where the pattern is matched against itself.
	 * @param pattern The needle
	 * @return int[] A map, specifying the occurrence count for each byte in the needle.
	 */
	private int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
    
	/**
	 * Same as indexOf(), but seek the last occurrence of the pattern
	 * @param data The haystak
	 * @param pattern The needle
	 * @return
	 */
	public int indexOfLast(byte[] data, byte[] pattern) {
    	boolean flag = true;
    	int idx=0;
    	
    	// Flip data
    	byte[] data_flipped = new byte[data.length];
    	for (int i=0; i<data.length; i++) 
    		data_flipped[i] = data[data.length - i - 1];
    	
    	// Flip pattern
    	byte[] pattern_flipped = new byte[pattern.length];
    	for (int i=0; i<pattern.length; i++) 
    		pattern_flipped[i] = pattern[pattern.length - i - 1];
    	
    	idx = this.indexOf(data_flipped, pattern_flipped);
    	if (idx > 0)
    		return data.length - idx - pattern.length;
    	
    	return -1;
    }
}

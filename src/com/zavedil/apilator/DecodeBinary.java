package com.zavedil.apilator;

/**
 * Knuth-Morris-Pratt Algorithm for Pattern Matching
 */

public class DecodeBinary {
    /**
     * Finds the first occurrence of the pattern in the text.
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
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
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
     * Find indexOf of the last occurrence of the pattern
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

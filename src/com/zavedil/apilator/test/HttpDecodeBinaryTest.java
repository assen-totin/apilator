package com.zavedil.apilator.test;

import com.zavedil.apilator.core.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HttpDecodeBinaryTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		final byte[] haystack = new byte[] { (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f};
		final byte[] needle1 = new byte[] {(byte)0x06, (byte)0x07};
		
		HttpDecodeBinary decoder = new HttpDecodeBinary();
		
		assertEquals("First occurrence of a substring should be found in the string", 5, decoder.indexOf(haystack, needle1));
		assertEquals("Last occurrence of a substring should be found in the string", 5, decoder.indexOfLast(haystack, needle1));
	}
}

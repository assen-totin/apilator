package com.zavedil.apilator.test;

import java.util.Arrays;

import com.zavedil.apilator.core.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HttpDecodeQuotedPrintableTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		final String input_s = "=08F'|=8D=8C|=9E=A2=96yij=95=A1+=";
		final byte[] input_b = input_s.getBytes();
		
		final byte[] output_b = new byte[] { (byte)0x08, (byte)0x46, (byte)0x27, (byte)0x7c, (byte)0x8d, (byte)0x8c, (byte)0x7c, (byte)0x9e, (byte)0xa2, (byte)0x96, (byte)0x79, (byte)0x69, (byte)0x6a, (byte)0x95, (byte)0xa1, (byte)0x2b};
		
		HttpDecodeQuotedPrintable decoder = new HttpDecodeQuotedPrintable();
		
		System.out.println(input_b.length);
		System.out.println(decoder.decode(input_b).length);
		System.out.println(bytesToHex(input_b));
		System.out.println(bytesToHex(decoder.decode(input_b)));
		
		assertEquals("Decoded string should equal unencoded", true, Arrays.equals(output_b, decoder.decode(input_b)));
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}

package com.zavedil.apilator.test;

import java.util.Arrays;

import com.zavedil.apilator.core.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HttpDecodeBase64Test {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		final String output_s = "Once upon a time in the west...";
		final byte[] output_b = output_s.getBytes();
		
		final String input_s = "T25jZSB1cG9uIGEgdGltZSBpbiB0aGUgd2VzdC4uLg==";
		final byte[] input_b = input_s.getBytes();
		
		HttpDecodeBase64 decoder = new HttpDecodeBase64();
		
		assertEquals("Decoded string should equal unencoded", true, Arrays.equals(output_b, decoder.decode(input_b)));
	}

}

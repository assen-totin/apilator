package com.zavedil.apilator;

import java.util.Arrays;

/** A very fast and memory efficient class to encode and decode to and from BASE64 in full accordance
 * with RFC 2045.<br><br>
 * On Windows XP sp1 with 1.4.2_04 and later ;), this encoder and decoder is about 10 times faster
 * on small arrays (10 - 1000 bytes) and 2-3 times as fast on larger arrays (10000 - 1000000 bytes)
 * compared to <code>sun.misc.Encoder()/Decoder()</code>.<br><br>
 *
 * On byte arrays the encoder is about 20% faster than Jakarta Commons Base64 Codec for encode and
 * about 50% faster for decoding large arrays. This implementation is about twice as fast on very small
 * arrays (&lt 30 bytes). If source/destination is a <code>String</code> this
 * version is about three times as fast due to the fact that the Commons Codec result has to be recoded
 * to a <code>String</code> from <code>byte[]</code>, which is very expensive.<br><br>
 *
 * This encode/decode algorithm doesn't create any temporary arrays as many other codecs do, it only
 * allocates the resulting array. This produces less garbage and it is possible to handle arrays twice
 * as large as algorithms that create a temporary array. (E.g. Jakarta Commons Codec). It is unknown
 * whether Sun's <code>sun.misc.Encoder()/Decoder()</code> produce temporary arrays but since performance
 * is quite low it probably does.<br><br>
 *
 * The encoder produces the same output as the Sun one except that the Sun's encoder appends
 * a trailing line separator if the last character isn't a pad. Unclear why but it only adds to the
 * length and is probably a side effect. Both are in conformance with RFC 2045 though.<br>
 * Commons codec seem to always att a trailing line separator.<br><br>
 *
 * <b>Note!</b>
 * The encode/decode method pairs (types) come in three versions with the <b>exact</b> same algorithm and
 * thus a lot of code redundancy. This is to not create any temporary arrays for transcoding to/from different
 * format types. The methods not used can simply be commented out.<br><br>
 *
 * There is also a "fast" version of all decode methods that works the same way as the normal ones, but
 * har a few demands on the decoded input. Normally though, these fast verions should be used if the source if
 * the input is known and it hasn't bee tampered with.<br><br>
 *
 * If you find the code useful or you find a bug, please send me a note at base64 @ miginfocom . com.
 *
 * Licence (BSD):
 * ==============
 *
 * Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (base64 @ miginfocom . com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * Neither the name of the MiG InfoCom AB nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * @version 2.2
 * @author Mikael Grev
 *         Date: 2004-aug-02
 *         Time: 11:31:11
 */

public class DecodeBase64 {
	private final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private final int[] IA = new int[256];
	{
		Arrays.fill(IA, -1);
		for (int i = 0, iS = CA.length; i < iS; i++)
			IA[CA[i]] = i;
		IA['='] = 0;
	}

	// ****************************************************************************************
	// *  byte[] version
	// ****************************************************************************************

	/** Decodes a BASE64 encoded byte array. All illegal characters will be ignored and can handle both arrays with
	 * and without line separators.
	 * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
	 * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
	 * (including '=') isn't divideable by 4. (I.e. definitely corrupted).
	 */
	public final byte[] decode(byte[] sArr)	{
		// Check special case
		int sLen = sArr.length;

		// Count illegal characters (including '\r', '\n') to know what size the returned array will be,
		// so we don't have to reallocate & copy it later.
		int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
		for (int i = 0; i < sLen; i++)      // If input is "pure" (I.e. no line separators or illegal chars) base64 this loop can be commented out.
			if (IA[sArr[i] & 0xff] < 0)
				sepCnt++;

		// Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
		if ((sLen - sepCnt) % 4 != 0)
			return null;

		int pad = 0;
		for (int i = sLen; i > 1 && IA[sArr[--i] & 0xff] <= 0;)
			if (sArr[i] == '=')
				pad++;

		int len = ((sLen - sepCnt) * 6 >> 3) - pad;

		byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

		for (int s = 0, d = 0; d < len;) {
			// Assemble three bytes into an int from four "valid" characters.
			int i = 0;
			for (int j = 0; j < 4; j++) {   // j only increased if a valid char was found.
				int c = IA[sArr[s++] & 0xff];
				if (c >= 0)
				    i |= c << (18 - j * 6);
				else
					j--;
			}

			// Add the bytes
			dArr[d++] = (byte) (i >> 16);
			if (d < len) {
				dArr[d++]= (byte) (i >> 8);
				if (d < len)
					dArr[d++] = (byte) i;
			}
		}

		return dArr;
	}

	// ****************************************************************************************
	// * String version
	// ****************************************************************************************

	/** Decodes a BASE64 encoded <code>String</code>. All illegal characters will be ignored and can handle both strings with
	 * and without line separators.<br>
	 * <b>Note!</b> It can be up to about 2x the speed to call <code>decode(str.toCharArray())</code> instead. That
	 * will create a temporary array though. This version will use <code>str.charAt(i)</code> to iterate the string.
	 * @param str The source string. <code>null</code> or length 0 will return an empty array.
	 * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
	 * (including '=') isn't divideable by 4.  (I.e. definitely corrupted).
	 */
	public final byte[] decode(String str) {
		// Check special case
		int sLen = str != null ? str.length() : 0;
		if (sLen == 0)
			return new byte[0];

		// Count illegal characters (including '\r', '\n') to know what size the returned array will be,
		// so we don't have to reallocate & copy it later.
		int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
		for (int i = 0; i < sLen; i++)  // If input is "pure" (I.e. no line separators or illegal chars) base64 this loop can be commented out.
			if (IA[str.charAt(i)] < 0)
				sepCnt++;

		// Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
		if ((sLen - sepCnt) % 4 != 0)
			return null;

		// Count '=' at end
		int pad = 0;
		for (int i = sLen; i > 1 && IA[str.charAt(--i)] <= 0;)
			if (str.charAt(i) == '=')
				pad++;

		int len = ((sLen - sepCnt) * 6 >> 3) - pad;

		byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

		for (int s = 0, d = 0; d < len;) {
			// Assemble three bytes into an int from four "valid" characters.
			int i = 0;
			for (int j = 0; j < 4; j++) {   // j only increased if a valid char was found.
				int c = IA[str.charAt(s++)];
				if (c >= 0)
				    i |= c << (18 - j * 6);
				else
					j--;
			}
			// Add the bytes
			dArr[d++] = (byte) (i >> 16);
			if (d < len) {
				dArr[d++]= (byte) (i >> 8);
				if (d < len)
					dArr[d++] = (byte) i;
			}
		}
		return dArr;
	}
}

package com.zavedil.apilator.core;

/**
 * Class implementing decoding from Quoted-Printable.
 * @author Funambol Inc.
 * @author Assen Totin assen.totin@gmail.com
 * 
 * Original work of Copyright (C) 2003 - 2007 Funambol, Inc.
 * Modified by the Apilator project, copyright (C) 2014 Assen Totin, assen.totin@gmail.com 
 */

// ORIGINAL LICENSE FOLLOWS

/**
 * Funambol is a mobile platform developed by Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

import java.io.UnsupportedEncodingException;

public class HttpDecodeQuotedPrintable {
	private byte HT = 0x09; // \t
	private byte LF = 0x0A; // \n
	private byte CR = 0x0D; // \r

	/**
	 * A method to decode quoted printable encoded data using the same array.
     * @param qp byte[] Input byte array to decode.
     * @return byte[] The decoded array.
     */
	public byte[] decode(byte[] qp) {
		final int qplen = qp.length;
		int retlen = 0;

		for (int i = 0; i < qplen; i++) {
			// Handle encoded chars
			if (qp[i] == '=') {
				if (qplen - i > 2) {
					// The sequence can be complete, check it
					if (qp[i + 1] == CR && qp[i + 2] == LF) {
						// soft line break, ignore it
						i += 2;
						continue;
					} 
					else if (isHexDigit(qp[i + 1]) && isHexDigit(qp[i + 2])) {
						// convert the number into an integer, taking
						// the ascii digits stored in the array.
						qp[retlen++] = (byte) (getHexValue(qp[i + 1]) * 16 + getHexValue(qp[i + 2]));
						i += 2;
						continue;
					} 
					//else 
						//Log.error("decode: Invalid sequence = " + qp[i + 1] + qp[i + 2]);

					// RFC 2045 says to exclude control characters mistakenly
					// present (unencoded) in the encoded stream.
					// As an exception, we keep unencoded tabs (0x09)
					if ((qp[i] >= 0x20 && qp[i] <= 0x7f) || qp[i] == HT || qp[i] == CR  || qp[i] == LF)
					qp[retlen++] = qp[i];
				}
			}
		}
		return qp;
	}

	/**
	 * Method to check if a byte is valid hexadecimal number.
	 * @param b byte The byte to check
	 * @return boolean TRUE if the byte is a valid hexadecimal number, FALSE otherwise.
	 */
	private boolean isHexDigit(final byte b) {
		return ((b >= 0x30 && b <= 0x39) || (b >= 0x41 && b <= 0x46));
	}

	/**
	 * Method to convert a byte
	 * @param b byte The byte to convert
	 * @return byte The converted value
	 */
	private byte getHexValue(final byte b) {
		return (byte) Character.digit((char) b, 16);
	}

	/**
     * A wrapper method for decoding to a String with a specified encoding; uses decode().
     * @param qp byte[] Byte array to decode
     * @param enc String The character encoding of the decoded string
     * @return String The decoded string.
     */
	public String decode(byte[] qp, final String enc) {
		qp = decode(qp);
		final int len = qp.length; 
		try {
			return new String(qp, 0, len, enc);
		} 
		catch (final UnsupportedEncodingException e) {
			//  Log.error("qp.decode: " + enc + " not supported. " + e.toString());
			return new String(qp, 0, len);
		}
	}
}


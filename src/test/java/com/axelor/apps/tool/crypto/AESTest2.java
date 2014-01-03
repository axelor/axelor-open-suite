/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.junit.Test;

/**
 * From: http://stackoverflow.com/questions/587357/rijndael-support-in-java
 */

public class AESTest2 {

	@Test
	public void test() throws Exception {

		String message = "Hello World";

		byte[] CRYPTO_KEY_EXT = { (byte) 0xEF, (byte) 0xA4, (byte) 0xA8,
				(byte) 0x04, (byte) 0xB6, (byte) 0x14, (byte) 0x3E,
				(byte) 0xF7, (byte) 0xCE, (byte) 0xD2, (byte) 0xA2,
				(byte) 0x78, (byte) 0x10, (byte) 0xB2, (byte) 0x2B, (byte) 0x43 };

		byte[] CRYPTO_IV_EXT = { (byte) 0xCC, (byte) 0xBA, (byte) 0xAC,
				(byte) 0x54, (byte) 0xA2, (byte) 0x35, (byte) 0x56,
				(byte) 0x9E, (byte) 0xEA, (byte) 0x36, (byte) 0xAB,
				(byte) 0x31, (byte) 0xBC, (byte) 0xB4, (byte) 0x34, (byte) 0x31 };

		byte[] sessionKey = CRYPTO_KEY_EXT; // Where you get this from is beyond
											// the scope of this post
		byte[] iv = CRYPTO_IV_EXT; // Ditto

		byte[] plaintext = message.getBytes("UTF8"); // Whatever you want to
														// encrypt/decrypt
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// You can use ENCRYPT_MODE or DECRYPT_MODE
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sessionKey, "AES"),
				new IvParameterSpec(iv));
		byte[] ciphertext = cipher.doFinal(plaintext);

		Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// You can use DECRYPT_MODE or DECRYPT_MODE
		cipher2.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sessionKey, "AES"),
				new IvParameterSpec(iv));
		byte[] roundTriptext = cipher2.doFinal(ciphertext);
		String roundTrip = new String(roundTriptext, "UTF8");
		
		Assert.assertEquals(message, roundTrip);
	
	}
}

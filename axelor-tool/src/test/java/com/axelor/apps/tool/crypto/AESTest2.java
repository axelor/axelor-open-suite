/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Assert;
import org.junit.Test;

/** From: http://stackoverflow.com/questions/587357/rijndael-support-in-java */
public class AESTest2 {

  @Test
  public void test() throws Exception {

    String message = "Hello World";

    byte[] CRYPTO_KEY_EXT = {
      (byte) 0xEF,
      (byte) 0xA4,
      (byte) 0xA8,
      (byte) 0x04,
      (byte) 0xB6,
      (byte) 0x14,
      (byte) 0x3E,
      (byte) 0xF7,
      (byte) 0xCE,
      (byte) 0xD2,
      (byte) 0xA2,
      (byte) 0x78,
      (byte) 0x10,
      (byte) 0xB2,
      (byte) 0x2B,
      (byte) 0x43
    };

    byte[] CRYPTO_IV_EXT = {
      (byte) 0xCC,
      (byte) 0xBA,
      (byte) 0xAC,
      (byte) 0x54,
      (byte) 0xA2,
      (byte) 0x35,
      (byte) 0x56,
      (byte) 0x9E,
      (byte) 0xEA,
      (byte) 0x36,
      (byte) 0xAB,
      (byte) 0x31,
      (byte) 0xBC,
      (byte) 0xB4,
      (byte) 0x34,
      (byte) 0x31
    };

    byte[] sessionKey = CRYPTO_KEY_EXT; // Where you get this from is beyond
    // the scope of this post
    byte[] iv = CRYPTO_IV_EXT; // Ditto

    byte[] plaintext = message.getBytes("UTF8"); // Whatever you want to
    // encrypt/decrypt
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    // You can use ENCRYPT_MODE or DECRYPT_MODE
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sessionKey, "AES"), new IvParameterSpec(iv));
    byte[] ciphertext = cipher.doFinal(plaintext);

    Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
    // You can use DECRYPT_MODE or DECRYPT_MODE
    cipher2.init(
        Cipher.DECRYPT_MODE, new SecretKeySpec(sessionKey, "AES"), new IvParameterSpec(iv));
    byte[] roundTriptext = cipher2.doFinal(ciphertext);
    String roundTrip = new String(roundTriptext, "UTF8");

    Assert.assertEquals(message, roundTrip);
  }
}

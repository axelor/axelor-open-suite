/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Assert;
import org.junit.Test;

/**
 * From: http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html This program
 * generates a AES key, retrieves its raw bytes, and then reinstantiates a AES key from the key
 * bytes. The reinstantiated key is used to initialize a AES cipher for encryption and decryption.
 */
public class AESTest {

  /**
   * Turns array of bytes into string
   *
   * @param buf Array of bytes to convert to hex string
   * @return Generated hex string
   */
  public static String asHex(byte buf[]) {
    StringBuffer strbuf = new StringBuffer(buf.length * 2);
    int i;

    for (i = 0; i < buf.length; i++) {
      if (((int) buf[i] & 0xff) < 0x10) strbuf.append("0");

      strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
    }

    return strbuf.toString();
  }

  @Test
  public void test() throws Exception {

    // Get the KeyGenerator
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(128); // 192 and 256 bits may not be available

    // Generate the secret key specs.
    SecretKey skey = kgen.generateKey();
    byte[] raw = skey.getEncoded();

    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

    // Instantiate the cipher

    Cipher cipher = Cipher.getInstance("AES");

    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

    byte[] encrypted = cipher.doFinal("Hello World".getBytes());

    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    byte[] original = cipher.doFinal(encrypted);
    String originalString = new String(original);
    Assert.assertEquals("Hello World", originalString);
  }
}

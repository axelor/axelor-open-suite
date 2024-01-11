/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.certificate;

/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Some key utilities
 *
 * @author hachani
 */
public class KeyUtil {

  /**
   * Generates a <code>KeyPair</code> in RSA format.
   *
   * @param keyLen - key size
   * @return KeyPair the key pair
   * @throws NoSuchAlgorithmException
   */
  public static KeyPair makeKeyPair(int keyLen) throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen;

    keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(keyLen, new SecureRandom());

    KeyPair keypair = keyGen.generateKeyPair();

    return keypair;
  }

  /**
   * Generates a random password
   *
   * @return the password
   */
  public static String generatePassword() {
    SecureRandom random;

    try {
      random = SecureRandom.getInstance("SHA1PRNG");
      String pwd = Base64.encodeBase64String(random.generateSeed(5));

      return pwd.substring(0, pwd.length() - 2);
    } catch (NoSuchAlgorithmException e) {
      return "changeit";
    }
  }

  /**
   * Returns the digest value of a given public key.
   *
   * <p>In Version “H003” of the EBICS protocol the ES of the financial:
   *
   * <p>The SHA-256 hash values of the financial institution's public keys for X002 and E002 are
   * composed by concatenating the exponent with a blank character and the modulus in hexadecimal
   * representation (using lower case letters) without leading zero (as to the hexadecimal
   * representation). The resulting string has to be converted into a byte array based on US ASCII
   * code.
   *
   * @param publicKey the public key
   * @return the digest value
   * @throws EbicsException
   */
  public static byte[] getKeyDigest(RSAPublicKey publicKey) throws AxelorException {
    String modulus;
    String exponent;
    String hash;
    byte[] digest;

    exponent = Hex.encodeHexString(publicKey.getPublicExponent().toByteArray());
    modulus = Hex.encodeHexString(removeFirstByte(publicKey.getModulus().toByteArray()));
    hash = exponent + " " + modulus;

    if (hash.charAt(0) == '0') {
      hash = hash.substring(1);
    }

    try {
      digest = MessageDigest.getInstance("SHA-256", "BC").digest(hash.getBytes("US-ASCII"));
    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }

    return new String(Hex.encodeHex(digest, false)).getBytes();
  }

  /**
   * Remove the first byte of an byte array
   *
   * @return the array
   */
  private static byte[] removeFirstByte(byte[] byteArray) {
    byte[] b = new byte[byteArray.length - 1];
    System.arraycopy(byteArray, 1, b, 0, b.length);
    return b;
  }
}

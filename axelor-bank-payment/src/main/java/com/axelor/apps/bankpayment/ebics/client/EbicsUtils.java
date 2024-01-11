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
package com.axelor.apps.bankpayment.ebics.client;

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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.xml.XPathParse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.utils.IgnoreAllErrorHandler;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Some utilities for EBICS request creation and reception
 *
 * @author hachani
 */
public class EbicsUtils {

  private static final String HTTP_PROXY_HOST = "http.proxy.host";
  private static final String HTTP_PROXY_PORT = "http.proxy.port";
  private static final String HTTP_PROXY_AUTH_USER = "http.proxy.auth.user";
  private static final String HTTP_PROXY_AUTH_PASSWORD = "http.proxy.auth.password";

  /**
   * Compresses an input of byte array
   *
   * <p>The Decompression is ensured via Universal compression algorithm (RFC 1950, RFC 1951) As
   * specified in the EBICS specification (16 Appendix: Standards and references)
   *
   * @param toZip the input to be compressed
   * @return the compressed input data
   * @throws IOException compression failed
   */
  public static byte[] zip(byte[] toZip) throws AxelorException {

    if (toZip == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get("The input to be zipped cannot be null"));
    }

    Deflater compressor;
    ByteArrayOutputStream output;
    byte[] buffer;

    output = new ByteArrayOutputStream(toZip.length);
    buffer = new byte[1024];
    compressor = new Deflater(Deflater.BEST_COMPRESSION);
    compressor.setInput(toZip);
    compressor.finish();

    while (!compressor.finished()) {
      int count = compressor.deflate(buffer);
      output.write(buffer, 0, count);
    }

    try {
      output.close();
    } catch (IOException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    compressor.end();

    return output.toByteArray();
  }

  /**
   * Generates a random nonce.
   *
   * <p>EBICS Specification 2.4.2 - 11.6 Generation of the transaction IDs:
   *
   * <p>Transaction IDs are cryptographically-strong random numbers with a length of 128 bits. This
   * means that the likelihood of any two bank systems using the same transaction ID at the same
   * time is sufficiently small.
   *
   * <p>Transaction IDs are generated by cryptographic pseudo-random number generators (PRNG) that
   * have been initialized with a real random number (seed). The entropy of the seed should be at
   * least 100 bits.
   *
   * @return a random nonce.
   * @throws EbicsException nonce generation fails.
   */
  public static byte[] generateNonce() throws AxelorException {
    SecureRandom secureRandom;

    try {
      secureRandom = SecureRandom.getInstance("SHA1PRNG");
      return secureRandom.generateSeed(16);
    } catch (NoSuchAlgorithmException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  /**
   * Uncompresses a given byte array input.
   *
   * <p>The Decompression is ensured via Universal compression algorithm (RFC 1950, RFC 1951) As
   * specified in the EBICS specification (16 Appendix: Standards and references)
   *
   * @param zip the zipped input.
   * @return the uncompressed data.
   */
  public static byte[] unzip(byte[] zip) throws AxelorException {
    Inflater decompressor;
    ByteArrayOutputStream output;
    byte[] buf;

    decompressor = new Inflater();
    output = new ByteArrayOutputStream(zip.length);
    decompressor.setInput(zip);
    buf = new byte[1024];

    while (!decompressor.finished()) {
      int count;

      try {
        count = decompressor.inflate(buf);
      } catch (DataFormatException e) {
        throw new AxelorException(
            e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
      }
      output.write(buf, 0, count);
    }

    try {
      output.close();
    } catch (IOException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    decompressor.end();

    return output.toByteArray();
  }

  /**
   * Canonizes an input with inclusive c14n without comments algorithm.
   *
   * <p>EBICS Specification 2.4.2 - 5.5.1.1.1 EBICS messages in transaction initialization:
   *
   * <p>The identification and authentication signature includes all XML elements of the EBICS
   * request whose attribute value for @authenticate is equal to “true”. The definition of the XML
   * schema “ebics_request.xsd“ guarantees that the value of the attribute @authenticate is equal to
   * “true” for precisely those elements that also need to be signed.
   *
   * <p>Thus, All the Elements with the attribute authenticate = true and their sub elements are
   * considered for the canonization process. This is performed via the {@link
   * XPathAPI#selectNodeIterator(Node, String) selectNodeIterator(Node, String)}.
   *
   * @param input the byte array XML input.
   * @return the canonized form of the given XML
   * @throws EbicsException
   */
  public static byte[] canonize(byte[] input) throws AxelorException {
    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    Document document;
    NodeIterator iter;
    ByteArrayOutputStream output;
    Node node;

    try {
      factory = Beans.get(XPathParse.class).getDocumentBuilderFactory();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new IgnoreAllErrorHandler());
      document = builder.parse(new ByteArrayInputStream(input));
      iter = XPathAPI.selectNodeIterator(document, "//*[@authenticate='true']");
      output = new ByteArrayOutputStream();
      while ((node = iter.nextNode()) != null) {
        Canonicalizer canonicalizer;

        canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        canonicalizer.canonicalizeSubtree(node, output);
      }

      return output.toByteArray();
    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Encrypts an input with a given key spec.
   *
   * <p>EBICS Specification 2.4.2 - 15.1 Workflows at the sender’s end:
   *
   * <p><b>Preparation for DEK encryption</b>
   *
   * <p>The 128 bit DEK that is interpreted as a natural number is filled out with null bits to 768
   * bits in front of the highest-value bit. The result is called PDEK.
   *
   * <p><b>Encryption of the secret DES key</b>
   *
   * <p>PDEK is then encrypted with the recipient’s public key of the RSA key system and is then
   * expanded with leading null bits to 1024 bits.
   *
   * <p>The result is called EDEK. It must be ensured that EDEK is not equal to DEK.
   *
   * <p><b>Encryption of the messages</b>
   *
   * <p><U>Padding of the message:</U>
   *
   * <p>The method Padding with Octets in accordance with ANSI X9.23 is used for padding the
   * message, i.e. in all cases, data is appended to the message that is to be encrypted.
   *
   * <p><U>Application of the encryption algorithm:</U>
   *
   * <p>The message is encrypted in CBC mode in accordance with ANSI X3.106 with the secret key DEK
   * according to the 2-key triple DES process as specified in ANSI X3.92-1981.
   *
   * <p>In doing this, the following initialization value “ICV” is used: X ‘00 00 00 00 00 00 00
   * 00’.
   *
   * @param input the input to encrypt
   * @param keySpec the key spec
   * @return the encrypted input
   * @throws EbicsException
   */
  public static byte[] encrypt(byte[] input, SecretKeySpec keySpec) throws AxelorException {
    return encryptOrDecrypt(Cipher.ENCRYPT_MODE, input, keySpec);
  }

  /**
   * Decrypts the given input according to key spec.
   *
   * @param input the input to decrypt
   * @param keySpec the key spec
   * @return the decrypted input
   * @throws EbicsException
   */
  public static byte[] decrypt(byte[] input, SecretKeySpec keySpec) throws AxelorException {
    return encryptOrDecrypt(Cipher.DECRYPT_MODE, input, keySpec);
  }

  /**
   * Encrypts or decrypts the given input according to key spec.
   *
   * @param mode the encryption-decryption mode.
   * @param input the input to encrypt or decrypt.
   * @param keySpec the key spec.
   * @return the encrypted or decrypted data.
   * @throws GeneralSecurityException
   */
  private static byte[] encryptOrDecrypt(int mode, byte[] input, SecretKeySpec keySpec)
      throws AxelorException {
    IvParameterSpec iv;
    Cipher cipher;

    iv = new IvParameterSpec(new byte[16]);
    try {
      cipher = Cipher.getInstance("AES/CBC/ISO10126Padding", BouncyCastleProvider.PROVIDER_NAME);
      cipher.init(mode, keySpec, iv);
      return cipher.doFinal(input);
    } catch (GeneralSecurityException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Checks for the returned http code
   *
   * @param httpCode the http code
   * @throws EbicsException
   */
  public static void checkHttpCode(int httpCode) throws AxelorException {
    if (httpCode != 200) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, "http.code.error[Code:%s]", httpCode);
    }
  }

  /**
   * * To set proxy settings in HttpClientBuilder
   *
   * @param client
   */
  public static void setProxy(HttpClientBuilder client) {

    final AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);

    if (StringUtils.isBlank(proxyHost) || proxyPort == 0) {
      return;
    }

    HttpHost proxy = new HttpHost(proxyHost.trim(), proxyPort);
    DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
    client.setRoutePlanner(routePlanner);

    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    if (StringUtils.isBlank(userName) || StringUtils.isBlank(userPassword)) {
      return;
    }

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        new AuthScope(proxy),
        new UsernamePasswordCredentials(userName.trim(), userPassword.trim()));
    client.setDefaultCredentialsProvider(credentialsProvider);
  }
}

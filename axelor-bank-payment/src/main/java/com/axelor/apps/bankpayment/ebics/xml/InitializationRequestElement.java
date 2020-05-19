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
package com.axelor.apps.bankpayment.ebics.xml;

import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.xml.XMLConstants;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The <code>InitializationRequestElement</code> is the root element for ebics uploads and downloads
 * requests. The response of this element is then used either to upload or download files from the
 * ebics server.
 *
 * @author Hachani
 */
public abstract class InitializationRequestElement extends DefaultEbicsRootElement {

  /**
   * Construct a new <code>InitializationRequestElement</code> root element.
   *
   * @param session the current ebics session.
   * @param type the initialization type (UPLOAD, DOWNLOAD).
   * @param name the element name.
   * @throws EbicsException
   */
  public InitializationRequestElement(EbicsSession session, OrderType type, String name)
      throws AxelorException {
    super(session);
    this.type = type;
    this.name = name;
    nonce = EbicsUtils.generateNonce();
  }

  @Override
  public void build() throws AxelorException {
    SignedInfo signedInfo;

    buildInitialization();
    signedInfo = new SignedInfo(session.getUser(), getDigest());
    signedInfo.build();
    ((EbicsRequestDocument) document)
        .getEbicsRequest()
        .setAuthSignature((SignatureType) signedInfo.getSignatureType());
    ((EbicsRequestDocument) document)
        .getEbicsRequest()
        .getAuthSignature()
        .setSignatureValue(
            EbicsXmlFactory.createSignatureValueType(signedInfo.sign(toByteArray())));
  }

  @Override
  public String getName() {
    return name + ".xml";
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", XMLConstants.DEFAULT_NS_PREFIX);

    return super.toByteArray();
  }

  /**
   * Returns the digest value of the authenticated XML portions.
   *
   * @return the digest value.
   * @throws EbicsException Failed to retrieve the digest value.
   */
  public byte[] getDigest() throws AxelorException {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");

    try {
      return MessageDigest.getInstance("SHA-256", "BC").digest(EbicsUtils.canonize(toByteArray()));
    } catch (NoSuchAlgorithmException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    } catch (NoSuchProviderException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Returns the element type.
   *
   * @return the element type.
   */
  public String getType() {
    return type.getOrderType();
  }

  /**
   * Decodes an hexadecimal input.
   *
   * @param hex the hexadecimal input
   * @return the decoded hexadecimal value
   * @throws EbicsException
   */
  protected byte[] decodeHex(byte[] hex) throws AxelorException {
    if (hex == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Bank digest is empty, HPB request must be performed before");
    }

    try {
      return Hex.decodeHex((new String(hex)).toCharArray());
    } catch (DecoderException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Generates the upload transaction key
   *
   * @return the transaction key
   */
  protected byte[] generateTransactionKey() throws AxelorException {
    try {
      Cipher cipher;

      cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
      cipher.init(Cipher.ENCRYPT_MODE, session.getBankE002Key());

      return cipher.doFinal(nonce);
    } catch (Exception e) {
      e.printStackTrace();
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Builds the initialization request according to the element type.
   *
   * @throws EbicsException build fails
   */
  public abstract void buildInitialization() throws AxelorException;

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String name;
  protected OrderType type;
  protected byte[] nonce;
}

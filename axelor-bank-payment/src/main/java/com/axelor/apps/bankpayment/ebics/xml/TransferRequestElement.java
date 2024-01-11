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
package com.axelor.apps.bankpayment.ebics.xml;

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

import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.xml.XMLConstants;

/**
 * The <code>TransferRequestElement</code> is the common root element for all ebics transfer for the
 * bank server.
 *
 * @author Hachani
 */
public abstract class TransferRequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new <code>TransferRequestElement</code> element.
   *
   * @param session the current ebics session
   * @param name the element name
   * @param type the order type
   * @param segmentNumber the segment number to be sent
   * @param lastSegment is it the last segment?
   * @param transactionID the transaction ID
   */
  public TransferRequestElement(
      EbicsSession session,
      String name,
      OrderType type,
      int segmentNumber,
      boolean lastSegment,
      byte[] transactionId) {
    super(session);
    this.type = type;
    this.name = name;
    this.segmentNumber = segmentNumber;
    this.lastSegment = lastSegment;
    this.transactionId = transactionId;
  }

  @Override
  public void build() throws AxelorException {
    SignedInfo signedInfo;

    buildTransfer();
    signedInfo = new SignedInfo(session.getUser(), getDigest());
    signedInfo.build();
    ((EbicsRequestDocument) document)
        .getEbicsRequest()
        .setAuthSignature(signedInfo.getSignatureType());
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
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Returns the order type of the element.
   *
   * @return the order type element.
   */
  public String getOrderType() {
    return type.getOrderType();
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", XMLConstants.DEFAULT_NS_PREFIX);

    return super.toByteArray();
  }

  /**
   * Builds the transfer request.
   *
   * @throws EbicsException
   */
  public abstract void buildTransfer() throws AxelorException;

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  protected int segmentNumber;
  protected boolean lastSegment;
  protected byte[] transactionId;
  private OrderType type;
  private String name;
}

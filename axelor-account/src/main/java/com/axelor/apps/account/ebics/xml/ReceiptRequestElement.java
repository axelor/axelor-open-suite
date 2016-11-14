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

package com.axelor.apps.account.ebics.xml;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.TransferReceipt;
import com.axelor.apps.account.ebics.utils.Utils;
import com.axelor.apps.account.ebics.client.DefaultEbicsRootElement;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;


/**
 * The <code>ReceiptRequestElement</code> is the element containing the
 * receipt request to tell the server bank that all segments are received.
 *
 * @author Hachani
 *
 */
public class ReceiptRequestElement extends DefaultEbicsRootElement {

  /**
   * Construct a new <code>ReceiptRequestElement</code> element.
   * @param session the current ebics session
   * @param name the element name
   */
  public ReceiptRequestElement(EbicsSession session,
                               byte[] transactionId,
                               String name)
  {
    super(session);
    this.transactionId = transactionId;
    this.name = name;
  }

  @Override
  public void build() throws AxelorException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    StaticHeaderType 			xstatic;
    TransferReceipt			transferReceipt;
    SignedInfo				signedInfo;

    mutable = EbicsXmlFactory.createMutableHeaderType("Receipt", null);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    transferReceipt = EbicsXmlFactory.createTransferReceipt(true, 0);
    body = EbicsXmlFactory.createEbicsRequestBody(transferReceipt);
    request = EbicsXmlFactory.createEbicsRequest(1,
	                                         "H003",
	                                         header,
	                                         body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
    signedInfo = new SignedInfo(session.getUser(), getDigest());
    signedInfo.build();
    ((EbicsRequestDocument)document).getEbicsRequest().setAuthSignature(signedInfo.getSignatureType());
    ((EbicsRequestDocument)document).getEbicsRequest().getAuthSignature().setSignatureValue(EbicsXmlFactory.createSignatureValueType(signedInfo.sign(toByteArray())));
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", "");

    return super.toByteArray();
  }

  @Override
  public String getName() {
    return name  + ".xml";
  }

  /**
   * Returns the digest value of the authenticated XML portions.
   * @return  the digest value.
   * @throws EbicsException Failed to retrieve the digest value.
   */
  public byte[] getDigest() throws AxelorException {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");

    try {
      return MessageDigest.getInstance("SHA-256", "BC").digest(Utils.canonize(toByteArray()));
    } catch (NoSuchAlgorithmException e) {
      throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR);
    } catch (NoSuchProviderException e) {
      throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR);
    }
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[] 			transactionId;
  private String			name;
  private static final long 		serialVersionUID = -1969616441705744725L;
}

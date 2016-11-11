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

import java.math.BigInteger;
import java.util.Calendar;

import com.axelor.apps.account.ebics.schema.s001.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyOrderDataType;
import com.axelor.apps.account.ebics.schema.xmldsig.RSAKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;
import com.axelor.apps.account.ebics.client.DefaultEbicsRootElement;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.exception.AxelorException;


/**
 * The <code>SignaturePubKeyOrderDataElement</code> is the order data
 * component for the INI request.
 *
 * @author hachani
 *
 */
public class SignaturePubKeyOrderDataElement extends DefaultEbicsRootElement {

  /**
   * Creates a new Signature Public Key Order Data element.
   * @param session the current ebics session
   */
  public SignaturePubKeyOrderDataElement(EbicsSession session) {
    super(session);
  }

  @Override
  public void build() throws AxelorException {
    SignaturePubKeyInfoType		signaturePubKeyInfo;
    X509DataType 			x509Data;
    RSAKeyValueType 			rsaKeyValue;
    PubKeyValueType 			pubKeyValue;
    SignaturePubKeyOrderDataType	signaturePubKeyOrderData;

    x509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
	                                          session.getUser().getA005Certificate());
    rsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(  new BigInteger( session.getUser().getA005PublicKeyExponent()).toByteArray(),
	                                               new BigInteger( session.getUser().getA005PublicKeyModulus()).toByteArray());
    pubKeyValue = EbicsXmlFactory.createPubKeyValueType(rsaKeyValue, Calendar.getInstance());
    signaturePubKeyInfo = EbicsXmlFactory.createSignaturePubKeyInfoType(x509Data,
	                                                                pubKeyValue,
	                                                                "A005");
    signaturePubKeyOrderData = EbicsXmlFactory.createSignaturePubKeyOrderData(signaturePubKeyInfo,
									      session.getUser().getEbicsPartner().getPartnerId(),
									      session.getUser().getUserId());
    document = EbicsXmlFactory.createSignaturePubKeyOrderDataDocument(signaturePubKeyOrderData);
  }

  @Override
  public String getName() {
    return "SignaturePubKeyOrderData.xml";
  }

  @Override
  public byte[] toByteArray() {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
    setSaveSuggestedPrefixes("http://www.ebics.org/S001", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long 		serialVersionUID = -5523105558015982970L;
}

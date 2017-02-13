/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.ebics.xml;

import java.math.BigInteger;
import java.util.Calendar;

import com.axelor.apps.account.db.EbicsCertificate;
import com.axelor.apps.account.ebics.schema.s001.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyOrderDataType;
import com.axelor.apps.account.ebics.schema.xmldsig.RSAKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;
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
    
    EbicsCertificate certificate = session.getUser().getA005Certificate();
    x509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),certificate.getCertificate());
    rsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(  new BigInteger( certificate.getPublicKeyExponent()).toByteArray(),
	                                               new BigInteger( certificate.getPublicKeyModulus()).toByteArray());
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

}

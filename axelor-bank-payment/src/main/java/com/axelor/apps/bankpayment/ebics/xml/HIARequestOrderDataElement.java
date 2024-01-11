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

import com.axelor.apps.account.ebics.schema.h003.AuthenticationPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.HIARequestOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.RSAKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import java.math.BigInteger;
import java.util.Calendar;
import javax.xml.XMLConstants;

/**
 * The <code>HIARequestOrderDataElement</code> is the element that contains X002 and E002 keys
 * information needed for a HIA request in order to send the authentication and encryption user keys
 * to the bank server.
 *
 * @author hachani
 */
public class HIARequestOrderDataElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new HIA Request Order Data element
   *
   * @param session the current ebics session
   */
  public HIARequestOrderDataElement(EbicsSession session) {
    super(session);
  }

  @Override
  public void build() {
    HIARequestOrderDataType request;
    AuthenticationPubKeyInfoType authenticationPubKeyInfo;
    EncryptionPubKeyInfoType encryptionPubKeyInfo;
    PubKeyValueType encryptionPubKeyValue;
    X509DataType encryptionX509Data = null;
    RSAKeyValueType encryptionRsaKeyValue;
    PubKeyValueType authPubKeyValue;
    X509DataType authX509Data = null;
    RSAKeyValueType authRsaKeyValue;

    EbicsCertificate certificate = session.getUser().getE002Certificate();

    encryptionX509Data =
        EbicsXmlFactory.createX509DataType(session.getUser().getDn(), certificate.getCertificate());

    // Include Certificate issuer and serial ?
    // encryptionX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
    // certificate.getCertificate(), certificate.getIssuer(),   new
    // BigInteger(certificate.getSerial(), 16));

    encryptionRsaKeyValue =
        EbicsXmlFactory.createRSAKeyValueType(
            new BigInteger(certificate.getPublicKeyExponent(), 16).toByteArray(),
            new BigInteger(certificate.getPublicKeyModulus(), 16).toByteArray());

    encryptionPubKeyValue =
        EbicsXmlFactory.createH003PubKeyValueType(encryptionRsaKeyValue, Calendar.getInstance());
    encryptionPubKeyInfo =
        EbicsXmlFactory.createEncryptionPubKeyInfoType(
            "E002", encryptionPubKeyValue, encryptionX509Data);
    certificate = session.getUser().getX002Certificate();

    authX509Data =
        EbicsXmlFactory.createX509DataType(session.getUser().getDn(), certificate.getCertificate());

    //  Include Certificate issuer and serial ?
    //	authX509Data = EbicsXmlFactory.createX509DataType(session.getUser().getDn(),
    // certificate.getCertificate(), certificate.getIssuer(),   new
    // BigInteger(certificate.getSerial(), 16));

    authRsaKeyValue =
        EbicsXmlFactory.createRSAKeyValueType(
            new BigInteger(certificate.getPublicKeyExponent(), 16).toByteArray(),
            new BigInteger(certificate.getPublicKeyModulus(), 16).toByteArray());

    authPubKeyValue =
        EbicsXmlFactory.createH003PubKeyValueType(authRsaKeyValue, Calendar.getInstance());
    authenticationPubKeyInfo =
        EbicsXmlFactory.createAuthenticationPubKeyInfoType("X002", authPubKeyValue, authX509Data);
    request =
        EbicsXmlFactory.createHIARequestOrderDataType(
            authenticationPubKeyInfo,
            encryptionPubKeyInfo,
            session.getUser().getEbicsPartner().getPartnerId(),
            session.getUser().getUserId());
    document = EbicsXmlFactory.createHIARequestOrderDataDocument(request);
  }

  @Override
  public String getName() {
    return "HIARequestOrderData.xml";
  }

  @Override
  public byte[] toByteArray() {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
    setSaveSuggestedPrefixes("http://www.ebics.org/S001", XMLConstants.DEFAULT_NS_PREFIX);

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

}
